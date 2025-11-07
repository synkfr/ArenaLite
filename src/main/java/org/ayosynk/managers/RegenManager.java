package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RegenManager {
    
    private final ArenaLite plugin;
    private final Map<String, BukkitTask> regenTasks = new HashMap<>();
    private final Map<String, Set<UUID>> frozenPlayers = new HashMap<>();
    
    public RegenManager(ArenaLite plugin) {
        this.plugin = plugin;
        startRegenScheduler();
    }
    
    private void startRegenScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Arena arena : plugin.getArenaManager().getArenas().values()) {
                if (!arena.isRegenEnabled()) continue;
                
                long timeSinceLastRegen = (System.currentTimeMillis() - arena.getLastRegen()) / 1000;
                if (timeSinceLastRegen >= arena.getRegenInterval()) {
                    regenArena(arena);
                }
            }
        }, 20L, 20L); // Check every second
    }
    
    public void regenArena(Arena arena) {
        if (plugin.getFAWEHook() == null || !plugin.getFAWEHook().isAvailable()) {
            plugin.getLogger().warning("Cannot regen arena " + arena.getName() + ": FAWE not available!");
            return;
        }
        
        // Freeze players in arena
        Set<UUID> frozen = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (arena.isInRegion(player.getLocation())) {
                frozen.add(player.getUniqueId());
                freezePlayer(player);
                if (arena.getSpawn() != null) {
                    player.teleport(arena.getSpawn());
                }
            }
        }
        frozenPlayers.put(arena.getName(), frozen);
        
        // Perform async regen
        plugin.getFAWEHook().regenArena(arena).thenRun(() -> {
            // Unfreeze players
            Set<UUID> unfreeze = frozenPlayers.remove(arena.getName());
            if (unfreeze != null) {
                for (UUID uuid : unfreeze) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        unfreezePlayer(player);
                    }
                }
            }
            
            arena.setLastRegen(System.currentTimeMillis());
            plugin.getLogger().info("Arena " + arena.getName() + " has been regenerated!");
        });
    }
    
    private void freezePlayer(Player player) {
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.setInvulnerable(true);
    }
    
    private void unfreezePlayer(Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setInvulnerable(false);
    }
    
    public boolean isFrozen(Player player) {
        for (Set<UUID> frozen : frozenPlayers.values()) {
            if (frozen.contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
    
    public long getTimeUntilNextRegen(Arena arena) {
        if (!arena.isRegenEnabled()) {
            return -1;
        }
        long elapsed = (System.currentTimeMillis() - arena.getLastRegen()) / 1000;
        return Math.max(0, arena.getRegenInterval() - elapsed);
    }
}

