package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;

@Data
@Builder
public class TrendMetrics {
    private final double slope;
    private final double rSquared;
    private final TrendType type;
    private final double strength;
    
    public enum TrendType {
        STRONG_UPTREND,
        UPTREND,
        SIDEWAYS,
        DOWNTREND,
        STRONG_DOWNTREND
    }
    
    public TrendType getTrend() {
        return type;
    }
    
    public static TrendMetrics calculate(List<Double> prices) {
        if (prices.size() < 2) {
            throw new IllegalArgumentException("Not enough price data for trend analysis");
        }
        
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < prices.size(); i++) {
            regression.addData(i, prices.get(i));
        }
        
        double slope = regression.getSlope();
        double rSquared = regression.getRSquare();
        
        // Determine trend type and strength
        TrendType type;
        double normalizedSlope = slope / prices.get(0) * 100; // Percentage change
        
        if (normalizedSlope > 1.0) {
            type = TrendType.STRONG_UPTREND;
        } else if (normalizedSlope > 0.2) {
            type = TrendType.UPTREND;
        } else if (normalizedSlope < -1.0) {
            type = TrendType.STRONG_DOWNTREND;
        } else if (normalizedSlope < -0.2) {
            type = TrendType.DOWNTREND;
        } else {
            type = TrendType.SIDEWAYS;
        }
        
        // Calculate trend strength (0-1)
        double strength = Math.min(1.0, Math.abs(normalizedSlope) * rSquared);
        
        return TrendMetrics.builder()
            .slope(slope)
            .rSquared(rSquared)
            .type(type)
            .strength(strength)
            .build();
    }
    
    public double getStrength() {
        return strength;
    }
}