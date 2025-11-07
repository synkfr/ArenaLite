package org.ayosynk.utils;

import org.ayosynk.ArenaLite;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.logging.Level;

public class ItemUtils {

    private ItemUtils() {
    }

    public static String serializeItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        try {
            byte[] data = item.clone().serializeAsBytes();
            return Base64.getEncoder().encodeToString(data);
        } catch (IllegalStateException ex) {
            if (ArenaLite.getInstance() != null) {
                ArenaLite.getInstance().getLogger().log(Level.SEVERE, "Failed to serialize item", ex);
            }
            return null;
        }
    }

    public static ItemStack deserializeItem(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        // Backwards compatibility: legacy MATERIAL:AMOUNT format
        if (!str.contains("=") && !str.contains(";") && !str.contains("\n")) {
            String[] parts = str.split(":");
            Material material = Material.matchMaterial(parts[0]);
            if (material == null) {
                return null;
            }
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            return new ItemStack(material, amount);
        }

        try {
            byte[] data = Base64.getDecoder().decode(str);
            return ItemStack.deserializeBytes(data);
        } catch (IllegalArgumentException ex) {
            if (ArenaLite.getInstance() != null) {
                ArenaLite.getInstance().getLogger().log(Level.SEVERE, "Failed to deserialize item", ex);
            }
            return null;
        }
    }
}

