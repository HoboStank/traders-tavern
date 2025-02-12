package com.traderstavern.plugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TradersTavernConfig.CONFIG_GROUP)
public interface TradersTavernConfig extends Config {
    String CONFIG_GROUP = "traderstavern";
    
    enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
    
    enum TimeFrame {
        M1, M5, M15, M30, H1, H4, D1
    }
    
    @ConfigItem(
        keyName = "riskLevel",
        name = "Risk Level",
        description = "Choose your preferred risk level for trade suggestions",
        position = 1
    )
    default RiskLevel riskLevel() {
        return RiskLevel.MEDIUM;
    }
    
    @ConfigItem(
        keyName = "timeFrame",
        name = "Time Frame",
        description = "Choose your preferred trading time frame",
        position = 2
    )
    default TimeFrame timeFrame() {
        return TimeFrame.H1;
    }
    
    @ConfigItem(
        keyName = "autoRefreshEnabled",
        name = "Auto Refresh",
        description = "Automatically refresh data",
        position = 3
    )
    default boolean autoRefreshEnabled() {
        return true;
    }
    
    @ConfigItem(
        keyName = "showTooltips",
        name = "Show Tooltips",
        description = "Show helpful tooltips",
        position = 4
    )
    default boolean showTooltips() {
        return true;
    }
    
    @ConfigItem(
        keyName = "showVolume",
        name = "Show Volume",
        description = "Show volume information",
        position = 5
    )
    default boolean showVolume() {
        return true;
    }
    
    @ConfigItem(
        keyName = "showIndicators",
        name = "Show Indicators",
        description = "Show technical indicators",
        position = 6
    )
    default boolean showIndicators() {
        return true;
    }
    
    @ConfigItem(
        keyName = "notificationsEnabled",
        name = "Enable Notifications",
        description = "Show price alerts",
        position = 7
    )
    default boolean notificationsEnabled() {
        return true;
    }
    
    @ConfigItem(
        keyName = "soundAlertsEnabled",
        name = "Sound Alerts",
        description = "Play sound on alerts",
        position = 8
    )
    default boolean soundAlertsEnabled() {
        return true;
    }
}