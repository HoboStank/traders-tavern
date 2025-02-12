package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

@Data
@Builder
public class MACD {
    private static final int FAST_PERIOD = 12;
    private static final int SLOW_PERIOD = 26;
    private static final int SIGNAL_PERIOD = 9;
    
    private final double[] macdLine;
    private final double[] signalLine;
    private final double[] histogram;
    
    public static MACD calculate(List<Double> prices) {
        if (prices.size() < SLOW_PERIOD) {
            throw new IllegalArgumentException("Not enough price data for MACD calculation");
        }
        
        double[] fastEMA = calculateEMA(prices, FAST_PERIOD);
        double[] slowEMA = calculateEMA(prices, SLOW_PERIOD);
        double[] macdLine = subtractArrays(fastEMA, slowEMA);
        double[] signalLine = calculateEMA(macdLine, SIGNAL_PERIOD);
        double[] histogram = subtractArrays(macdLine, signalLine);
        
        return MACD.builder()
            .macdLine(macdLine)
            .signalLine(signalLine)
            .histogram(histogram)
            .build();
    }
    
    private static double[] calculateEMA(List<Double> prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double[] ema = new double[prices.size()];
        
        // Initialize EMA with SMA
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += prices.get(i);
        }
        ema[period - 1] = sum / period;
        
        // Calculate EMA
        for (int i = period; i < prices.size(); i++) {
            ema[i] = (prices.get(i) - ema[i - 1]) * multiplier + ema[i - 1];
        }
        
        return ema;
    }
    
    private static double[] calculateEMA(double[] values, int period) {
        double multiplier = 2.0 / (period + 1);
        double[] ema = new double[values.length];
        
        // Initialize EMA with SMA
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += values[i];
        }
        ema[period - 1] = sum / period;
        
        // Calculate EMA
        for (int i = period; i < values.length; i++) {
            ema[i] = (values[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }
        
        return ema;
    }
    
    private static double[] subtractArrays(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }
    
    public double getValue() {
        if (macdLine.length == 0) return 0;
        return macdLine[macdLine.length - 1];
    }
    
    public double getStrength() {
        if (macdLine.length == 0) return 0;
        
        int last = macdLine.length - 1;
        double currentMACD = macdLine[last];
        double currentSignal = signalLine[last];
        double currentHistogram = histogram[last];
        
        // Calculate trend strength based on histogram
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = Math.max(0, last - 5); i <= last; i++) {
            stats.addValue(histogram[i]);
        }
        
        double histogramTrend = stats.getStandardDeviation();
        double crossoverSignal = currentMACD > currentSignal ? 1 : -1;
        
        // Normalize between 0 and 1
        return (histogramTrend * crossoverSignal + 1) / 2;
    }
}