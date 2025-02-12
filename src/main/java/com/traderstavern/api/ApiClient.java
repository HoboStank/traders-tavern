package com.traderstavern.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@Singleton
public class ApiClient {
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final RateLimiter rateLimiter;

    @Inject
    public ApiClient() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(10))
            .build();
        this.mapper = new ObjectMapper();
        this.rateLimiter = new RateLimiter();
    }

    public <T> T executeRequest(String url, Class<T> type) throws ApiException {
        if (!rateLimiter.allowRequest()) {
            throw new ApiException(ErrorType.RATE_LIMIT, 429);
        }

        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", "TradersTavern")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw ApiException.from(response);
            }
            return mapper.readValue(response.body().string(), type);
        } catch (IOException e) {
            log.error("API request failed", e);
            throw new ApiException(ErrorType.NETWORK_ERROR, 0);
        }
    }
}