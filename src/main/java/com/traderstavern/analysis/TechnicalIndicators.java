package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnicalIndicators {
    private final MACD macd;
    private final RSI rsi;
    private final ROC roc;
    private final VolumeMetrics volumeMetrics;
    private final TrendMetrics trendMetrics;
    
    public double getSignalStrength() {
        return (macd.getStrength() * 0.4) +
               (rsi.getStrength() * 0.3) +
               (roc.getStrength() * 0.3);
    }
    
    public double getOverallConfidence() {
        return (getSignalStrength() * 0.6) +
               (volumeMetrics.getStrength() * 0.2) +
               (trendMetrics.getStrength() * 0.2);
    }
}