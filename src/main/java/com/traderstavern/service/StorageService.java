package com.traderstavern.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traderstavern.analysis.MarketSentiment;
import com.traderstavern.analysis.TechnicalIndicators;
import com.traderstavern.db.DatabaseManager;
import com.traderstavern.model.PriceAlert;
import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class StorageService {
    private static final String CONFIG_GROUP = "traderstavern";
    private static final String WATCHED_ITEMS_KEY = "watchedItems";
    private static final String ALERTS_KEY = "alerts";
    private static final String DATA_DIR = System.getProperty("user.home") + 
        "/.runelite/traderstavern";
    
    private final ConfigManager configManager;
    private final ObjectMapper mapper;
    private final Map<Integer, List<PriceData>> priceCache;
    private final DatabaseManager databaseManager;
    private final Path dataPath;
    
    @Inject
    public StorageService(ConfigManager configManager, DatabaseManager databaseManager) {
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.mapper = new ObjectMapper();
        this.priceCache = new ConcurrentHashMap<>();
        this.dataPath = Path.of(DATA_DIR);
        
        initializeStorage();
    }
    
    private void initializeStorage() {
        try {
            Files.createDirectories(dataPath);
            loadPriceHistory();
        } catch (IOException e) {
            log.error("Failed to initialize storage", e);
        }
    }
    
    // Price History Management
    
    public void savePriceData(int itemId, PriceData price) {
        // Save to database
        databaseManager.savePriceData(itemId, price);
        
        // Update cache
        List<PriceData> history = priceCache.computeIfAbsent(itemId, 
            k -> new ArrayList<>());
        history.add(price);
        
        // Keep only last 100 price points
        if (history.size() > 100) {
            history = history.subList(history.size() - 100, history.size());
            priceCache.put(itemId, history);
        }
    }
    
    public List<PriceData> getPriceHistory(int itemId) {
        // Try cache first
        List<PriceData> cached = priceCache.get(itemId);
        if (cached != null) {
            return cached;
        }
        
        // Load from database
        List<PriceData> history = databaseManager.getPriceHistory(itemId);
        priceCache.put(itemId, history);
        return history;
    }
    
    private void savePriceHistory(int itemId) {
        try {
            Path filePath = dataPath.resolve("price_" + itemId + ".json");
            List<PriceData> history = priceCache.get(itemId);
            if (history != null) {
                mapper.writeValue(filePath.toFile(), history);
            }
        } catch (IOException e) {
            log.error("Failed to save price history for item {}", itemId, e);
        }
    }
    
    public Map<Integer, List<PriceData>> loadPriceHistory() {
        try {
            File[] files = dataPath.toFile().listFiles((dir, name) -> 
                name.startsWith("price_") && name.endsWith(".json"));
                
            if (files != null) {
                for (File file : files) {
                    try {
                        String fileName = file.getName();
                        int itemId = Integer.parseInt(
                            fileName.substring(6, fileName.length() - 5));
                        List<PriceData> history = mapper.readValue(file,
                            new TypeReference<List<PriceData>>() {});
                        priceCache.put(itemId, history);
                    } catch (Exception e) {
                        log.error("Failed to load price history from {}", 
                            file.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load price history", e);
        }
        return new HashMap<>(priceCache);
    }
    
    // Watched Items Management
    
    public Set<Integer> getWatchedItems() {
        String json = configManager.getConfiguration(CONFIG_GROUP, WATCHED_ITEMS_KEY);
        if (json == null || json.isEmpty()) {
            return new HashSet<>();
        }
        
        try {
            return mapper.readValue(json, new TypeReference<Set<Integer>>() {});
        } catch (IOException e) {
            log.error("Failed to load watched items", e);
            return new HashSet<>();
        }
    }
    
    public void saveWatchedItems(Set<Integer> items) {
        try {
            String json = mapper.writeValueAsString(items);
            configManager.setConfiguration(CONFIG_GROUP, WATCHED_ITEMS_KEY, json);
        } catch (IOException e) {
            log.error("Failed to save watched items", e);
        }
    }
    
    // Alerts Management
    
    public List<PriceAlert> getAlerts() {
        String json = configManager.getConfiguration(CONFIG_GROUP, ALERTS_KEY);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return mapper.readValue(json, new TypeReference<List<PriceAlert>>() {});
        } catch (IOException e) {
            log.error("Failed to load alerts", e);
            return new ArrayList<>();
        }
    }
    
    public void saveAlerts(List<PriceAlert> alerts) {
        try {
            String json = mapper.writeValueAsString(alerts);
            configManager.setConfiguration(CONFIG_GROUP, ALERTS_KEY, json);
        } catch (IOException e) {
            log.error("Failed to save alerts", e);
        }
    }
    
    // Analysis Management
    
    public void saveAnalysis(int itemId, TechnicalIndicators indicators, MarketSentiment sentiment) {
        try {
            Path filePath = dataPath.resolve("analysis_" + itemId + ".json");
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("itemId", itemId);
            analysis.put("indicators", indicators);
            analysis.put("sentiment", sentiment);
            analysis.put("timestamp", System.currentTimeMillis());
            mapper.writeValue(filePath.toFile(), analysis);
        } catch (IOException e) {
            log.error("Failed to save analysis for item {}", itemId, e);
        }
    }
    
    public Map<String, Object> getAnalysis(int itemId) {
        try {
            Path filePath = dataPath.resolve("analysis_" + itemId + ".json");
            if (Files.exists(filePath)) {
                return mapper.readValue(filePath.toFile(), 
                    new TypeReference<Map<String, Object>>() {});
            }
        } catch (IOException e) {
            log.error("Failed to load analysis for item {}", itemId, e);
        }
        return new HashMap<>();
    }
    
    // Data Cleanup
    
    public void cleanupOldData() {
        // Remove price history older than 30 days
        Instant cutoff = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        
        priceCache.forEach((itemId, history) -> {
            List<PriceData> filtered = history.stream()
                .filter(price -> Instant.ofEpochMilli(price.getHighTimestamp())
                    .isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (filtered.size() < history.size()) {
                priceCache.put(itemId, filtered);
                savePriceHistory(itemId);
            }
        });
    }
}