package com.traderstavern.ui;

import com.traderstavern.plugin.TradersTavernConfig;
import com.traderstavern.service.PriceMonitorService;
import com.traderstavern.service.SuggestionService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Slf4j
public class TradersTavernPanel extends PluginPanel {
    private final TradersTavernConfig config;
    private final PriceMonitorService monitorService;
    private final SuggestionService suggestionService;
    
    private final JPanel contentPanel = new JPanel();
    private final JComboBox<TradersTavernConfig.RiskLevel> riskSelector;
    private final JComboBox<TradersTavernConfig.TimeFrame> timeFrameSelector;
    private final SuggestionPanel suggestionPanel;
    
    @Inject
    public TradersTavernPanel(
            TradersTavernConfig config,
            PriceMonitorService monitorService,
            SuggestionService suggestionService) {
        super(false);
        this.config = config;
        this.monitorService = monitorService;
        this.suggestionService = suggestionService;
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Create risk selector
        riskSelector = new JComboBox<>(TradersTavernConfig.RiskLevel.values());
        riskSelector.setSelectedItem(config.riskLevel());
        riskSelector.addActionListener(e -> updateAnalysis());
        
        // Create time frame selector
        timeFrameSelector = new JComboBox<>(TradersTavernConfig.TimeFrame.values());
        timeFrameSelector.setSelectedItem(config.timeFrame());
        timeFrameSelector.addActionListener(e -> updateAnalysis());
        
        // Create control panel
        JPanel controlPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        controlPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel riskLabel = new JLabel("Risk Level:");
        riskLabel.setForeground(Color.WHITE);
        controlPanel.add(riskLabel);
        controlPanel.add(riskSelector);
        
        JLabel timeLabel = new JLabel("Time Frame:");
        timeLabel.setForeground(Color.WHITE);
        controlPanel.add(timeLabel);
        controlPanel.add(timeFrameSelector);
        
        // Create suggestion panel
        suggestionPanel = new SuggestionPanel();
        suggestionService.addListener(suggestionPanel);
        
        // Setup content panel
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.add(suggestionPanel);
        
        add(controlPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void updateAnalysis() {
        TradersTavernConfig.RiskLevel risk = 
            (TradersTavernConfig.RiskLevel) riskSelector.getSelectedItem();
        TradersTavernConfig.TimeFrame timeFrame = 
            (TradersTavernConfig.TimeFrame) timeFrameSelector.getSelectedItem();
            
        log.debug("Updating analysis with risk={}, timeFrame={}", risk, timeFrame);
        
        // Trigger reanalysis of all monitored items
        monitorService.updateAllItems();
    }

    public void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            updateAnalysis();
            revalidate();
            repaint();
        });
    }
}