package com.traderstavern.model;

import com.traderstavern.analysis.TrendMetrics;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradingSuggestion {
    public enum Action {
        BUY,
        SELL,
        HOLD
    }
    
    private final int itemId;
    private final String itemName;
    private final Action action;
    private final double confidence;
    private final double potentialProfit;
    private final double risk;
    private final TrendMetrics.TrendType trend;
    private final String reasoning;
    
    public static TradingSuggestion fromAnalysis(
            int itemId,
            String itemName,
            double buyPrice,
            double sellPrice,
            double confidence,
            TrendMetrics.TrendType trend) {
            
        Action action = determineAction(confidence, trend);
        double potentialProfit = calculatePotentialProfit(buyPrice, sellPrice);
        double risk = 1.0 - confidence;
        
        String reasoning = buildReasoning(action, confidence, trend, potentialProfit);
        
        return TradingSuggestion.builder()
            .itemId(itemId)
            .itemName(itemName)
            .action(action)
            .confidence(confidence)
            .potentialProfit(potentialProfit)
            .risk(risk)
            .trend(trend)
            .reasoning(reasoning)
            .build();
    }
    
    private static Action determineAction(double confidence, TrendMetrics.TrendType trend) {
        if (confidence < 0.4) return Action.HOLD;
        
        switch (trend) {
            case STRONG_UPTREND:
            case UPTREND:
                return Action.BUY;
            case STRONG_DOWNTREND:
            case DOWNTREND:
                return Action.SELL;
            default:
                return confidence > 0.7 ? Action.BUY : Action.HOLD;
        }
    }
    
    private static double calculatePotentialProfit(double buyPrice, double sellPrice) {
        return ((sellPrice - buyPrice) / buyPrice) * 100;
    }
    
    private static String buildReasoning(
            Action action,
            double confidence,
            TrendMetrics.TrendType trend,
            double potentialProfit) {
            
        StringBuilder sb = new StringBuilder();
        
        sb.append(action.name()).append(" recommendation with ")
          .append(String.format("%.1f%%", confidence * 100))
          .append(" confidence. ");
          
        sb.append("Market is showing a ").append(trend.name().toLowerCase())
          .append(". ");
          
        if (potentialProfit > 0) {
            sb.append("Potential profit: ")
              .append(String.format("%.1f%%", potentialProfit));
        }
        
        return sb.toString();
    }
}