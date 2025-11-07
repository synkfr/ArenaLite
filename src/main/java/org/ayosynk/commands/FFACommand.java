package org.ayosynk.commands;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.ayosynk.models.Kit;
import org.ayosynk.models.PlayerData;
import org.ayosynk.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FFACommand implements CommandExecutor {
    
    private final ArenaLite plugin;
    private final Map<UUID, BukkitTask> protectionTasks = new HashMap<>();
    
    public FFACommand(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "commands.ffa.join.usage");
                    return true;
                }
                handleJoin(player, args[1]);
                break;
                
            case "leave":
                handleLeave(player);
                break;
                
            case "kit":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "commands.ffa.kit.usage");
                    return true;
                }
                if (args[1].equalsIgnoreCase("preview")) {
                    if (args.length < 3) {
                        MessageUtils.sendMessage(player, "commands.ffa.kit.usage");
                        return true;
                    }
                    handleKitPreview(player, args[2]);
                } else if (args[1].equalsIgnoreCase("create")) {
                    if (!player.hasPermission("arenalite.admin")) {
                        MessageUtils.sendMessage(player, "messages.no-permission");
                        return true;
                    }
                    if (args.length < 3) {
                        MessageUtils.sendMessage(player, "commands.ffasetup.kit.usage");
                        return true;
                    }
                    handleKitCreate(player, args[2]);
                } else {
                    MessageUtils.sendMessage(player, "commands.ffa.kit.usage");
                }
                break;
                
            case "regen":
                if (!player.hasPermission("arenalite.admin")) {
                    MessageUtils.sendMessage(player, "messages.no-permission");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "commands.ffa.regen.usage");
                    return true;
                }
                handleRegen(player, args[1]);
                break;

            case "setspawn":
                if (!player.hasPermission("arenalite.admin")) {
                    MessageUtils.sendMessage(player, "messages.no-permission");
                    return true;
                }
                if (args.length > 1) {
                    MessageUtils.sendMessage(player, "commands.ffa.setspawn.usage");
                    return true;
                }
                handleSetSpawn(player);
                break;
                
            case "reload":
                if (!player.hasPermission("arenalite.admin")) {
                    MessageUtils.sendMessage(player, "messages.no-permission");
                    return true;
                }
                plugin.reload();
                MessageUtils.sendMessage(player, "commands.ffa.reload.success");
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleJoin(Player player, String kitName) {
        if (kitName == null) {
            MessageUtils.sendMessage(player, "messages.kit-not-found", "%kit%", "");
            return;
        }
        kitName = kitName.trim();
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "messages.kit-not-found", "%kit%", kitName);
            return;
        }
        
        String arenaName = kit.getLinkedArena();
        if (arenaName == null) {
            MessageUtils.sendMessage(player, "messages.kit-no-arena");
            return;
        }
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", arenaName);
            return;
        }
        
        if (arena.getSpawn() == null) {
            MessageUtils.sendMessage(player, "messages.arena-no-spawn", "%arena%", arenaName);
            return;
        }
        
        // TODO: Store lobby location for leave command
        
        // Teleport to arena
        player.teleport(arena.getSpawn());
        
        if (plugin.getConfig().getBoolean("join-protection.clear-effects", true)) {
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.setFireTicks(0);
        }

        // Clear inventory and give kit
        plugin.getKitManager().applyKit(player, kit);
        
        // Update player data
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        data.setInArena(true);
        data.setCurrentKit(kit.getName());
        data.setCurrentArena(arena.getName());
        data.setJoinTime(System.currentTimeMillis());
        
        // Ensure Survival gamemode and flight off
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);

        applyJoinProtection(player);
        
        MessageUtils.sendMessage(player, "messages.joined-arena", "%arena%", arenaName, "%kit%", kit.getName());
    }
    
    private void handleLeave(Player player) {
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        if (!data.isInArena()) {
            MessageUtils.sendMessage(player, "messages.not-in-arena");
            return;
        }
        
        // Remove protection
        removeJoinProtection(player);
        
        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        if (plugin.getConfig().getBoolean("join-protection.clear-effects", true)) {
            player.getActivePotionEffects().forEach(effect -> 
                player.removePotionEffect(effect.getType()));
        }
        
        // Teleport to spawn/lobby
        Location spawn = plugin.getFfaSpawn();
        if (spawn == null) {
            MessageUtils.sendMessage(player, "messages.spawn-not-set");
            spawn = player.getWorld().getSpawnLocation();
        }
        if (spawn != null) {
            player.teleport(spawn);
        }
        
        // Update data
        data.setInArena(false);
        data.setCurrentKit(null);
        data.setCurrentArena(null);
        
        MessageUtils.sendMessage(player, "messages.left-arena");
    }
    
    private void handleKitPreview(Player player, String kitName) {
        if (kitName == null) {
            MessageUtils.sendMessage(player, "messages.kit-not-found", "%kit%", "");
            return;
        }
        kitName = kitName.trim();
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "messages.kit-not-found", "%kit%", kitName);
            return;
        }
        if (!plugin.getConfig().getBoolean("settings.kit-previews.enabled", true)) {
            MessageUtils.sendMessage(player, "messages.feature-disabled", "%feature%", "Kit previews");
            return;
        }
        plugin.getKitManager().openPreview(player, kit);
        MessageUtils.sendMessage(player, "messages.kit-preview", "%kit%", kit.getName());
    }

    private void handleKitCreate(Player player, String kitName) {
        if (kitName == null) {
            MessageUtils.sendMessage(player, "commands.ffasetup.kit.create.usage");
            return;
        }

        kitName = kitName.trim();
        if (kitName.isEmpty()) {
            MessageUtils.sendMessage(player, "commands.ffasetup.kit.create.usage");
            return;
        }

        if (plugin.getKitManager().getKit(kitName) != null) {
            MessageUtils.sendMessage(player, "messages.kit-exists", "%kit%", kitName);
            return;
        }
        Kit kit = plugin.getKitManager().createKit(kitName);
        plugin.getKitManager().populateKitFromPlayer(kit, player);
        plugin.getKitManager().saveKits();
        MessageUtils.sendMessage(player, "commands.ffasetup.kit.create.success", "%kit%", kit.getName());
    }
    
    private void handleRegen(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", arenaName);
            return;
        }
        
        plugin.getRegenManager().regenArena(arena);
        MessageUtils.sendMessage(player, "commands.ffa.regen.started", "%arena%", arenaName);
    }

    private void handleSetSpawn(Player player) {
        plugin.setFfaSpawn(player.getLocation().clone());
        MessageUtils.sendMessage(player, "commands.ffa.setspawn.success");
    }

    private void applyJoinProtection(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("join-protection.enabled", true)) {
            return;
        }

        int durationSeconds = config.getInt("join-protection.duration", 10);
        if (durationSeconds <= 0) {
            return;
        }

        int duration = durationSeconds * 20; // Convert to ticks
        
        player.setInvulnerable(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 255, false, false));
        
        // Remove protection after duration
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeJoinProtection(player);
            protectionTasks.remove(player.getUniqueId());
        }, duration);
        
        protectionTasks.put(player.getUniqueId(), task);
    }
    
    private void removeJoinProtection(Player player) {
        player.setInvulnerable(false);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        
        BukkitTask task = protectionTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    private void sendHelp(Player player) {
        MessageUtils.sendMessage(player, "commands.ffa.help");
    }
}

