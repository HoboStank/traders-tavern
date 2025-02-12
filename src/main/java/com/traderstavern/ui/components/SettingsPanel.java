package com.traderstavern.ui.components;

import com.traderstavern.plugin.TradersTavernConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Slf4j
public class SettingsPanel extends BasePanel {
    private final TradersTavernConfig config;
    private final ConfigManager configManager;
    
    private final JPanel settingsContainer;
    
    @Inject
    public SettingsPanel(TradersTavernConfig config, ConfigManager configManager) {
        this.config = config;
        this.configManager = configManager;
        
        addHeader("Settings");
        
        // Create settings container
        settingsContainer = createContentPanel();
        
        // Add settings sections
        addGeneralSettings();
        addAnalysisSettings();
        addNotificationSettings();
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(settingsContainer);
        scrollPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void addGeneralSettings() {
        addSectionHeader("General Settings");
        addSettingCheckbox("Enable Auto-Refresh", config.autoRefreshEnabled(),
            "autoRefreshEnabled", "Automatically refresh data");
        addSettingCheckbox("Show Tooltips", config.showTooltips(),
            "showTooltips", "Show helpful tooltips");
    }
    
    private void addAnalysisSettings() {
        addSectionHeader("Analysis Settings");
        addSettingCheckbox("Show Volume", config.showVolume(),
            "showVolume", "Show volume information");
        addSettingCheckbox("Show Indicators", config.showIndicators(),
            "showIndicators", "Show technical indicators");
    }
    
    private void addNotificationSettings() {
        addSectionHeader("Notification Settings");
        addSettingCheckbox("Enable Notifications", config.notificationsEnabled(),
            "notificationsEnabled", "Show price alerts");
        addSettingCheckbox("Sound Alerts", config.soundAlertsEnabled(),
            "soundAlertsEnabled", "Play sound on alerts");
    }
    
    private void addSectionHeader(String text) {
        JLabel header = new JLabel(text);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        settingsContainer.add(header);
    }
    
    private void addSettingCheckbox(String text, boolean defaultValue, String key, String tooltip) {
        JCheckBox checkbox = createCheckBox(text);
        checkbox.setSelected(defaultValue);
        checkbox.setToolTipText(tooltip);
        checkbox.addActionListener(e -> {
            configManager.setConfiguration(
                TradersTavernConfig.CONFIG_GROUP,
                key,
                checkbox.isSelected()
            );
        });
        settingsContainer.add(checkbox);
    }
    
    @Override
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            // Update settings if needed
            revalidate();
            repaint();
        });
    }
}