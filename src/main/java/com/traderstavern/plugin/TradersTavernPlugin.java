package com.traderstavern.plugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import com.traderstavern.ui.TradersTavernPanel;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
    name = "Traders Tavern",
    description = "Advanced OSRS trading analysis and flip suggestions",
    tags = {"trading", "flipping", "grand exchange", "market"}
)
public class TradersTavernPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private TradersTavernConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private TradersTavernPanel panel;
    
    @Inject
    private StorageService storageService;

    private NavigationButton navButton;

    @Override
    protected void startUp() {
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/tavern_icon.png");
        
        navButton = NavigationButton.builder()
            .tooltip("Traders Tavern")
            .icon(icon)
            .priority(5)
            .panel(panel)
            .build();
        
        clientToolbar.addNavigation(navButton);
        log.info("Traders Tavern started!");
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
        storageService.cleanupOldData();
        log.info("Traders Tavern stopped!");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            log.info("Player logged in!");
        }
    }

    @Provides
    TradersTavernConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TradersTavernConfig.class);
    }
}