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
        String lowerId = identifier.toLowerCase();
        
        // Leaderboard placeholders: leaderboard_<type>_<kit>_<position>
        // Format: leaderboard_kills_kitname_1
        if (lowerId.startsWith("leaderboard_")) {
            String rest = lowerId.substring("leaderboard_".length());
            String[] parts = rest.split("_");
            if (parts.length >= 3) {
                String type = parts[0]; // kills, deaths, kdr, streak, player
                // Last part is position, everything in between is kit name
                try {
                    int position = Integer.parseInt(parts[parts.length - 1]);
                    // Reconstruct kit name from middle parts
                    StringBuilder kitNameBuilder = new StringBuilder();
                    for (int i = 1; i < parts.length - 1; i++) {
                        if (i > 1) kitNameBuilder.append("_");
                        kitNameBuilder.append(parts[i]);
                    }
                    String kitName = kitNameBuilder.toString();
                    return getLeaderboardValue(type, kitName, position);
                } catch (NumberFormatException e) {
                    return "Invalid position";
                }
            }
            return null;
        }
        
        if (player == null) {
            return "";
        }
        
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        switch (lowerId) {
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
    
    private String getLeaderboardValue(String type, String kitName, int position) {
        if (position < 1) {
            return "N/A";
        }
        
        int index = position - 1; // Convert to 0-based index
        
        switch (type.toLowerCase()) {
            case "kills": {
                var entries = plugin.getStatsManager().getTopKillsForKit(kitName, position);
                if (index >= entries.size()) {
                    return "N/A";
                }
                return String.valueOf(entries.get(index).getValue());
            }
            case "deaths": {
                var entries = plugin.getStatsManager().getTopDeathsForKit(kitName, position);
                if (index >= entries.size()) {
                    return "N/A";
                }
                return String.valueOf(entries.get(index).getValue());
            }
            case "kdr": {
                var entries = plugin.getStatsManager().getTopKDRForKit(kitName, position);
                if (index >= entries.size()) {
                    return "N/A";
                }
                return String.format("%.2f", entries.get(index).getValue());
            }
            case "streak": {
                var entries = plugin.getStatsManager().getTopStreakForKit(kitName, position);
                if (index >= entries.size()) {
                    return "N/A";
                }
                return String.valueOf(entries.get(index).getValue());
            }
            case "player": {
                // Try kills first, then deaths, then kdr, then streak
                var entries = plugin.getStatsManager().getTopKillsForKit(kitName, position);
                if (index >= entries.size()) {
                    entries = plugin.getStatsManager().getTopDeathsForKit(kitName, position);
                    if (index >= entries.size()) {
                        var kdrEntries = plugin.getStatsManager().getTopKDRForKit(kitName, position);
                        if (index >= kdrEntries.size()) {
                            entries = plugin.getStatsManager().getTopStreakForKit(kitName, position);
                            if (index >= entries.size()) {
                                return "N/A";
                            }
                        } else {
                            return plugin.getStatsManager().getPlayerName(kdrEntries.get(index).getKey());
                        }
                    }
                }
                return plugin.getStatsManager().getPlayerName(entries.get(index).getKey());
            }
            default:
                return "Invalid type";
        }
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}

