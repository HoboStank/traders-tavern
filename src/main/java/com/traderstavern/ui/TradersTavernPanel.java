package com.traderstavern.ui;

import com.traderstavern.plugin.TradersTavernConfig;
import com.traderstavern.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Slf4j
public class TradersTavernPanel extends PluginPanel {
    private final TradersTavernConfig config;
    private final PriceService priceService;
    
    private final JPanel contentPanel = new JPanel();
    private final JComboBox<TradersTavernConfig.RiskLevel> riskSelector;
    private final JComboBox<TradersTavernConfig.TimeFrame> timeFrameSelector;
    
    @Inject
    public TradersTavernPanel(TradersTavernConfig config, PriceService priceService) {
        super(false);
        this.config = config;
        this.priceService = priceService;
        
        setLayout(new BorderLayout());
        
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
        controlPanel.add(new JLabel("Risk Level:"));
        controlPanel.add(riskSelector);
        controlPanel.add(new JLabel("Time Frame:"));
        controlPanel.add(timeFrameSelector);
        
        // Setup content panel
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        add(controlPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void updateAnalysis() {
        // Will be implemented in the next phase
        log.debug("Updating analysis with risk={}, timeFrame={}", 
            riskSelector.getSelectedItem(),
            timeFrameSelector.getSelectedItem());
    }
}