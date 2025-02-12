package com.traderstavern.service;

import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class PriceService {
    private final ItemManager itemManager;
    private final StorageService storageService;

    @Inject
    public PriceService(ItemManager itemManager, StorageService storageService) {
        this.itemManager = itemManager;
        this.storageService = storageService;
    }

    public void updatePrice(GrandExchangeOffer offer) {
        if (offer == null) return;

        PriceData priceData = PriceData.builder()
            .itemId(offer.getItemId())
            .high(offer.getPrice())
            .low(offer.getPrice())
            .highTimestamp(System.currentTimeMillis())
            .lowTimestamp(System.currentTimeMillis())
            .build();

        storageService.savePriceData(offer.getItemId(), priceData);
    }

    public void updatePriceHistory(int itemId, List<PriceData> history) {
        history.forEach(price -> storageService.savePriceData(itemId, price));
    }

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.traderstavern.api.ApiClient;
import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Slf4j
@Singleton
public class PriceService {
    private static final String BASE_URL = 
        "https://prices.runescape.wiki/api/v1/osrs/latest";
    
    private final ApiClient apiClient;
    private final Cache<Integer, PriceData> cache;
    
    @Inject
    public PriceService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .build();
    }
    
    public PriceData getPrice(int itemId) {
        return cache.get(itemId, this::fetchPrice);
    }
    
    private PriceData fetchPrice(int itemId) {
        String url = String.format("%s?id=%d", BASE_URL, itemId);
        return apiClient.executeRequest(url, PriceData.class);
    }
}