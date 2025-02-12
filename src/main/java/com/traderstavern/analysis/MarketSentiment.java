package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import com.traderstavern.analysis.pattern.PricePattern;

import java.util.List;

@Data
@Builder
public class MarketSentiment {
    public enum SentimentLevel {
        VERY_BEARISH,
        BEARISH,
        NEUTRAL,
        BULLISH,
        VERY_BULLISH
    }
    
    private final SentimentLevel level;
    private final double strength;
    private final double volatility;
    private final double momentum;
    private final double volumeProfile;
    private final List<PricePattern> patterns;
    
    public static MarketSentiment analyze(
            List<Double> prices,
            List<Integer> volumes,
            List<PricePattern> patterns) {
        if (prices.size() < 10) {
            return defaultSentiment();
        }
        
        double volatility = calculateVolatility(prices);
        double momentum = calculateMomentum(prices);
        double volumeProfile = calculateVolumeProfile(volumes);
        
        // Calculate overall sentiment
        double sentimentScore = calculateSentimentScore(
            volatility, momentum, volumeProfile, patterns);
            
        return MarketSentiment.builder()
            .level(getSentimentLevel(sentimentScore))
            .strength(Math.abs(sentimentScore))
            .volatility(volatility)
            .momentum(momentum)
            .volumeProfile(volumeProfile)
            .patterns(patterns)
            .build();
    }
    
    private static MarketSentiment defaultSentiment() {
        return MarketSentiment.builder()
            .level(SentimentLevel.NEUTRAL)
            .strength(0)
            .volatility(0)
            .momentum(0)
            .volumeProfile(0)
            .patterns(List.of())
            .build();
    }
    
    private static double calculateVolatility(List<Double> prices) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 1; i < prices.size(); i++) {
            double returns = Math.log(prices.get(i) / prices.get(i - 1));
            stats.addValue(returns);
        }
        
        return stats.getStandardDeviation() * Math.sqrt(252); // Annualized
    }
    
    private static double calculateMomentum(List<Double> prices) {
        if (prices.size() < 2) return 0;
        
        // Calculate short-term and long-term momentum
        double shortTerm = calculatePeriodReturn(prices, 5);
        double mediumTerm = calculatePeriodReturn(prices, 10);
        double longTerm = calculatePeriodReturn(prices, 20);
        
        // Weight the different periods
        return (shortTerm * 0.5) + (mediumTerm * 0.3) + (longTerm * 0.2);
    }
    
    private static double calculatePeriodReturn(List<Double> prices, int period) {
        int start = Math.max(0, prices.size() - period);
        double startPrice = prices.get(start);
        double endPrice = prices.get(prices.size() - 1);
        
        return (endPrice - startPrice) / startPrice;
    }
    
    private static double calculateVolumeProfile(List<Integer> volumes) {
        if (volumes.size() < 5) return 0;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        volumes.forEach(stats::addValue);
        
        double mean = stats.getMean();
        double recent = volumes.get(volumes.size() - 1);
        
        return (recent - mean) / mean;
    }
    
    private static double calculateSentimentScore(
            double volatility,
            double momentum,
            double volumeProfile,
            List<PricePattern> patterns) {
        // Base score on momentum
        double score = momentum;
        
        // Adjust for volatility (high volatility reduces confidence)
        score *= Math.max(0, 1 - (volatility * 2));
        
        // Adjust for volume profile
        score *= (1 + (volumeProfile * 0.5));
        
        // Adjust for patterns
        double patternAdjustment = patterns.stream()
            .mapToDouble(p -> p.getReliability() * getPatternSentiment(p.getType()))
            .average()
            .orElse(0);
            
        score += patternAdjustment * 0.3;
        
        return Math.max(-1, Math.min(1, score));
    }
    
    private static SentimentLevel getSentimentLevel(double score) {
        if (score < -0.6) return SentimentLevel.VERY_BEARISH;
        if (score < -0.2) return SentimentLevel.BEARISH;
        if (score < 0.2) return SentimentLevel.NEUTRAL;
        if (score < 0.6) return SentimentLevel.BULLISH;
        return SentimentLevel.VERY_BULLISH;
    }
    
    private static double getPatternSentiment(PricePattern.PatternType type) {
        return switch (type) {
            case DOUBLE_TOP, HEAD_AND_SHOULDERS, DESCENDING_TRIANGLE, BEARISH_FLAG -> -1.0;
            case DOUBLE_BOTTOM, INVERSE_HEAD_AND_SHOULDERS, ASCENDING_TRIANGLE, BULLISH_FLAG -> 1.0;
            case SYMMETRICAL_TRIANGLE -> 0.0;
            case CUP_AND_HANDLE -> 0.8;
        };
    }
}