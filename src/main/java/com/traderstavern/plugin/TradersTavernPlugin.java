/*
 * Copyright (c) 2024, OpenHands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.traderstavern.plugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import com.traderstavern.service.AnalysisService;
import com.traderstavern.service.PriceService;
import com.traderstavern.service.StorageService;
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
    private StorageService storageService;

    @Inject
    private PriceService priceService;

    @Inject
    private AnalysisService analysisService;

    private NavigationButton navButton;
    private TradersTavernPanel panel;

    @Provides
    TradersTavernConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TradersTavernConfig.class);
    }

    @Override
    protected void startUp() {
        log.info("Traders Tavern plugin starting up...");
        panel = injector.getInstance(TradersTavernPanel.class);

        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/tavern_icon.png");

        navButton = NavigationButton.builder()
            .tooltip("Traders Tavern")
            .icon(icon)
            .priority(5)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navButton);
        log.info("Traders Tavern started!");

        if (client.getGameState() == GameState.LOGGED_IN) {
            loadSavedData();
        }
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
        storageService.cleanupOldData();
        log.info("Traders Tavern stopped!");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            loadSavedData();
        }
    }

    @Subscribe
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
        // Update price tracking and analysis
        priceService.updatePrice(event.getOffer());
        panel.refresh();
    }

    private void loadSavedData() {
        // Load saved data from storage
        storageService.loadPriceHistory().forEach((itemId, history) -> {
            priceService.updatePriceHistory(itemId, history);
        });
        panel.refresh();
    }
}