package com.traderstavern.ui;

import com.traderstavern.plugin.TradersTavernConfig;
import com.traderstavern.service.AnalysisService;
import com.traderstavern.service.PriceService;
import com.traderstavern.ui.components.AnalysisPanel;
import com.traderstavern.ui.components.SettingsPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Slf4j
public class TradersTavernPanel extends PluginPanel {
    private static final int CONTENT_PADDING = 10;
    
    private final TradersTavernConfig config;
    private final AnalysisService analysisService;
    private final PriceService priceService;
    private final ConfigManager configManager;
    
    private final JPanel contentPanel = new JPanel();
    private final MaterialTabGroup tabGroup = new MaterialTabGroup(contentPanel);
    
    private final AnalysisPanel analysisPanel;
    private final SettingsPanel settingsPanel;
    
    private MaterialTab analysisTab;
    private MaterialTab settingsTab;
    
    @Inject
    public TradersTavernPanel(
            TradersTavernConfig config,
            AnalysisService analysisService,
            PriceService priceService,
            ConfigManager configManager) {
        super(false);
        this.config = config;
        this.analysisService = analysisService;
        this.priceService = priceService;
        this.configManager = configManager;
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Create panels
        analysisPanel = new AnalysisPanel(config, analysisService);
        settingsPanel = new SettingsPanel(config, configManager);
        
        // Setup tabs
        setupTabs();
        
        // Add components
        add(tabGroup, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Select default tab
        tabGroup.select(analysisTab);
        
        log.info("Traders Tavern panel initialized");
    }
    
    private void setupTabs() {
        analysisTab = new MaterialTab("Analysis", tabGroup, analysisPanel);
        settingsTab = new MaterialTab("Settings", tabGroup, settingsPanel);
        
        tabGroup.addTab(analysisTab);
        tabGroup.addTab(settingsTab);
    }
    
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            analysisPanel.refresh();
            settingsPanel.refresh();
            revalidate();
            repaint();
        });
    }
}