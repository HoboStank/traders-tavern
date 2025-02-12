package com.traderstavern.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceAlert {
    public enum AlertType {
        PRICE_ABOVE,
        PRICE_BELOW,
        PRICE_CHANGE_PERCENT,
        VOLUME_ABOVE,
        VOLUME_BELOW
    }
    
    private final int itemId;
    private final AlertType type;
    private final double threshold;
    private final boolean repeatable;
    
    public boolean isTriggered(PriceData price) {
        switch (type) {
            case PRICE_ABOVE:
                return price.getHigh() > threshold;
            case PRICE_BELOW:
                return price.getLow() < threshold;
            case PRICE_CHANGE_PERCENT:
                return Math.abs(price.getMargin()) * 100 > threshold;
            case VOLUME_ABOVE:
                return (price.getHigh() - price.getLow()) > threshold;
            case VOLUME_BELOW:
                return (price.getHigh() - price.getLow()) < threshold;
            default:
                return false;
        }
    }
}