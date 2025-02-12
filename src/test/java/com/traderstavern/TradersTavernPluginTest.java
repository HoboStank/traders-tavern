package com.traderstavern;

import com.traderstavern.plugin.TradersTavernPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TradersTavernPluginTest {
    public static void main(String[] args) throws Exception {
        // Clear RuneLite cache if needed
        if (args.length > 0 && args[0].equals("--clear-cache")) {
            clearRuneLiteCache();
        }
        
        ExternalPluginManager.loadBuiltin(TradersTavernPlugin.class);
        RuneLite.main(args);
    }
    
    private static void clearRuneLiteCache() {
        try {
            String userHome = System.getProperty("user.home");
            Path cachePath = Path.of(userHome, ".runelite", "cache");
            
            if (Files.exists(cachePath)) {
                Files.walk(cachePath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
                System.out.println("RuneLite cache cleared successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to clear RuneLite cache: " + e.getMessage());
        }
    }
}