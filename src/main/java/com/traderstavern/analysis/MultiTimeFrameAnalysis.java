package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MultiTimeFrameAnalysis {
    private final Map<TimeFrame, TechnicalIndicators> timeFrameAnalysis;
    private final double overallStrength;
    private final TrendAlignment trendAlignment;
    
    @Data
    @Builder
    public static class TrendAlignment {
        private final boolean aligned;
        private final double strength;
        private final TrendMetrics.TrendType dominantTrend;
        
        public static TrendAlignment calculate(Map<TimeFrame, TechnicalIndicators> analysis) {
            if (analysis.isEmpty()) {
                return TrendAlignment.builder()
                    .aligned(false)
                    .strength(0)
                    .dominantTrend(TrendMetrics.TrendType.SIDEWAYS)
                    .build();
            }
            
            // Count trend types across timeframes
            Map<TrendMetrics.TrendType, Integer> trendCounts = new EnumMap<>(TrendMetrics.TrendType.class);
            Map<TrendMetrics.TrendType, Double> trendStrengths = new EnumMap<>(TrendMetrics.TrendType.class);
            
            analysis.forEach((tf, indicators) -> {
                TrendMetrics.TrendType trend = indicators.getTrendMetrics().getType();
                trendCounts.merge(trend, 1, Integer::sum);
                trendStrengths.merge(trend, indicators.getTrendMetrics().getStrength(), Double::sum);
            });
            
            // Find dominant trend
            TrendMetrics.TrendType dominantTrend = trendCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TrendMetrics.TrendType.SIDEWAYS);
            
            // Calculate alignment strength
            int maxCount = trendCounts.getOrDefault(dominantTrend, 0);
            double alignmentStrength = (double) maxCount / analysis.size();
            
            // Calculate average strength for dominant trend
            double avgStrength = trendStrengths.getOrDefault(dominantTrend, 0.0) / maxCount;
            
            return TrendAlignment.builder()
                .aligned(alignmentStrength > 0.6) // More than 60% alignment
                .strength(alignmentStrength * avgStrength)
                .dominantTrend(dominantTrend)
                .build();
        }
    }
    
    public static MultiTimeFrameAnalysis analyze(Map<TimeFrame, List<Double>> timeFramePrices) {
        Map<TimeFrame, TechnicalIndicators> analysis = new EnumMap<>(TimeFrame.class);
        
        // Analyze each timeframe
        timeFramePrices.forEach((tf, prices) -> {
            try {
                MACD macd = MACD.calculate(prices);
                RSI rsi = RSI.calculate(prices);
                ROC roc = ROC.calculate(prices);
                List<Integer> volumes = prices.stream()
                    .map(p -> (int) Math.round(p))
                    .toList();
                VolumeMetrics volumeMetrics = VolumeMetrics.calculate(volumes);
                TrendMetrics trendMetrics = TrendMetrics.calculate(prices);
                
                TechnicalIndicators indicators = TechnicalIndicators.builder()
                    .macd(macd)
                    .rsi(rsi)
                    .roc(roc)
                    .volumeMetrics(volumeMetrics)
                    .trendMetrics(trendMetrics)
                    .build();
                    
                analysis.put(tf, indicators);
            } catch (Exception e) {
                // Skip timeframe if analysis fails
            }
        });
        
        // Calculate trend alignment
        TrendAlignment trendAlignment = TrendAlignment.calculate(analysis);
        
        // Calculate overall strength
        double overallStrength = calculateOverallStrength(analysis, trendAlignment);
        
        return MultiTimeFrameAnalysis.builder()
            .timeFrameAnalysis(analysis)
            .overallStrength(overallStrength)
            .trendAlignment(trendAlignment)
            .build();
    }
    
    private static double calculateOverallStrength(
            Map<TimeFrame, TechnicalIndicators> analysis,
            TrendAlignment trendAlignment) {
        if (analysis.isEmpty()) return 0;
        
        // Weight different timeframes
        double totalWeight = 0;
        double weightedStrength = 0;
        
        for (Map.Entry<TimeFrame, TechnicalIndicators> entry : analysis.entrySet()) {
            double weight = getTimeFrameWeight(entry.getKey());
            totalWeight += weight;
            weightedStrength += weight * entry.getValue().getOverallConfidence();
        }
        
        double baseStrength = weightedStrength / totalWeight;
        
        // Adjust based on trend alignment
        return baseStrength * (0.7 + (0.3 * trendAlignment.getStrength()));
    }
    
    private static double getTimeFrameWeight(TimeFrame tf) {
        return switch (tf) {
            case M1 -> 0.05;
            case M5 -> 0.1;
            case M15 -> 0.15;
            case M30 -> 0.15;
            case H1 -> 0.2;
            case H4 -> 0.15;
            case D1 -> 0.1;
            case W1 -> 0.1;
        };
    }
}