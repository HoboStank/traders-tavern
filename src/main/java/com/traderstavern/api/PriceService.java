package com.traderstavern.api;

import com.traderstavern.model.TradingItem;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PriceService {
    CompletableFuture<TradingItem> getItemPrice(int itemId);
    CompletableFuture<List<TradingItem>> getTopFlips(int limit);
    CompletableFuture<List<TradingItem>> searchItems(String query);
    CompletableFuture<List<Integer>> getHistoricalPrices(int itemId, int days);
}