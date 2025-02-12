package com.traderstavern.plugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("traderstavern")
public interface TradersTavernConfig extends Config {
    @ConfigItem(
        keyName = "riskLevel",
        name = "Risk Level",
        description = "Choose your preferred risk level for trade suggestions",
        position = 1
    )
    default RiskLevel riskLevel() {
        return RiskLevel.BALANCED;
    }

    @ConfigItem(
        keyName = "timeFrame",
        name = "Time Frame",
        description = "Choose your preferred trading time frame",
        position = 2
    )
    default TimeFrame timeFrame() {
        return TimeFrame.MEDIUM;
    }

    enum RiskLevel {
        SAFER, SAFE, BALANCED, RISKY, RISKIER
    }

    enum TimeFrame {
        SHORT, MEDIUM, LONG
    }
}