package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.KitStats;
import org.ayosynk.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class StatsManager {
    
    private final ArenaLite plugin;
    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    
    public StatsManager(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, k -> {
            PlayerData data = new PlayerData(uuid);
            loadPlayerData(data);
            return data;
        });
    }
    
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    public void loadPlayerData(PlayerData data) {
        plugin.getPlayerDataStorage().loadPlayerData(data).thenRun(() -> {
            // Data loaded
        });
    }
    
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return plugin.getPlayerDataStorage().savePlayerData(data);
    }
    
    public void saveAllStats() {
        for (PlayerData data : playerDataCache.values()) {
            savePlayerData(data);
        }
    }
    
    public void removePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.remove(uuid);
        if (data != null) {
            savePlayerData(data);
        }
    }
    
    public void onPlayerKill(Player killer, Player victim) {
        PlayerData killerData = getPlayerData(killer);
        PlayerData victimData = getPlayerData(victim);
        
        killerData.addKill();
        victimData.addDeath();
        
        // Track per-kit stats
        String killerKit = killerData.getCurrentKit();
        String victimKit = victimData.getCurrentKit();
        
        if (killerKit != null) {
            killerData.addKillForKit(killerKit);
        }
        if (victimKit != null) {
            victimData.addDeathForKit(victimKit);
        }
        
        savePlayerData(killerData);
        savePlayerData(victimData);
    }
    
    // Leaderboard methods
    public List<Map.Entry<UUID, Integer>> getTopKillsForKit(String kitName, int limit) {
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>();
        
        for (PlayerData data : playerDataCache.values()) {
            KitStats stats = data.getKitStats(kitName);
            if (stats != null && stats.getKills() > 0) {
                entries.add(new AbstractMap.SimpleEntry<>(data.getUuid(), stats.getKills()));
            }
        }
        
        // Also check offline players from storage
        plugin.getPlayerDataStorage().getAllPlayerData().forEach((uuid, data) -> {
            if (!playerDataCache.containsKey(uuid)) {
                KitStats stats = data.getKitStats(kitName);
                if (stats != null && stats.getKills() > 0) {
                    entries.add(new AbstractMap.SimpleEntry<>(uuid, stats.getKills()));
                }
            }
        });
        
        return entries.stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<Map.Entry<UUID, Integer>> getTopDeathsForKit(String kitName, int limit) {
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>();
        
        for (PlayerData data : playerDataCache.values()) {
            KitStats stats = data.getKitStats(kitName);
            if (stats != null && stats.getDeaths() > 0) {
                entries.add(new AbstractMap.SimpleEntry<>(data.getUuid(), stats.getDeaths()));
            }
        }
        
        plugin.getPlayerDataStorage().getAllPlayerData().forEach((uuid, data) -> {
            if (!playerDataCache.containsKey(uuid)) {
                KitStats stats = data.getKitStats(kitName);
                if (stats != null && stats.getDeaths() > 0) {
                    entries.add(new AbstractMap.SimpleEntry<>(uuid, stats.getDeaths()));
                }
            }
        });
        
        return entries.stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<Map.Entry<UUID, Double>> getTopKDRForKit(String kitName, int limit) {
        List<Map.Entry<UUID, Double>> entries = new ArrayList<>();
        
        for (PlayerData data : playerDataCache.values()) {
            KitStats stats = data.getKitStats(kitName);
            if (stats != null && stats.getKills() > 0) {
                entries.add(new AbstractMap.SimpleEntry<>(data.getUuid(), stats.getKDR()));
            }
        }
        
        plugin.getPlayerDataStorage().getAllPlayerData().forEach((uuid, data) -> {
            if (!playerDataCache.containsKey(uuid)) {
                KitStats stats = data.getKitStats(kitName);
                if (stats != null && stats.getKills() > 0) {
                    entries.add(new AbstractMap.SimpleEntry<>(uuid, stats.getKDR()));
                }
            }
        });
        
        return entries.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<Map.Entry<UUID, Integer>> getTopStreakForKit(String kitName, int limit) {
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>();
        
        for (PlayerData data : playerDataCache.values()) {
            KitStats stats = data.getKitStats(kitName);
            if (stats != null && stats.getStreak() > 0) {
                entries.add(new AbstractMap.SimpleEntry<>(data.getUuid(), stats.getStreak()));
            }
        }
        
        plugin.getPlayerDataStorage().getAllPlayerData().forEach((uuid, data) -> {
            if (!playerDataCache.containsKey(uuid)) {
                KitStats stats = data.getKitStats(kitName);
                if (stats != null && stats.getStreak() > 0) {
                    entries.add(new AbstractMap.SimpleEntry<>(uuid, stats.getStreak()));
                }
            }
        });
        
        return entries.stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public String getPlayerName(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() != null) {
            return player.getName();
        }
        return "Unknown";
    }
}

