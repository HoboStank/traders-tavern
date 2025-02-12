package com.traderstavern.analysis;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

@Data
@Builder
public class VolumeMetrics {
    private final double volumeMA;
    private final double volumeStdDev;
    private final boolean isHighVolume;
    private final double[] volumeHistory;
    
    public static VolumeMetrics calculate(List<Integer> volumes) {
        if (volumes.isEmpty()) {
            throw new IllegalArgumentException("No volume data provided");
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        volumes.forEach(stats::addValue);
        
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        double currentVolume = volumes.get(volumes.size() - 1);
        boolean isHigh = currentVolume > (mean + stdDev);
        
        double[] history = volumes.stream()
            .mapToDouble(Integer::doubleValue)
            .toArray();
        
        return VolumeMetrics.builder()
            .volumeMA(mean)
            .volumeStdDev(stdDev)
            .isHighVolume(isHigh)
            .volumeHistory(history)
            .build();
    }
    
    public double getCurrentVolume() {
        if (volumeHistory.length == 0) return 0;
        return volumeHistory[volumeHistory.length - 1];
    }
    
    public double getStrength() {
        if (volumeHistory.length == 0) return 0;
        
        double currentVolume = volumeHistory[volumeHistory.length - 1];
        double normalizedVolume = (currentVolume - volumeMA) / volumeStdDev;
        
        // Convert to a 0-1 scale using sigmoid function
        return 1 / (1 + Math.exp(-normalizedVolume));
    }
}