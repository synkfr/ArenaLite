package org.ayosynk.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.ayosynk.models.Kit;
import org.ayosynk.models.PlayerData;
import org.ayosynk.setup.SetupSession;
import org.ayosynk.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    
    private final ArenaLite plugin;
    
    public PlayerListener(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().getPlayerData(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        plugin.getStatsManager().savePlayerData(data);
        plugin.getStatsManager().removePlayerData(player.getUniqueId());
        if (plugin.getKitManager() != null) {
            plugin.getKitManager().clearPreview(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        if (data.isInArena()) {
            Arena arena = plugin.getArenaManager().getArena(data.getCurrentArena());
            if (arena != null && arena.getSpawn() != null) {
                event.setRespawnLocation(arena.getSpawn());
                
                // Give kit again
                int delay = Math.max(0, plugin.getConfig().getInt("settings.instant-respawn-delay", 1));
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (data.getCurrentKit() != null) {
                        Kit kit = plugin.getKitManager().getKit(data.getCurrentKit());
                        if (kit != null) {
                            plugin.getKitManager().applyKit(player, kit);
                        }
                    }
                }, delay);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() != Material.WOODEN_AXE) {
            return;
        }
        
        if (!item.hasItemMeta() || item.getItemMeta().displayName() == null) {
            return;
        }
        
        String displayName = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        if (!displayName.contains("Arena Selection Wand")) {
            return;
        }
        
        event.setCancelled(true);
        
        org.ayosynk.commands.FFASetupCommand setupCommand = 
            (org.ayosynk.commands.FFASetupCommand) plugin.getServer()
                .getPluginCommand("ffasetup").getExecutor();
        SetupSession session = setupCommand != null ? 
            setupCommand.getSetupSession(player) : null;
        
        if (session == null) {
            return;
        }
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            session.setPos1(event.getClickedBlock().getLocation());
            player.sendMessage(MessageUtils.colorize("&a&l[✓] &aPosition 1 set at &e" + 
                event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ()));
            if (session.getPos2() == null) {
                player.sendMessage(MessageUtils.colorize("&7Now right-click a block to set position 2."));
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            session.setPos2(event.getClickedBlock().getLocation());
            player.sendMessage(MessageUtils.colorize("&a&l[✓] &aPosition 2 set at &e" + 
                event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ()));
            
            if (session.getPos1() != null && session.getPos2() != null) {
                session.getArena().setPos1(session.getPos1());
                session.getArena().setPos2(session.getPos2());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(MessageUtils.colorize("&a&l[✓] &aArena region set for &e" + session.getArena().getName() + "&a!"));
                player.sendMessage(MessageUtils.colorize("&7You can now use &e/ffasetup setspawn " + session.getArena().getName() + " &7to set the spawn."));
            } else {
                player.sendMessage(MessageUtils.colorize("&7Now left-click a block to set position 1."));
            }
        }
    }
}

