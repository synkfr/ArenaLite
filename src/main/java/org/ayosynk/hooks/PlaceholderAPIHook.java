package org.ayosynk.hooks;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.ayosynk.models.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    
    private final ArenaLite plugin;
    
    public PlaceholderAPIHook(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "arenalite";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        switch (identifier.toLowerCase()) {
            case "kills":
                return String.valueOf(data.getKills());
                
            case "deaths":
                return String.valueOf(data.getDeaths());
                
            case "kdr":
                return String.format("%.2f", data.getKDR());
                
            case "streak":
                return String.valueOf(data.getStreak());
                
            case "kit":
                return data.getCurrentKit() != null ? data.getCurrentKit() : "None";
                
            case "arena":
                return data.getCurrentArena() != null ? data.getCurrentArena() : "None";
                
            case "nextregen_time":
                if (data.getCurrentArena() == null) {
                    return "N/A";
                }
                Arena arena = plugin.getArenaManager().getArena(data.getCurrentArena());
                if (arena == null || !arena.isRegenEnabled()) {
                    return "N/A";
                }
                long time = plugin.getRegenManager().getTimeUntilNextRegen(arena);
                if (time < 0) {
                    return "N/A";
                }
                return formatTime(time);
                
            default:
                return null;
        }
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}

