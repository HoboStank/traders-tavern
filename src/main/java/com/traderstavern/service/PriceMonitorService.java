package com.traderstavern.service;

import com.traderstavern.analysis.TechnicalIndicators;
import com.traderstavern.model.PriceAlert;
import com.traderstavern.model.PriceData;
import com.traderstavern.model.TradingSuggestion;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.events.GrandExchangeOfferChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Singleton
public class PriceMonitorService {
    private static final Duration UPDATE_INTERVAL = Duration.ofMinutes(5);
    private static final int MAX_TRACKED_ITEMS = 100;
    
    private final PriceService priceService;
    private final AnalysisService analysisService;
    private final Map<Integer, PriceData> lastPrices;
    private final Map<Integer, List<PriceData>> priceHistory;
    private final Map<Integer, List<PriceAlert>> priceAlerts;
    private final ScheduledExecutorService scheduler;
    private final List<PriceUpdateListener> listeners;
    
    @Inject
    public PriceMonitorService(PriceService priceService, AnalysisService analysisService) {
        this.priceService = priceService;
        this.analysisService = analysisService;
        this.lastPrices = new ConcurrentHashMap<>();
        this.priceHistory = new ConcurrentHashMap<>();
        this.priceAlerts = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.listeners = new CopyOnWriteArrayList<>();
        
        startMonitoring();
    }
    
    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(
            this::updatePrices,
            0,
            UPDATE_INTERVAL.toMinutes(),
            TimeUnit.MINUTES
        );
    }
    
    public void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void updatePrices() {
        Set<Integer> itemIds = new HashSet<>(lastPrices.keySet());
        for (int itemId : itemIds) {
            try {
                PriceData newPrice = priceService.getPrice(itemId);
                PriceData oldPrice = lastPrices.get(itemId);
                
                if (hasSignificantChange(oldPrice, newPrice)) {
                    updatePriceHistory(itemId, newPrice);
                    notifyListeners(itemId, newPrice);
                    checkAlerts(itemId, newPrice);
                }
                
                lastPrices.put(itemId, newPrice);
            } catch (Exception e) {
                log.error("Error updating price for item {}", itemId, e);
            }
        }
    }
    
    private boolean hasSignificantChange(PriceData oldPrice, PriceData newPrice) {
        if (oldPrice == null) return true;
        
        double changePercent = Math.abs(
            (newPrice.getHigh() - oldPrice.getHigh()) / (double) oldPrice.getHigh()
        ) * 100;
        
        return changePercent >= 1.0; // 1% change threshold
    }
    
    private void updatePriceHistory(int itemId, PriceData price) {
        priceHistory.computeIfAbsent(itemId, k -> new ArrayList<>())
            .add(price);
            
        // Keep only last 100 price points
        List<PriceData> history = priceHistory.get(itemId);
        if (history.size() > 100) {
            history.remove(0);
        }
    }
    
    public void trackItem(int itemId) {
        if (lastPrices.size() >= MAX_TRACKED_ITEMS) {
            log.warn("Maximum number of tracked items reached");
            return;
        }
        
        try {
            PriceData price = priceService.getPrice(itemId);
            lastPrices.put(itemId, price);
            updatePriceHistory(itemId, price);
        } catch (Exception e) {
            log.error("Error tracking item {}", itemId, e);
        }
    }
    
    public void untrackItem(int itemId) {
        lastPrices.remove(itemId);
        priceHistory.remove(itemId);
        priceAlerts.remove(itemId);
    }
    
    public void addAlert(PriceAlert alert) {
        priceAlerts.computeIfAbsent(alert.getItemId(), k -> new ArrayList<>())
            .add(alert);
    }
    
    public void removeAlert(PriceAlert alert) {
        List<PriceAlert> alerts = priceAlerts.get(alert.getItemId());
        if (alerts != null) {
            alerts.remove(alert);
        }
    }
    
    private void checkAlerts(int itemId, PriceData price) {
        List<PriceAlert> alerts = priceAlerts.get(itemId);
        if (alerts == null) return;
        
        Iterator<PriceAlert> iterator = alerts.iterator();
        while (iterator.hasNext()) {
            PriceAlert alert = iterator.next();
            if (alert.isTriggered(price)) {
                notifyAlertListeners(alert, price);
                if (!alert.isRepeatable()) {
                    iterator.remove();
                }
            }
        }
    }
    
    public void addListener(PriceUpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(PriceUpdateListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(int itemId, PriceData price) {
        for (PriceUpdateListener listener : listeners) {
            try {
                listener.onPriceUpdate(itemId, price);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }
    
    private void notifyAlertListeners(PriceAlert alert, PriceData price) {
        for (PriceUpdateListener listener : listeners) {
            try {
                listener.onAlertTriggered(alert, price);
            } catch (Exception e) {
                log.error("Error notifying alert listener", e);
            }
        }
    }
    
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
        GrandExchangeOffer offer = event.getOffer();
        if (offer.getItemId() > 0) {
            trackItem(offer.getItemId());
        }
    }
    
    public List<PriceData> getPriceHistory(int itemId) {
        return priceHistory.get(itemId);
    }
    
    public void updateAllItems() {
        updatePrices();
    }
    
    public interface PriceUpdateListener {
        void onPriceUpdate(int itemId, PriceData price);
        void onAlertTriggered(PriceAlert alert, PriceData price);
    }
}