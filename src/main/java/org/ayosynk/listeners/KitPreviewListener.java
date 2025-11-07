package org.ayosynk.listeners;

import org.ayosynk.ArenaLite;
import org.ayosynk.managers.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class KitPreviewListener implements Listener {

    private final ArenaLite plugin;
    private final KitManager kitManager;

    public KitPreviewListener(ArenaLite plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!kitManager.isPreviewInventory(player, event.getView().getTopInventory())) {
            return;
        }

        if (plugin.getConfig().getBoolean("settings.kit-previews.readonly", true)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!kitManager.isPreviewInventory(player, event.getView().getTopInventory())) {
            return;
        }

        if (plugin.getConfig().getBoolean("settings.kit-previews.readonly", true)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!kitManager.isPreviewing(player)) {
            return;
        }

        kitManager.clearPreview(player);
    }
}

