package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

@Data
@Builder
public class ROC {
    private static final int DEFAULT_PERIOD = 12;
    
    private final double value;
    private final double[] history;
    private final int period;
    
    public static ROC calculate(List<Double> prices) {
        return calculate(prices, DEFAULT_PERIOD);
    }
    
    public static ROC calculate(List<Double> prices, int period) {
        if (prices.size() < period + 1) {
            throw new IllegalArgumentException("Not enough price data for ROC calculation");
        }
        
        double[] rocValues = new double[prices.size()];
        
        // Calculate ROC values
        for (int i = period; i < prices.size(); i++) {
            double currentPrice = prices.get(i);
            double oldPrice = prices.get(i - period);
            rocValues[i] = ((currentPrice - oldPrice) / oldPrice) * 100;
        }
        
        return ROC.builder()
            .value(rocValues[rocValues.length - 1])
            .history(rocValues)
            .period(period)
            .build();
    }
    
    public double getStrength() {
        // ROC interpretation:
        // Positive values indicate upward momentum
        // Negative values indicate downward momentum
        // The larger the absolute value, the stronger the momentum
        
        // Calculate standard deviation of recent ROC values to normalize
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = Math.max(0, history.length - period); i < history.length; i++) {
            stats.addValue(history[i]);
        }
        
        double stdDev = stats.getStandardDeviation();
        if (stdDev == 0) return 0.5; // No momentum
        
        // Normalize ROC value using standard deviation
        double normalizedROC = value / (2 * stdDev);
        
        // Convert to a 0-1 scale
        return 1 / (1 + Math.exp(-normalizedROC));
    }
}