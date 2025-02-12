package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

@Data
@Builder
public class RSI {
    private static final int PERIOD = 14;
    private final double value;
    private final double[] history;
    
    public static RSI calculate(List<Double> prices) {
        if (prices.size() < PERIOD + 1) {
            throw new IllegalArgumentException("Not enough price data for RSI calculation");
        }
        
        double[] gains = new double[prices.size()];
        double[] losses = new double[prices.size()];
        double[] rsiValues = new double[prices.size()];
        
        // Calculate price changes and separate gains and losses
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gains[i] = change;
                losses[i] = 0;
            } else {
                gains[i] = 0;
                losses[i] = Math.abs(change);
            }
        }
        
        // Calculate initial averages
        double avgGain = calculateAverage(gains, 1, PERIOD);
        double avgLoss = calculateAverage(losses, 1, PERIOD);
        
        // Calculate first RSI
        rsiValues[PERIOD] = calculateRSI(avgGain, avgLoss);
        
        // Calculate subsequent values using smoothing
        for (int i = PERIOD + 1; i < prices.size(); i++) {
            avgGain = ((avgGain * (PERIOD - 1)) + gains[i]) / PERIOD;
            avgLoss = ((avgLoss * (PERIOD - 1)) + losses[i]) / PERIOD;
            rsiValues[i] = calculateRSI(avgGain, avgLoss);
        }
        
        return RSI.builder()
            .value(rsiValues[rsiValues.length - 1])
            .history(rsiValues)
            .build();
    }
    
    private static double calculateAverage(double[] values, int start, int length) {
        double sum = 0;
        for (int i = start; i < start + length; i++) {
            sum += values[i];
        }
        return sum / length;
    }
    
    private static double calculateRSI(double avgGain, double avgLoss) {
        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }
    
    public double getStrength() {
        // RSI ranges from 0 to 100
        // Below 30 is oversold (good buying opportunity)
        // Above 70 is overbought (good selling opportunity)
        if (value <= 30) {
            return 1.0; // Strong buy signal
        } else if (value >= 70) {
            return 0.0; // Strong sell signal
        } else {
            // Linear interpolation between 30-70
            return 1.0 - ((value - 30) / 40);
        }
    }
}