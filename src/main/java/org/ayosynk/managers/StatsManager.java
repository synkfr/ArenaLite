package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        
        savePlayerData(killerData);
        savePlayerData(victimData);
    }
}

