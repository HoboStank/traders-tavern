package com.traderstavern.api;

import lombok.Getter;
import okhttp3.Response;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorType type;
    private final int code;
    
    public ApiException(ErrorType type, int code) {
        super(type.name());
        this.type = type;
        this.code = code;
    }
    
    public static ApiException from(Response response) {
        int code = response.code();
        ErrorType type = ErrorType.fromCode(code);
        return new ApiException(type, code);
    }
}