package com.traderstavern.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@Singleton
public class CacheManager {
    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(5);
    
    private final ConcurrentHashMap<String, Cache<Object, Object>> caches;
    private final Executor asyncExecutor;
    
    @Inject
    public CacheManager() {
        this.caches = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "CacheManager-Async");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    public <K, V> Cache<K, V> getCache(String name) {
        return getCache(name, DEFAULT_CACHE_SIZE, DEFAULT_EXPIRY);
    }
    
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name, int size, Duration expiry) {
        return (Cache<K, V>) caches.computeIfAbsent(name, k -> 
            Caffeine.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(expiry)
                .recordStats()
                .build()
        );
    }
    
    public <K, V> V computeAsync(String cacheName, K key, Function<K, V> loader) {
        Cache<K, V> cache = getCache(cacheName);
        V value = cache.getIfPresent(key);
        
        if (value == null) {
            asyncExecutor.execute(() -> {
                try {
                    V newValue = loader.apply(key);
                    if (newValue != null) {
                        cache.put(key, newValue);
                    }
                } catch (Exception e) {
                    log.error("Error loading value for key {} in cache {}", key, cacheName, e);
                }
            });
        }
        
        return value;
    }
    
    public void clearCache(String name) {
        Cache<?, ?> cache = caches.get(name);
        if (cache != null) {
            cache.invalidateAll();
        }
    }
    
    public void clearAllCaches() {
        caches.values().forEach(Cache::invalidateAll);
    }
    
    public void cleanupExpiredEntries() {
        caches.values().forEach(Cache::cleanUp);
    }
}