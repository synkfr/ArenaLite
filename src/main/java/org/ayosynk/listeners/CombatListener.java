package org.ayosynk.listeners;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatListener implements Listener {
    
    private final ArenaLite plugin;
    
    public CombatListener(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        if (!data.isInArena()) {
            return;
        }
        
        event.setCancelled(false);

        // Check if player is frozen during regen
        if (plugin.getRegenManager().isFrozen(player)) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        if (!data.isInArena()) {
            return;
        }
        
        // Cancel death screen
        event.setKeepInventory(false);
        event.setKeepLevel(false);
        event.getDrops().clear();
        
        // Handle killer
        Player killer = player.getKiller();
        if (killer != null) {
            plugin.getStatsManager().onPlayerKill(killer, player);
        }
        
        data.addDeath();
        
        // Instant respawn
        int delay = Math.max(0, plugin.getConfig().getInt("settings.instant-respawn-delay", 1));
        new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().respawn();
            }
        }.runTaskLater(plugin, delay);
        
        // Apply anti-cleanup effects
        if (killer != null) {
            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("anti-cleanup.enabled", true)) {
                boolean regenEnabled = config.getBoolean("anti-cleanup.regen.enabled", true);
                boolean resistanceEnabled = config.getBoolean("anti-cleanup.resistance.enabled", true);
                int regenDuration = config.getInt("anti-cleanup.regen.duration", 3) * 20;
                int resistanceDuration = config.getInt("anti-cleanup.resistance.duration", 5) * 20;
                int resistanceLevel = config.getInt("anti-cleanup.resistance.level", 2);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!killer.isOnline()) {
                            return;
                        }
                        if (regenEnabled && regenDuration > 0) {
                            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration, 1, false, false));
                        }
                        if (resistanceEnabled && resistanceDuration > 0) {
                            killer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, resistanceDuration, Math.max(0, resistanceLevel), false, false));
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }
}

