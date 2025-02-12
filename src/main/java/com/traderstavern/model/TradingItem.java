package com.traderstavern.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class TradingItem {
    private final int itemId;
    private final String name;
    private final long buyPrice;
    private final long sellPrice;
    private final int volume;
    private final double margin;
    private final double roi;
    private final Instant lastUpdate;
    private final double riskScore;
    private final double confidenceScore;
}