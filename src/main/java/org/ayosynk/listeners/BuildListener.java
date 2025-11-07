package org.ayosynk.listeners;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.ayosynk.models.PlayerData;
import org.ayosynk.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuildListener implements Listener {

    private final ArenaLite plugin;
    private final Map<UUID, Long> lastWarning = new HashMap<>();
    private static final long WARNING_COOLDOWN_MS = 2000L;

    public BuildListener(ArenaLite plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStatsManager().getPlayerData(player);

        if (!data.isInArena()) {
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(data.getCurrentArena());
        if (arena == null) {
            return;
        }
        handleBuildFlag(player, arena, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStatsManager().getPlayerData(player);

        if (!data.isInArena()) {
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(data.getCurrentArena());
        if (arena == null) {
            return;
        }

        handleBuildFlag(player, arena, event);
    }

    private void handleBuildFlag(Player player, Arena arena, Cancellable event) {
        if (arena.isBuildEnabled()) {
            event.setCancelled(false);
        } else {
            event.setCancelled(true);
            long now = System.currentTimeMillis();
            Long last = lastWarning.get(player.getUniqueId());
            if (last == null || now - last > WARNING_COOLDOWN_MS) {
                MessageUtils.sendMessage(player, "messages.build-disabled");
                lastWarning.put(player.getUniqueId(), now);
            }
        }
    }
}


