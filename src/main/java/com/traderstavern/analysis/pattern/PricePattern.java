package com.traderstavern.analysis.pattern;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PricePattern {
    public enum PatternType {
        DOUBLE_TOP,
        DOUBLE_BOTTOM,
        HEAD_AND_SHOULDERS,
        INVERSE_HEAD_AND_SHOULDERS,
        ASCENDING_TRIANGLE,
        DESCENDING_TRIANGLE,
        SYMMETRICAL_TRIANGLE,
        BULLISH_FLAG,
        BEARISH_FLAG,
        CUP_AND_HANDLE
    }
    
    private final PatternType type;
    private final double reliability;
    private final int startIndex;
    private final int endIndex;
    private final double targetPrice;
    private final double stopLoss;
    
    public static List<PricePattern> findPatterns(List<Double> prices) {
        return List.of(
            findDoubleTop(prices),
            findDoubleBottom(prices),
            findHeadAndShoulders(prices),
            findInverseHeadAndShoulders(prices),
            findTriangles(prices),
            findFlags(prices),
            findCupAndHandle(prices)
        ).stream()
        .filter(p -> p != null && p.getReliability() > 0.6)
        .toList();
    }
    
    private static PricePattern findDoubleTop(List<Double> prices) {
        if (prices.size() < 20) return null;
        
        // Find two peaks with similar heights
        double maxDiff = 0.02; // 2% difference allowed
        int firstPeak = -1;
        int secondPeak = -1;
        
        for (int i = 5; i < prices.size() - 5; i++) {
            if (isLocalPeak(prices, i)) {
                if (firstPeak == -1) {
                    firstPeak = i;
                } else {
                    double diff = Math.abs(prices.get(i) - prices.get(firstPeak)) / prices.get(firstPeak);
                    if (diff <= maxDiff) {
                        secondPeak = i;
                        break;
                    }
                }
            }
        }
        
        if (firstPeak != -1 && secondPeak != -1) {
            double necklinePrice = findNeckline(prices, firstPeak, secondPeak);
            double height = prices.get(firstPeak) - necklinePrice;
            double targetPrice = necklinePrice - height;
            double stopLoss = prices.get(Math.max(firstPeak, secondPeak)) + (height * 0.1);
            
            return PricePattern.builder()
                .type(PatternType.DOUBLE_TOP)
                .reliability(calculateReliability(prices, firstPeak, secondPeak))
                .startIndex(firstPeak)
                .endIndex(secondPeak)
                .targetPrice(targetPrice)
                .stopLoss(stopLoss)
                .build();
        }
        
        return null;
    }
    
    private static PricePattern findDoubleBottom(List<Double> prices) {
        if (prices.size() < 20) return null;
        
        // Similar to double top but looking for valleys
        double maxDiff = 0.02;
        int firstValley = -1;
        int secondValley = -1;
        
        for (int i = 5; i < prices.size() - 5; i++) {
            if (isLocalValley(prices, i)) {
                if (firstValley == -1) {
                    firstValley = i;
                } else {
                    double diff = Math.abs(prices.get(i) - prices.get(firstValley)) / prices.get(firstValley);
                    if (diff <= maxDiff) {
                        secondValley = i;
                        break;
                    }
                }
            }
        }
        
        if (firstValley != -1 && secondValley != -1) {
            double necklinePrice = findNeckline(prices, firstValley, secondValley);
            double height = necklinePrice - prices.get(firstValley);
            double targetPrice = necklinePrice + height;
            double stopLoss = prices.get(Math.min(firstValley, secondValley)) - (height * 0.1);
            
            return PricePattern.builder()
                .type(PatternType.DOUBLE_BOTTOM)
                .reliability(calculateReliability(prices, firstValley, secondValley))
                .startIndex(firstValley)
                .endIndex(secondValley)
                .targetPrice(targetPrice)
                .stopLoss(stopLoss)
                .build();
        }
        
        return null;
    }
    
    private static boolean isLocalPeak(List<Double> prices, int index) {
        if (index < 2 || index >= prices.size() - 2) return false;
        
        double price = prices.get(index);
        return price > prices.get(index - 2) &&
               price > prices.get(index - 1) &&
               price > prices.get(index + 1) &&
               price > prices.get(index + 2);
    }
    
    private static boolean isLocalValley(List<Double> prices, int index) {
        if (index < 2 || index >= prices.size() - 2) return false;
        
        double price = prices.get(index);
        return price < prices.get(index - 2) &&
               price < prices.get(index - 1) &&
               price < prices.get(index + 1) &&
               price < prices.get(index + 2);
    }
    
    private static double findNeckline(List<Double> prices, int start, int end) {
        // Find the lowest high between the two points
        double neckline = Double.MAX_VALUE;
        for (int i = start; i <= end; i++) {
            neckline = Math.min(neckline, prices.get(i));
        }
        return neckline;
    }
    
    private static double calculateReliability(List<Double> prices, int start, int end) {
        // Base reliability on:
        // 1. Pattern symmetry
        // 2. Volume confirmation
        // 3. Price momentum
        
        double symmetryScore = calculateSymmetry(prices, start, end);
        double volumeScore = 0.8; // Placeholder for volume analysis
        double momentumScore = calculateMomentum(prices, end);
        
        return (symmetryScore * 0.4) + (volumeScore * 0.3) + (momentumScore * 0.3);
    }
    
    private static double calculateSymmetry(List<Double> prices, int start, int end) {
        if (end - start < 3) return 0;
        
        double midpoint = (end + start) / 2.0;
        double leftSum = 0;
        double rightSum = 0;
        int count = 0;
        
        for (int i = start; i < midpoint; i++) {
            leftSum += prices.get(i);
            rightSum += prices.get(end - (i - start));
            count++;
        }
        
        double leftAvg = leftSum / count;
        double rightAvg = rightSum / count;
        
        return 1 - Math.min(1, Math.abs(leftAvg - rightAvg) / leftAvg);
    }
    
    private static double calculateMomentum(List<Double> prices, int end) {
        if (end < 5 || end >= prices.size() - 1) return 0;
        
        double momentum = 0;
        for (int i = end - 4; i <= end; i++) {
            momentum += (prices.get(i + 1) - prices.get(i)) / prices.get(i);
        }
        
        return Math.min(1, Math.abs(momentum));
    }
    
    // Other pattern detection methods would be implemented similarly
    private static PricePattern findHeadAndShoulders(List<Double> prices) {
        // TODO: Implement head and shoulders pattern detection
        return null;
    }
    
    private static PricePattern findInverseHeadAndShoulders(List<Double> prices) {
        // TODO: Implement inverse head and shoulders pattern detection
        return null;
    }
    
    private static PricePattern findTriangles(List<Double> prices) {
        // TODO: Implement triangle pattern detection
        return null;
    }
    
    private static PricePattern findFlags(List<Double> prices) {
        // TODO: Implement flag pattern detection
        return null;
    }
    
    private static PricePattern findCupAndHandle(List<Double> prices) {
        // TODO: Implement cup and handle pattern detection
        return null;
    }
}