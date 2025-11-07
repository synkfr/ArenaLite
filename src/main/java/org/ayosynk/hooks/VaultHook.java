package org.ayosynk.hooks;

import org.ayosynk.ArenaLite;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    
    private final ArenaLite plugin;
    private Economy economy;
    
    public VaultHook(ArenaLite plugin) {
        this.plugin = plugin;
        setupEconomy();
    }
    
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer()
            .getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) {
            return;
        }
        
        economy = rsp.getProvider();
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean isAvailable() {
        return economy != null;
    }
}

