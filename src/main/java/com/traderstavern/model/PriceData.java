package com.traderstavern.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceData {
    private int itemId;
    private int high;
    private int low;
    
    @JsonProperty("highTime")
    private long highTimestamp;
    
    @JsonProperty("lowTime")
    private long lowTimestamp;
    
    public int getSpread() {
        return high - low;
    }
    
    public double getMargin() {
        return low > 0 ? (high - low) / (double) low : 0;
    }
    
    public double getMarginPercent() {
        return getMargin() * 100;
    }
}