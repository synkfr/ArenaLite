package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final ArenaLite plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();
    
    public ConfigManager(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        // Create default configs
        plugin.saveDefaultConfig();
        saveDefaultConfig("arenas.yml");
        saveDefaultConfig("kits.yml");
        saveDefaultConfig("settings.yml");
        saveDefaultConfig("messages.yml");
        saveDefaultConfig("scoreboard.yml");
        saveDefaultConfig("hotbar.yml");
        
        // Load all configs
        configs.put("config", plugin.getConfig());
        loadConfig("arenas.yml");
        loadConfig("kits.yml");
        loadConfig("settings.yml");
        loadConfig("messages.yml");
        loadConfig("scoreboard.yml");
        loadConfig("hotbar.yml");
    }
    
    private void saveDefaultConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }
    
    private void loadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            saveDefaultConfig(name);
        }
        configs.put(name.replace(".yml", ""), YamlConfiguration.loadConfiguration(file));
        configFiles.put(name.replace(".yml", ""), file);
    }
    
    public FileConfiguration getConfig(String name) {
        if (name.equals("config")) {
            return plugin.getConfig();
        }
        return configs.get(name);
    }
    
    public void saveConfig(String name) {
        try {
            if (name.equals("config")) {
                plugin.saveConfig();
            } else {
                FileConfiguration config = configs.get(name);
                File file = configFiles.get(name);
                if (config != null && file != null) {
                    config.save(file);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config: " + name);
            e.printStackTrace();
        }
    }
    
    public void reloadConfig(String name) {
        if (name.equals("config")) {
            plugin.reloadConfig();
        } else {
            loadConfig(name + ".yml");
        }
    }
}

