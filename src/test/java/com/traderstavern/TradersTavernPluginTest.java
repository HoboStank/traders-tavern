package com.traderstavern;

import com.traderstavern.plugin.TradersTavernPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TradersTavernPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(TradersTavernPlugin.class);
        RuneLite.main(args);
    }
}