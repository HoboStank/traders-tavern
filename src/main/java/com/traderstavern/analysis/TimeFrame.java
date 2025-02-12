package com.traderstavern.analysis;

import lombok.Getter;

@Getter
public enum TimeFrame {
    M1("1m", 60),
    M5("5m", 300),
    M15("15m", 900),
    M30("30m", 1800),
    H1("1h", 3600),
    H4("4h", 14400),
    D1("1d", 86400),
    W1("1w", 604800);
    
    private final String label;
    private final int seconds;
    
    TimeFrame(String label, int seconds) {
        this.label = label;
        this.seconds = seconds;
    }
    
    public static TimeFrame fromLabel(String label) {
        for (TimeFrame tf : values()) {
            if (tf.label.equals(label)) {
                return tf;
            }
        }
        throw new IllegalArgumentException("Invalid timeframe: " + label);
    }
}