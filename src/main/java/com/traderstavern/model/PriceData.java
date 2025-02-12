package com.traderstavern.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceData {
    private final int high;
    private final int low;
    
    @JsonProperty("highTime")
    private final long highTimestamp;
    
    @JsonProperty("lowTime")
    private final long lowTimestamp;
    
    public int getSpread() {
        return high - low;
    }
    
    public double getMargin() {
        return low > 0 ? (high - low) / (double) low : 0;
    }
}