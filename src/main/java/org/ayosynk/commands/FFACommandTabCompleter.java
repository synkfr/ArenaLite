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

public class FFACommandTabCompleter implements TabCompleter {
    
    private final ArenaLite plugin;
    
    public FFACommandTabCompleter(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("join", "leave", "kit"));
            if (player.hasPermission("arenalite.admin")) {
                completions.add("setspawn");
                completions.add("regen");
                completions.add("reload");
            }
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                if (args.length == 2) {
                    return plugin.getKitManager().getKitNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
                
            case "kit":
                if (args.length == 2) {
                    List<String> sub = new ArrayList<>();
                    if (plugin.getConfig().getBoolean("settings.kit-previews.enabled", true)) {
                        sub.add("preview");
                    }
                    if (player.hasPermission("arenalite.admin")) {
                        sub.add("create");
                    }
                    return sub.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 3 && args[1].equalsIgnoreCase("preview")) {
                    return plugin.getKitManager().getKitNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
                
            case "regen":
                if (args.length == 2 && player.hasPermission("arenalite.admin")) {
                    return plugin.getArenaManager().getArenaNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
        }
        
        return new ArrayList<>();
    }
}

