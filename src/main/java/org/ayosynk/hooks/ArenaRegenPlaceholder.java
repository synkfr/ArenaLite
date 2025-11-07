package org.ayosynk.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.bukkit.entity.Player;

public class ArenaRegenPlaceholder extends PlaceholderExpansion {

    private final ArenaLite plugin;

    public ArenaRegenPlaceholder(ArenaLite plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "arenaregen";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        String lowerId = identifier.toLowerCase();

        if (lowerId.startsWith("next_")) {
            String arenaName = identifier.substring("next_".length());
            if (arenaName.isEmpty()) {
                return "N/A";
            }
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena == null) {
                for (String name : plugin.getArenaManager().getArenas().keySet()) {
                    if (name.equalsIgnoreCase(arenaName)) {
                        arena = plugin.getArenaManager().getArenas().get(name);
                        break;
                    }
                }
            }
            if (arena == null || !arena.isRegenEnabled()) {
                return "N/A";
            }
            long time = plugin.getRegenManager().getTimeUntilNextRegen(arena);
            if (time < 0) {
                return "N/A";
            }
            return formatTime(time);
        }

        return null;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
