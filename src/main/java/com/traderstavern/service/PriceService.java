package com.traderstavern.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.traderstavern.api.ApiClient;
import com.traderstavern.cache.CacheManager;
import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class PriceService {
    private static final String BASE_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final String PRICE_CACHE = "prices";
    private static final String ITEM_CACHE = "items";
    private static final String IMAGE_CACHE = "images";
    
    private final ItemManager itemManager;
    private final StorageService storageService;
    private final ApiClient apiClient;
    private final CacheManager cacheManager;
    
    private final Cache<Integer, PriceData> priceCache;
    private final Cache<Integer, String> itemNameCache;
    private final Cache<Integer, BufferedImage> imageCache;

    @Inject
    public PriceService(
            ItemManager itemManager,
            StorageService storageService,
            ApiClient apiClient,
            CacheManager cacheManager) {
        this.itemManager = itemManager;
        this.storageService = storageService;
        this.apiClient = apiClient;
        this.cacheManager = cacheManager;
        
        this.priceCache = cacheManager.getCache(PRICE_CACHE);
        this.itemNameCache = cacheManager.getCache(ITEM_CACHE);
        this.imageCache = cacheManager.getCache(IMAGE_CACHE);
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
        cacheManager.computeAsync(PRICE_CACHE, offer.getItemId(), id -> {
            storageService.savePriceData(id, priceData);
            return priceData;
        });
    }

    public synchronized void updatePriceHistory(int itemId, List<PriceData> history) {
        List<PriceData> safeHistory = new ArrayList<>(history);
        safeHistory.forEach(price -> {
            priceCache.put(itemId, price);
            cacheManager.computeAsync(PRICE_CACHE, itemId, id -> {
                storageService.savePriceData(id, price);
                return price;
            });
        });
    }
    
    public PriceData getPrice(int itemId) {
        PriceData cached = priceCache.getIfPresent(itemId);
        if (cached != null) {
            return cached;
        }
        
        return cacheManager.computeAsync(PRICE_CACHE, itemId, this::fetchPrice);
    }
    
    private PriceData fetchPrice(int itemId) {
        String url = String.format("%s?id=%d", BASE_URL, itemId);
        return apiClient.executeRequest(url, PriceData.class);
    }
    
    public String getItemName(int itemId) {
        return itemNameCache.get(itemId, key -> 
            itemManager.getItemComposition(key).getName());
    }
    
    public BufferedImage getItemImage(int itemId) {
        return imageCache.get(itemId, itemManager::getImage);
    }
}