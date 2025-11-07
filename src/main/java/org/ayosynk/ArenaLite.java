package org.ayosynk;

import org.ayosynk.hooks.FAWEHook;
import org.ayosynk.hooks.PlaceholderAPIHook;
import org.ayosynk.hooks.VaultHook;
import org.ayosynk.hooks.ArenaRegenPlaceholder;
import org.ayosynk.listeners.BuildListener;
import org.ayosynk.listeners.CombatListener;
import org.ayosynk.listeners.KitPreviewListener;
import org.ayosynk.listeners.PlayerListener;
import org.ayosynk.listeners.FreezeListener;
import org.ayosynk.managers.ArenaManager;
import org.ayosynk.managers.ConfigManager;
import org.ayosynk.managers.KitManager;
import org.ayosynk.managers.RegenManager;
import org.ayosynk.managers.StatsManager;
import org.ayosynk.managers.SnapshotManager;
import org.ayosynk.storage.PlayerDataStorage;
import org.ayosynk.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaLite extends JavaPlugin {

    private static ArenaLite instance;
    
    private ConfigManager configManager;
    private PlayerDataStorage playerDataStorage;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private RegenManager regenManager;
    private StatsManager statsManager;
    private SnapshotManager snapshotManager;
    
    private FAWEHook faweHook;
    private PlaceholderAPIHook placeholderAPIHook;
    private VaultHook vaultHook;

    private Location ffaSpawn;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize config manager first
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        loadFfaSpawn();
        
        // Initialize player data storage
        playerDataStorage = new PlayerDataStorage(this);
        playerDataStorage.initialize();
        
        // Initialize managers
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        regenManager = new RegenManager(this);
        statsManager = new StatsManager(this);
        snapshotManager = new SnapshotManager(this);
        
        // Load data
        arenaManager.loadArenas();
        kitManager.loadKits();
        
        // Register commands
        if (getCommand("ffa") != null) {
            getCommand("ffa").setExecutor(new org.ayosynk.commands.FFACommand(this));
            getCommand("ffa").setTabCompleter(new org.ayosynk.commands.FFACommandTabCompleter(this));
        }
        if (getCommand("ffasetup") != null) {
            getCommand("ffasetup").setExecutor(new org.ayosynk.commands.FFASetupCommand(this));
            getCommand("ffasetup").setTabCompleter(new org.ayosynk.commands.FFASetupCommandTabCompleter(this));
        }
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new KitPreviewListener(this, kitManager), this);
        getServer().getPluginManager().registerEvents(new BuildListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        
        // Initialize hooks
        initializeHooks();
        
        getLogger().info("ArenaLite has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data
        if (arenaManager != null) {
            arenaManager.saveArenas();
        }
        if (kitManager != null) {
            kitManager.saveKits();
        }
        if (statsManager != null) {
            statsManager.saveAllStats();
        }
        
        // Close storage
        if (playerDataStorage != null) {
            playerDataStorage.close();
        }
        
        getLogger().info("ArenaLite has been disabled!");
    }
    
    private void initializeHooks() {
        // FAWE Hook
        if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            faweHook = new FAWEHook(this);
            getLogger().info("FAWE hook enabled (optional).");
        } else {
            // Optional dependency; built-in regen is used when FAWE is not present
        }
        
        // PlaceholderAPI Hook
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIHook = new PlaceholderAPIHook(this);
            placeholderAPIHook.register();
            new ArenaRegenPlaceholder(this).register();
            getLogger().info("PlaceholderAPI hook enabled!");
        }
        
        // Vault Hook
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(this);
            getLogger().info("Vault hook enabled!");
        }

    }
    
    public void reload() {
        configManager.loadConfigs();
        arenaManager.loadArenas();
        kitManager.loadKits();
        loadFfaSpawn();
        if (playerDataStorage != null) {
            playerDataStorage.reload();
        }
        getLogger().info("ArenaLite reloaded!");
    }
    
    public static ArenaLite getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public PlayerDataStorage getPlayerDataStorage() {
        return playerDataStorage;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public RegenManager getRegenManager() {
        return regenManager;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public FAWEHook getFAWEHook() {
        return faweHook;
    }
    
    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }
    
    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public SnapshotManager getSnapshotManager() {
        return snapshotManager;
    }

    public Location getFfaSpawn() {
        return ffaSpawn;
    }

    public void setFfaSpawn(Location location) {
        this.ffaSpawn = location != null ? location.clone() : null;
        if (location != null) {
            getConfig().set("ffa.spawn", LocationUtils.serializeLocation(location));
        } else {
            getConfig().set("ffa.spawn", null);
        }
        saveConfig();
    }

    private void loadFfaSpawn() {
        String serialized = getConfig().getString("ffa.spawn", "");
        if (serialized == null || serialized.isEmpty()) {
            ffaSpawn = null;
            return;
        }
        Location fallback = getServer().getWorlds().isEmpty() ? null : getServer().getWorlds().get(0).getSpawnLocation();
        ffaSpawn = LocationUtils.deserializeLocation(serialized, fallback != null ? fallback.getWorld() : null);
    }
}

