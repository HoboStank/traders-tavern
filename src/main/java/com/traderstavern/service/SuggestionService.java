package com.traderstavern.service;

import com.traderstavern.analysis.TechnicalIndicators;
import com.traderstavern.model.PriceAlert;
import com.traderstavern.model.PriceData;
import com.traderstavern.model.TradingSuggestion;
import com.traderstavern.plugin.TradersTavernConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class SuggestionService implements PriceMonitorService.PriceUpdateListener {
    private final PriceMonitorService monitorService;
    private final AnalysisService analysisService;
    private final ItemManager itemManager;
    private final TradersTavernConfig config;
    private final Map<Integer, TradingSuggestion> currentSuggestions;
    private final List<SuggestionListener> listeners;
    
    @Inject
    public SuggestionService(
            PriceMonitorService monitorService,
            AnalysisService analysisService,
            ItemManager itemManager,
            TradersTavernConfig config) {
        this.monitorService = monitorService;
        this.analysisService = analysisService;
        this.itemManager = itemManager;
        this.config = config;
        this.currentSuggestions = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        
        monitorService.addListener(this);
    }
    
    @Override
    public void onPriceUpdate(int itemId, PriceData price) {
        try {
            updateSuggestion(itemId, price);
        } catch (Exception e) {
            log.error("Error updating suggestion for item {}", itemId, e);
        }
    }
    
    @Override
    public void onAlertTriggered(PriceAlert alert, PriceData price) {
        // Alerts might trigger immediate suggestion updates
        updateSuggestion(alert.getItemId(), price);
    }
    
    private void updateSuggestion(int itemId, PriceData price) {
        List<PriceData> history = monitorService.getPriceHistory(itemId);
        if (history == null || history.isEmpty()) return;
        
        TechnicalIndicators indicators = analysisService.analyze(itemId, history);
        if (indicators == null) return;
        
        ItemComposition item = itemManager.getItemComposition(itemId);
        TradingSuggestion suggestion = TradingSuggestion.fromAnalysis(
            itemId,
            item.getName(),
            price.getLow(),
            price.getHigh(),
            indicators.getOverallConfidence(),
            indicators.getTrendMetrics().getType()
        );
        
        // Only notify if the suggestion has changed significantly
        TradingSuggestion oldSuggestion = currentSuggestions.get(itemId);
        if (hasSignificantChange(oldSuggestion, suggestion)) {
            currentSuggestions.put(itemId, suggestion);
            notifyListeners(suggestion);
        }
    }
    
    private boolean hasSignificantChange(TradingSuggestion old, TradingSuggestion current) {
        if (old == null) return true;
        
        // Check if action has changed
        if (old.getAction() != current.getAction()) return true;
        
        // Check if confidence has changed significantly (>10%)
        if (Math.abs(old.getConfidence() - current.getConfidence()) > 0.1) return true;
        
        // Check if potential profit has changed significantly (>5%)
        return Math.abs(old.getPotentialProfit() - current.getPotentialProfit()) > 5.0;
    }
    
    public void addListener(SuggestionListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(SuggestionListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(TradingSuggestion suggestion) {
        for (SuggestionListener listener : listeners) {
            try {
                listener.onSuggestionUpdate(suggestion);
            } catch (Exception e) {
                log.error("Error notifying suggestion listener", e);
            }
        }
    }
    
    public interface SuggestionListener {
        void onSuggestionUpdate(TradingSuggestion suggestion);
    }
}