package com.traderstavern.service;

import com.traderstavern.analysis.*;
import com.traderstavern.analysis.pattern.PricePattern;
import com.traderstavern.model.PriceData;
import com.traderstavern.plugin.TradersTavernConfig;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class AnalysisService {
    private final PriceService priceService;
    private final StorageService storageService;
    
    @Inject
    public AnalysisService(PriceService priceService, StorageService storageService) {
        this.priceService = priceService;
        this.storageService = storageService;
    }
    
    public void updateAnalysis(TradersTavernConfig.RiskLevel risk, TradersTavernConfig.TimeFrame timeFrame) {
        log.debug("Updating analysis with risk={}, timeFrame={}", risk, timeFrame);
        
        // Get all monitored items
        Set<Integer> monitoredItems = storageService.getWatchedItems();
        
        // Analyze each item
        for (int itemId : monitoredItems) {
            List<PriceData> history = storageService.getPriceHistory(itemId);
            if (history.isEmpty()) continue;
            
            // Calculate technical indicators
            TechnicalIndicators indicators = analyze(itemId, history);
            if (indicators == null) continue;
            
            // Calculate market sentiment
            MarketSentiment sentiment = analyzeSentiment(itemId, history);
            
            // Store analysis results
            storageService.saveAnalysis(itemId, indicators, sentiment);
        }
    }
    
    public TechnicalIndicators analyze(int itemId, List<PriceData> priceHistory) {
        List<Double> prices = priceHistory.stream()
            .map(p -> (double) p.getHigh())
            .collect(Collectors.toList());
            
        List<Integer> volumes = priceHistory.stream()
            .map(p -> Math.abs(p.getHigh() - p.getLow()))
            .collect(Collectors.toList());
        
        try {
            MACD macd = MACD.calculate(prices);
            RSI rsi = RSI.calculate(prices);
            ROC roc = ROC.calculate(prices);
            VolumeMetrics volumeMetrics = VolumeMetrics.calculate(volumes);
            TrendMetrics trendMetrics = TrendMetrics.calculate(prices);
            
            return TechnicalIndicators.builder()
                .macd(macd)
                .rsi(rsi)
                .roc(roc)
                .volumeMetrics(volumeMetrics)
                .trendMetrics(trendMetrics)
                .build();
                
        } catch (Exception e) {
            log.error("Error calculating technical indicators for item {}", itemId, e);
            return null;
        }
    }
    
    public MultiTimeFrameAnalysis analyzeMultiTimeframe(int itemId) {
        Map<TimeFrame, List<Double>> timeFramePrices = new EnumMap<>(TimeFrame.class);
        
        // Get all price history
        List<PriceData> history = storageService.getPriceHistory(itemId);
        if (history.isEmpty()) return null;
        
        // Sort by timestamp
        history.sort(Comparator.comparingLong(PriceData::getHighTimestamp));
        
        // Calculate prices for each timeframe
        for (TimeFrame tf : TimeFrame.values()) {
            List<Double> prices = aggregatePrices(history, tf);
            if (!prices.isEmpty()) {
                timeFramePrices.put(tf, prices);
            }
        }
        
        return MultiTimeFrameAnalysis.analyze(timeFramePrices);
    }
    
    public MarketSentiment analyzeSentiment(int itemId, List<PriceData> priceHistory) {
        List<Double> prices = priceHistory.stream()
            .map(p -> (double) p.getHigh())
            .collect(Collectors.toList());
            
        List<Integer> volumes = priceHistory.stream()
            .map(p -> Math.abs(p.getHigh() - p.getLow()))
            .collect(Collectors.toList());
            
        List<PricePattern> patterns = PricePattern.findPatterns(prices);
        
        return MarketSentiment.analyze(prices, volumes, patterns);
    }
    
    private List<Double> aggregatePrices(List<PriceData> history, TimeFrame timeFrame) {
        if (history.isEmpty()) return List.of();
        
        List<Double> aggregated = new ArrayList<>();
        long interval = timeFrame.getSeconds() * 1000L; // Convert to milliseconds
        long currentTime = history.get(0).getHighTimestamp();
        List<Double> currentPrices = new ArrayList<>();
        
        for (PriceData price : history) {
            if (price.getHighTimestamp() - currentTime > interval) {
                if (!currentPrices.isEmpty()) {
                    // Calculate OHLC or average for the interval
                    aggregated.add(calculateAveragePrice(currentPrices));
                    currentPrices.clear();
                }
                currentTime = price.getHighTimestamp();
            }
            currentPrices.add((double) price.getHigh());
        }
        
        // Add the last interval if not empty
        if (!currentPrices.isEmpty()) {
            aggregated.add(calculateAveragePrice(currentPrices));
        }
        
        return aggregated;
    }
    
    private double calculateAveragePrice(List<Double> prices) {
        return prices.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
}