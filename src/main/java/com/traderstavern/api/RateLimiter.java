package com.traderstavern.api;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class RateLimiter {
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final Queue<Instant> requests = new LinkedList<>();

    public synchronized boolean allowRequest() {
        Instant now = Instant.now();
        while (!requests.isEmpty() && 
               Duration.between(requests.peek(), now)
                   .compareTo(WINDOW) > 0) {
            requests.poll();
        }
        
        if (requests.size() < MAX_REQUESTS) {
            requests.offer(now);
            return true;
        }
        return false;
    }
}