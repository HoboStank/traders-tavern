package com.traderstavern.api;

public enum ErrorType {
    RATE_LIMIT(429),
    SERVER_ERROR(500),
    NETWORK_ERROR(0);
    
    private final int code;
    
    ErrorType(int code) {
        this.code = code;
    }
    
    public static ErrorType fromCode(int code) {
        if (code == 429) return RATE_LIMIT;
        if (code >= 500) return SERVER_ERROR;
        return NETWORK_ERROR;
    }
}