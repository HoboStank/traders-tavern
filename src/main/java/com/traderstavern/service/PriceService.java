package com.traderstavern.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.traderstavern.api.ApiClient;
import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;

@Slf4j
@Singleton
public class PriceService {
    private static final String BASE_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    
    private final ItemManager itemManager;
    private final StorageService storageService;
    private final Cache<Integer, PriceData> priceCache;
    private final ApiClient apiClient;

    @Inject
    public PriceService(ItemManager itemManager, StorageService storageService, ApiClient apiClient) {
        this.itemManager = itemManager;
        this.storageService = storageService;
        this.apiClient = apiClient;
        
        this.priceCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .build();
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

        priceCache.put(offer.getItemId(), priceData);
        storageService.savePriceData(offer.getItemId(), priceData);
    }

    public void updatePriceHistory(int itemId, List<PriceData> history) {
        history.forEach(price -> {
            priceCache.put(itemId, price);
            storageService.savePriceData(itemId, price);
        });
    }
    
    public PriceData getPrice(int itemId) {
        return priceCache.get(itemId, this::fetchPrice);
    }
    
    private PriceData fetchPrice(int itemId) {
        String url = String.format("%s?id=%d", BASE_URL, itemId);
        return apiClient.executeRequest(url, PriceData.class);
    }
}