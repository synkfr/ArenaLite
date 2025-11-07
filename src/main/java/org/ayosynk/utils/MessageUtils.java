package org.ayosynk.utils;

import org.ayosynk.ArenaLite;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static String applyHex(String input) {
        if (input == null || input.isEmpty()) return input;
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            String color = net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(color));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String colorize(String message) {
        // First replace hex tokens like &#RRGGBB with section-coded colors, then apply legacy '&' colors
        String withHex = applyHex(message);
        return ChatColor.translateAlternateColorCodes('&', withHex);
    }
    
    public static void sendMessage(Player player, String key) {
        FileConfiguration messages = ArenaLite.getInstance().getConfigManager().getConfig("messages");
        String prefix = messages.getString("prefix", "");
        String message = messages.getString(key, "&cMessage not found: " + key);
        player.sendMessage(colorize((prefix.isEmpty() ? "" : prefix + " ") + message));
    }
    
    public static void sendMessage(Player player, String key, String... replacements) {
        FileConfiguration messages = ArenaLite.getInstance().getConfigManager().getConfig("messages");
        String prefix = messages.getString("prefix", "");
        String message = messages.getString(key, "&cMessage not found: " + key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        player.sendMessage(colorize((prefix.isEmpty() ? "" : prefix + " ") + message));
    }
}


