package com.traderstavern.ui;

import com.traderstavern.model.TradingSuggestion;
import com.traderstavern.service.SuggestionService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SuggestionPanel extends JPanel implements SuggestionService.SuggestionListener {
    private static final int MAX_SUGGESTIONS = 5;
    private final Map<Integer, JPanel> suggestionPanels;
    private final JPanel suggestionsContainer;
    
    public SuggestionPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Title
        JLabel title = new JLabel("Trading Suggestions");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // Suggestions container
        suggestionsContainer = new JPanel();
        suggestionsContainer.setLayout(new BoxLayout(suggestionsContainer, BoxLayout.Y_AXIS));
        suggestionsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        suggestionPanels = new HashMap<>();
        
        add(title, BorderLayout.NORTH);
        add(suggestionsContainer, BorderLayout.CENTER);
    }
    
    @Override
    public void onSuggestionUpdate(TradingSuggestion suggestion) {
        SwingUtilities.invokeLater(() -> updateSuggestion(suggestion));
    }
    
    private void updateSuggestion(TradingSuggestion suggestion) {
        JPanel panel = suggestionPanels.computeIfAbsent(suggestion.getItemId(), 
            k -> createSuggestionPanel());
            
        updateSuggestionPanel(panel, suggestion);
        
        // Ensure panel is in the container
        if (!suggestionsContainer.isAncestorOf(panel)) {
            if (suggestionPanels.size() > MAX_SUGGESTIONS) {
                // Remove oldest suggestion
                suggestionsContainer.remove(0);
                suggestionPanels.remove(
                    suggestionPanels.keySet().iterator().next()
                );
            }
            suggestionsContainer.add(panel);
        }
        
        revalidate();
        repaint();
    }
    
    private JPanel createSuggestionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        panel.setBorder(BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR));
        return panel;
    }
    
    private void updateSuggestionPanel(JPanel panel, TradingSuggestion suggestion) {
        panel.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        
        // Item name
        JLabel nameLabel = new JLabel(suggestion.getItemName());
        nameLabel.setForeground(Color.WHITE);
        panel.add(nameLabel, c);
        
        // Action
        c.gridy++;
        JLabel actionLabel = new JLabel(suggestion.getAction().name());
        actionLabel.setForeground(getActionColor(suggestion.getAction()));
        panel.add(actionLabel, c);
        
        // Confidence
        c.gridy++;
        JLabel confidenceLabel = new JLabel(String.format("Confidence: %.1f%%", 
            suggestion.getConfidence() * 100));
        confidenceLabel.setForeground(Color.LIGHT_GRAY);
        panel.add(confidenceLabel, c);
        
        // Potential profit
        c.gridy++;
        JLabel profitLabel = new JLabel(String.format("Potential Profit: %.1f%%", 
            suggestion.getPotentialProfit()));
        profitLabel.setForeground(getProfitColor(suggestion.getPotentialProfit()));
        panel.add(profitLabel, c);
        
        // Reasoning
        c.gridy++;
        JTextArea reasoningArea = new JTextArea(suggestion.getReasoning());
        reasoningArea.setLineWrap(true);
        reasoningArea.setWrapStyleWord(true);
        reasoningArea.setEditable(false);
        reasoningArea.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        reasoningArea.setForeground(Color.LIGHT_GRAY);
        panel.add(reasoningArea, c);
    }
    
    private Color getActionColor(TradingSuggestion.Action action) {
        switch (action) {
            case BUY:
                return new Color(0, 200, 83);
            case SELL:
                return new Color(200, 83, 0);
            default:
                return Color.YELLOW;
        }
    }
    
    private Color getProfitColor(double profit) {
        if (profit > 5) return new Color(0, 200, 83);
        if (profit > 0) return new Color(200, 200, 83);
        return new Color(200, 83, 0);
    }
}