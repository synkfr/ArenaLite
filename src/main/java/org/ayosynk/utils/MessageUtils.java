package org.ayosynk.utils;

import org.ayosynk.ArenaLite;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(Player player, String key) {
        FileConfiguration messages = ArenaLite.getInstance().getConfigManager().getConfig("messages");
        String message = messages.getString(key, "&cMessage not found: " + key);
        player.sendMessage(colorize(message));
    }
    
    public static void sendMessage(Player player, String key, String... replacements) {
        FileConfiguration messages = ArenaLite.getInstance().getConfigManager().getConfig("messages");
        String message = messages.getString(key, "&cMessage not found: " + key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        player.sendMessage(colorize(message));
    }
}

