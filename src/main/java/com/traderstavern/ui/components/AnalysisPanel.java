package com.traderstavern.ui.components;

import com.traderstavern.plugin.TradersTavernConfig;
import com.traderstavern.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.ComboBoxListRenderer;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Slf4j
public class AnalysisPanel extends BasePanel {
    private final TradersTavernConfig config;
    private final AnalysisService analysisService;
    
    private final JComboBox<TradersTavernConfig.RiskLevel> riskSelector;
    private final JComboBox<TradersTavernConfig.TimeFrame> timeFrameSelector;
    private final JPanel resultsPanel;
    
    @Inject
    public AnalysisPanel(TradersTavernConfig config, AnalysisService analysisService) {
        this.config = config;
        this.analysisService = analysisService;
        
        addHeader("Market Analysis");
        
        // Create selectors panel
        JPanel selectorsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        selectorsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Create risk selector
        riskSelector = new JComboBox<>(TradersTavernConfig.RiskLevel.values());
        riskSelector.setRenderer(new ComboBoxListRenderer<>());
        riskSelector.setForeground(Color.WHITE);
        riskSelector.setSelectedItem(config.riskLevel());
        riskSelector.addActionListener(e -> updateAnalysis());
        
        // Create time frame selector
        timeFrameSelector = new JComboBox<>(TradersTavernConfig.TimeFrame.values());
        timeFrameSelector.setRenderer(new ComboBoxListRenderer<>());
        timeFrameSelector.setForeground(Color.WHITE);
        timeFrameSelector.setSelectedItem(config.timeFrame());
        timeFrameSelector.addActionListener(e -> updateAnalysis());
        
        // Add components to selectors panel
        selectorsPanel.add(createLabel("Risk Level:"));
        selectorsPanel.add(riskSelector);
        selectorsPanel.add(createLabel("Time Frame:"));
        selectorsPanel.add(timeFrameSelector);
        
        // Create results panel
        resultsPanel = createContentPanel();
        
        // Add panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        mainPanel.add(selectorsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, CONTENT_PADDING)));
        mainPanel.add(resultsPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void updateAnalysis() {
        TradersTavernConfig.RiskLevel risk = 
            (TradersTavernConfig.RiskLevel) riskSelector.getSelectedItem();
        TradersTavernConfig.TimeFrame timeFrame = 
            (TradersTavernConfig.TimeFrame) timeFrameSelector.getSelectedItem();
            
        log.debug("Updating analysis with risk={}, timeFrame={}", risk, timeFrame);
        
        // Trigger reanalysis
        analysisService.updateAnalysis(risk, timeFrame);
        refresh();
    }
    
    @Override
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            riskSelector.setSelectedItem(config.riskLevel());
            timeFrameSelector.setSelectedItem(config.timeFrame());
            
            resultsPanel.removeAll();
            // Add analysis results here
            resultsPanel.revalidate();
            resultsPanel.repaint();
        });
    }
}