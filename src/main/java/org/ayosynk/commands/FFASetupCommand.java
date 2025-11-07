package org.ayosynk.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.ayosynk.models.Kit;
import org.ayosynk.setup.SetupSession;
import org.ayosynk.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FFASetupCommand implements CommandExecutor {
    
    private final ArenaLite plugin;
    private final Map<UUID, SetupSession> setupSessions = new HashMap<>();
    
    public FFASetupCommand(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("arenalite.admin")) {
            MessageUtils.sendMessage(player, "messages.no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "wand":
                handleWand(player);
                break;
                
            case "create":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.create.usage");
                    return true;
                }
                handleCreate(player, args[1]);
                break;

            case "delete":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.delete.usage");
                    return true;
                }
                handleDelete(player, args[1]);
                break;
                
            case "setspawn":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.setspawn.usage");
                    return true;
                }
                handleSetSpawn(player, args[1]);
                break;
                
            case "setregen":
                if (args.length < 3) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.setregen.usage");
                    return true;
                }
                handleSetRegen(player, args[1], args[2]);
                break;

            case "build":
                if (args.length < 3) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.build.usage");
                    return true;
                }
                handleBuild(player, args[1], args[2]);
                break;

            case "list":
                handleList(player);
                break;
                
            case "kit":
                if (args.length < 3) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.kit.usage");
                    return true;
                }
                if (args[1].equalsIgnoreCase("create")) {
                    handleKitCreate(player, args[2]);
                } else if (args[1].equalsIgnoreCase("delete")) {
                    handleKitDelete(player, args[2]);
                }
                break;
                
            case "kitlink":
                if (args.length < 3) {
                    MessageUtils.sendMessage(player, "commands.ffasetup.kitlink.usage");
                    return true;
                }
                handleKitLink(player, args[1], args[2]);
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleWand(Player player) {
        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            Component name = LegacyComponentSerializer.legacyAmpersand().deserialize("&6Arena Selection Wand");
            meta.displayName(name);
        }
        wand.setItemMeta(meta);
        
        player.getInventory().addItem(wand);
        MessageUtils.sendMessage(player, "commands.ffasetup.wand.received");
    }
    
    private void handleCreate(Player player, String arenaName) {
        if (plugin.getArenaManager().getArena(arenaName) != null) {
            MessageUtils.sendMessage(player, "messages.arena-exists", "%arena%", arenaName);
            return;
        }
        
        // Check if player has a setup session with corners selected
        SetupSession existingSession = setupSessions.get(player.getUniqueId());
        if (existingSession != null && existingSession.getPos1() != null && existingSession.getPos2() != null) {
            // Use existing session corners
            Arena arena = plugin.getArenaManager().createArena(arenaName);
            arena.setWorld(player.getWorld());
            arena.setPos1(existingSession.getPos1());
            arena.setPos2(existingSession.getPos2());
            plugin.getArenaManager().saveArenas();
            setupSessions.remove(player.getUniqueId());
            MessageUtils.sendMessage(player, "commands.ffasetup.create.success", "%arena%", arenaName);
            MessageUtils.sendMessage(player, "commands.ffasetup.create.region-set");
            return;
        }
        
        // Create new arena and session
        Arena arena = plugin.getArenaManager().createArena(arenaName);
        arena.setWorld(player.getWorld());
        arena.setBuildEnabled(plugin.getConfig().getBoolean("arenas.default-build-enabled", true));
        if (plugin.getConfig().getBoolean("arenas.default-regen-enabled", false)) {
            arena.setRegenEnabled(true);
            arena.setRegenInterval(Math.max(1, plugin.getConfig().getInt("arenas.default-regen-interval", 300)));
        }
        
        SetupSession session = new SetupSession(arena);
        setupSessions.put(player.getUniqueId(), session);
        
        MessageUtils.sendMessage(player, "commands.ffasetup.create.success", "%arena%", arenaName);
        MessageUtils.sendMessage(player, "commands.ffasetup.create.instructions");
        MessageUtils.sendMessage(player, "commands.ffasetup.create.select-corners");
    }
    
    private void handleSetSpawn(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", arenaName);
            return;
        }
        
        // Validate arena has region set
        if (arena.getPos1() == null || arena.getPos2() == null) {
            MessageUtils.sendMessage(player, "commands.ffasetup.setspawn.no-region", "%arena%", arenaName);
            MessageUtils.sendMessage(player, "commands.ffasetup.setspawn.select-corners");
            return;
        }
        
        // Validate spawn is within region
        if (!arena.isInRegion(player.getLocation())) {
            MessageUtils.sendMessage(player, "commands.ffasetup.setspawn.outside-region", "%arena%", arenaName);
            return;
        }
        
        arena.setSpawn(player.getLocation());
        plugin.getArenaManager().saveArenas();
        
        MessageUtils.sendMessage(player, "commands.ffasetup.setspawn.success", "%arena%", arenaName);
    }

    private void handleDelete(Player player, String arenaName) {
        String targetArena = arenaName != null ? arenaName.trim() : null;
        Arena arena = plugin.getArenaManager().getArena(targetArena);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", targetArena);
            return;
        }

        plugin.getArenaManager().deleteArena(targetArena);
        plugin.getArenaManager().saveArenas();
        plugin.getKitManager().unlinkArena(targetArena);
        plugin.getKitManager().saveKits();

        setupSessions.entrySet().removeIf(entry -> {
            SetupSession session = entry.getValue();
            return session != null && session.getArena() != null &&
                session.getArena().getName().equalsIgnoreCase(targetArena);
        });

        MessageUtils.sendMessage(player, "commands.ffasetup.delete.success", "%arena%", arena.getName());
    }

    private void handleBuild(Player player, String arenaName, String value) {
        String targetArena = arenaName != null ? arenaName.trim() : null;
        String targetValue = value != null ? value.trim() : null;
        Arena arena = plugin.getArenaManager().getArena(targetArena);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", targetArena);
            return;
        }

        if (targetValue == null || (!targetValue.equalsIgnoreCase("true") && !targetValue.equalsIgnoreCase("false"))) {
            MessageUtils.sendMessage(player, "commands.ffasetup.build.invalid-value");
            return;
        }

        boolean enabled = Boolean.parseBoolean(targetValue);
        String status = enabled ? "&aenabled" : "&cdisabled";
        arena.setBuildEnabled(enabled);
        plugin.getArenaManager().saveArenas();

        MessageUtils.sendMessage(player, "commands.ffasetup.build.success",
            "%arena%", arena.getName(),
            "%value%", status);
    }
    
    private void handleSetRegen(Player player, String arenaName, String enabled) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", arenaName);
            return;
        }
        
        // Validate arena has region and spawn set
        if (arena.getPos1() == null || arena.getPos2() == null) {
            MessageUtils.sendMessage(player, "commands.ffasetup.setregen.no-region", "%arena%", arenaName);
            return;
        }
        
        if (arena.getSpawn() == null) {
            MessageUtils.sendMessage(player, "commands.ffasetup.setregen.no-spawn", "%arena%", arenaName);
            return;
        }
        
        if (!enabled.equalsIgnoreCase("true") && !enabled.equalsIgnoreCase("false")) {
            MessageUtils.sendMessage(player, "commands.ffasetup.setregen.invalid-value");
            return;
        }
        
        boolean regen = enabled.equalsIgnoreCase("true");
        arena.setRegenEnabled(regen);
        
        if (regen) {
            int interval = plugin.getConfig().getInt("regen.default-interval", 300);
            arena.setRegenInterval(interval);
            // Capture snapshot immediately so regen can work without FAWE
            plugin.getSnapshotManager().captureSnapshot(arena).whenComplete((v, ex) -> {
                if (ex != null) {
                    plugin.getLogger().warning("Snapshot capture failed for arena " + arena.getName() + ": " + ex.getMessage());
                }
            });
        }
        
        plugin.getArenaManager().saveArenas();
        MessageUtils.sendMessage(player, "commands.ffasetup.setregen.success", 
            "%arena%", arenaName, "%enabled%", String.valueOf(regen));
    }

    private void handleList(Player player) {
        Map<String, Arena> arenas = plugin.getArenaManager().getArenas();
        if (arenas.isEmpty()) {
            MessageUtils.sendMessage(player, "commands.ffasetup.list.empty");
            return;
        }

        MessageUtils.sendMessage(player, "commands.ffasetup.list.header", "%count%", String.valueOf(arenas.size()));
        for (Arena arena : arenas.values()) {
            String spawnStatus = arena.getSpawn() != null ? "&aYes" : "&cNo";
            String regenStatus = arena.isRegenEnabled() ? "&a" + arena.getRegenInterval() + "s" : "&cDisabled";
            String buildStatus = arena.isBuildEnabled() ? "&aEnabled" : "&cDisabled";
            MessageUtils.sendMessage(player, "commands.ffasetup.list.entry",
                "%arena%", arena.getName(),
                "%spawn%", spawnStatus,
                "%regen%", regenStatus,
                "%build%", buildStatus);
        }
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
        
        // Save current inventory
        plugin.getKitManager().populateKitFromPlayer(kit, player);
        
        plugin.getKitManager().saveKits();
        
        MessageUtils.sendMessage(player, "commands.ffasetup.kit.create.success", "%kit%", kit.getName());
    }
    
    private void handleKitDelete(Player player, String kitName) {
        if (kitName != null) {
            kitName = kitName.trim();
        }
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "messages.kit-not-found", "%kit%", kitName);
            return;
        }
        
        plugin.getKitManager().deleteKit(kitName);
        plugin.getKitManager().saveKits();
        
        MessageUtils.sendMessage(player, "commands.ffasetup.kit.delete.success", "%kit%", kit.getName());
    }
    
    private void handleKitLink(Player player, String kitName, String arenaName) {
        if (kitName != null) {
            kitName = kitName.trim();
        }
        if (arenaName != null) {
            arenaName = arenaName.trim();
        }
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "messages.kit-not-found", "%kit%", kitName);
            return;
        }
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtils.sendMessage(player, "messages.arena-not-found", "%arena%", arenaName);
            return;
        }
        
        kit.setLinkedArena(arena.getName());
        plugin.getKitManager().saveKits();
        
        MessageUtils.sendMessage(player, "commands.ffasetup.kitlink.success", 
            "%kit%", kit.getName(), "%arena%", arena.getName());
    }
    
    public SetupSession getSetupSession(Player player) {
        return setupSessions.get(player.getUniqueId());
    }
    
    private void sendHelp(Player player) {
        MessageUtils.sendMessage(player, "commands.ffasetup.help");
    }

}

