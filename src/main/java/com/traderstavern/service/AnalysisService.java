package com.traderstavern.service;

import com.traderstavern.analysis.*;
import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class AnalysisService {
    private final PriceService priceService;
    
    @Inject
    public AnalysisService(PriceService priceService) {
        this.priceService = priceService;
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
}