package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.ayosynk.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArenaManager {
    
    private final ArenaLite plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    
    public ArenaManager(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    public void loadArenas() {
        arenas.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("arenas");
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");
        
        if (arenasSection == null) {
            return;
        }
        
        for (String name : arenasSection.getKeys(false)) {
            Arena arena = new Arena(name);
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(name);
            
            if (arenaSection == null) continue;
            
            // Load world
            String worldName = arenaSection.getString("world");
            if (worldName != null) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    arena.setWorld(world);
                }
            }
            
            // Load spawn
            String spawnStr = arenaSection.getString("spawn");
            if (spawnStr != null) {
                Location spawn = LocationUtils.deserializeLocation(spawnStr, arena.getWorld());
                if (spawn != null) {
                    arena.setSpawn(spawn);
                }
            }
            
            arena.setBuildEnabled(arenaSection.getBoolean("build-enabled", true));

            // Load region
            ConfigurationSection regionSection = arenaSection.getConfigurationSection("region");
            if (regionSection != null) {
                String pos1Str = regionSection.getString("pos1");
                String pos2Str = regionSection.getString("pos2");
                if (pos1Str != null) {
                    arena.setPos1(LocationUtils.deserializeLocation(pos1Str, arena.getWorld()));
                }
                if (pos2Str != null) {
                    arena.setPos2(LocationUtils.deserializeLocation(pos2Str, arena.getWorld()));
                }
            }
            
            // Load regen settings
            ConfigurationSection regenSection = arenaSection.getConfigurationSection("regen");
            if (regenSection != null) {
                arena.setRegenEnabled(regenSection.getBoolean("enabled", false));
                arena.setRegenInterval(regenSection.getInt("interval", 300));
            }
            
            arenas.put(name, arena);
        }
        
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas!");
    }
    
    public void saveArenas() {
        FileConfiguration config = plugin.getConfigManager().getConfig("arenas");
        config.set("arenas", null);
        
        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            Map<String, Object> serialized = arena.serialize();
            
            for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                config.set(path + "." + entry.getKey(), entry.getValue());
            }
            
            // Save region separately
            if (arena.getPos1() != null && arena.getPos2() != null) {
                config.set(path + ".region.pos1", 
                    arena.getPos1().getBlockX() + "," + 
                    arena.getPos1().getBlockY() + "," + 
                    arena.getPos1().getBlockZ());
                config.set(path + ".region.pos2", 
                    arena.getPos2().getBlockX() + "," + 
                    arena.getPos2().getBlockY() + "," + 
                    arena.getPos2().getBlockZ());
            }

            config.set(path + ".build-enabled", arena.isBuildEnabled());
        }
        
        plugin.getConfigManager().saveConfig("arenas");
    }
    
    public Arena getArena(String name) {
        return arenas.get(name);
    }
    
    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        return arena;
    }
    
    public void deleteArena(String name) {
        arenas.remove(name);
    }
    
    public Set<String> getArenaNames() {
        return arenas.keySet();
    }
    
    public Map<String, Arena> getArenas() {
        return arenas;
    }
}

