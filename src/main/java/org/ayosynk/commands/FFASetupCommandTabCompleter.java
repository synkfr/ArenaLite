package org.ayosynk.commands;

import org.ayosynk.ArenaLite;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FFASetupCommandTabCompleter implements TabCompleter {
    
    private final ArenaLite plugin;
    
    public FFASetupCommandTabCompleter(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("arenalite.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("wand", "create", "delete", "setspawn", "setregen", "build", "list", "kit", "kitlink"));
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        switch (args[0].toLowerCase()) {
            case "setspawn":
            case "setregen":
            case "delete":
            case "build":
                if (args.length == 2) {
                    return plugin.getArenaManager().getArenaNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 3 && args[0].equalsIgnoreCase("setregen")) {
                    return Arrays.asList("true", "false").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 3 && args[0].equalsIgnoreCase("build")) {
                    return Arrays.asList("true", "false").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
                
            case "kit":
                if (args.length == 2) {
                    return Arrays.asList("create", "delete").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("delete")) {
                        return plugin.getKitManager().getKitNames().stream()
                            .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    }
                }
                break;
                
            case "kitlink":
                if (args.length == 2) {
                    return plugin.getKitManager().getKitNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 3) {
                    return plugin.getArenaManager().getArenaNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
        }
        
        return new ArrayList<>();
    }
}

