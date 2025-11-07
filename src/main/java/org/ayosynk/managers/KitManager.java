package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Kit;
import org.ayosynk.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class KitManager {
    
    private final ArenaLite plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private static final int STORAGE_SIZE = 36;
    private final Map<UUID, PreviewSession> previewSessions = new HashMap<>();
    
    public KitManager(ArenaLite plugin) {
        this.plugin = plugin;
    }
    
    public void loadKits() {
        kits.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("kits");
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        
        if (kitsSection == null) {
            return;
        }
        
        for (String name : kitsSection.getKeys(false)) {
            Kit kit = new Kit(name);
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(name);
            
            if (kitSection == null) continue;
            
            kit.setLinkedArena(kitSection.getString("linked-arena"));
            
            List<ItemStack> contents = new ArrayList<>(Collections.nCopies(STORAGE_SIZE, null));
            ConfigurationSection contentsSection = kitSection.getConfigurationSection("contents");
            if (contentsSection != null) {
                for (String key : contentsSection.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        if (slot >= 0 && slot < STORAGE_SIZE) {
                            String itemData = contentsSection.getString(key);
                            ItemStack item = ItemUtils.deserializeItem(itemData);
                            if (item != null) {
                                contents.set(slot, item);
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                // Legacy string list support
                List<String> contentsList = kitSection.getStringList("contents");
                for (int i = 0; i < contentsList.size() && i < STORAGE_SIZE; i++) {
                    ItemStack item = ItemUtils.deserializeItem(contentsList.get(i));
                    if (item != null) {
                        contents.set(i, item);
                    }
                }
            }
            kit.setContents(contents);
            
            // Load armor
            ConfigurationSection armorSection = kitSection.getConfigurationSection("armor");
            if (armorSection != null) {
                String helmetStr = armorSection.getString("helmet");
                String chestplateStr = armorSection.getString("chestplate");
                String leggingsStr = armorSection.getString("leggings");
                String bootsStr = armorSection.getString("boots");
                String offhandStr = armorSection.getString("offhand");
                
                if (helmetStr != null) kit.setHelmet(ItemUtils.deserializeItem(helmetStr));
                if (chestplateStr != null) kit.setChestplate(ItemUtils.deserializeItem(chestplateStr));
                if (leggingsStr != null) kit.setLeggings(ItemUtils.deserializeItem(leggingsStr));
                if (bootsStr != null) kit.setBoots(ItemUtils.deserializeItem(bootsStr));
                if (offhandStr != null) kit.setOffhand(ItemUtils.deserializeItem(offhandStr));
            }
            
            kits.put(name.toLowerCase(Locale.ROOT), kit);
        }
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits!");
    }
    
    public void saveKits() {
        FileConfiguration config = plugin.getConfigManager().getConfig("kits");
        config.set("kits", null);
        
        for (Kit kit : kits.values()) {
            String path = "kits." + kit.getName();
            
            config.set(path + ".linked-arena", kit.getLinkedArena());

            // Save contents preserving slots
            List<ItemStack> contents = kit.getContents();
            if (contents != null) {
                for (int slot = 0; slot < contents.size(); slot++) {
                    ItemStack item = contents.get(slot);
                    String serialized = ItemUtils.serializeItem(item);
                    if (serialized != null) {
                        config.set(path + ".contents." + slot, serialized);
                    }
                }
            }

            // Save armor & offhand
            config.set(path + ".armor.helmet", ItemUtils.serializeItem(kit.getHelmet()));
            config.set(path + ".armor.chestplate", ItemUtils.serializeItem(kit.getChestplate()));
            config.set(path + ".armor.leggings", ItemUtils.serializeItem(kit.getLeggings()));
            config.set(path + ".armor.boots", ItemUtils.serializeItem(kit.getBoots()));
            config.set(path + ".armor.offhand", ItemUtils.serializeItem(kit.getOffhand()));
        }
        
        plugin.getConfigManager().saveConfig("kits");
    }
    
    public Kit getKit(String name) {
        if (name == null) {
            return null;
        }
        return kits.get(name.toLowerCase(Locale.ROOT));
    }
    
    public Kit createKit(String name) {
        Kit kit = new Kit(name);
        kits.put(name.toLowerCase(Locale.ROOT), kit);
        return kit;
    }
    
    public void deleteKit(String name) {
        if (name == null) {
            return;
        }
        kits.remove(name.toLowerCase(Locale.ROOT));
    }
    
    public Set<String> getKitNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Kit kit : kits.values()) {
            names.add(kit.getName());
        }
        return names;
    }
    
    public Map<String, Kit> getKits() {
        return kits;
    }

    public List<ItemStack> captureInventoryContents(ItemStack[] storageContents) {
        if (storageContents == null) {
            storageContents = new ItemStack[0];
        }
        List<ItemStack> contents = new ArrayList<>(STORAGE_SIZE);
        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack item = i < storageContents.length ? storageContents[i] : null;
            contents.add(item != null ? item.clone() : null);
        }
        return contents;
    }

    public void unlinkArena(String arenaName) {
        if (arenaName == null) {
            return;
        }
        for (Kit kit : kits.values()) {
            String linked = kit.getLinkedArena();
            if (linked != null && linked.equalsIgnoreCase(arenaName)) {
                kit.setLinkedArena(null);
            }
        }
    }

    public void populateKitFromPlayer(Kit kit, Player player) {
        PlayerInventory inventory = player.getInventory();
        kit.setContents(captureInventoryContents(inventory.getStorageContents()));
        kit.setHelmet(cloneItem(inventory.getHelmet()));
        kit.setChestplate(cloneItem(inventory.getChestplate()));
        kit.setLeggings(cloneItem(inventory.getLeggings()));
        kit.setBoots(cloneItem(inventory.getBoots()));
        kit.setOffhand(cloneItem(inventory.getItemInOffHand()));
    }

    public void applyKit(Player player, Kit kit) {
        if (kit == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(null);

        List<ItemStack> contents = kit.getContents();
        if (contents != null && !contents.isEmpty()) {
            ItemStack[] newContents = new ItemStack[inventory.getSize()];
            int limit = Math.min(newContents.length, contents.size());
            for (int i = 0; i < limit; i++) {
                newContents[i] = cloneItem(contents.get(i));
            }
            inventory.setContents(newContents);
        }

        inventory.setHelmet(cloneItem(kit.getHelmet()));
        inventory.setChestplate(cloneItem(kit.getChestplate()));
        inventory.setLeggings(cloneItem(kit.getLeggings()));
        inventory.setBoots(cloneItem(kit.getBoots()));
        inventory.setItemInOffHand(cloneItem(kit.getOffhand()));

        player.updateInventory();
    }

    private ItemStack cloneItem(ItemStack item) {
        return item != null ? item.clone() : null;
    }

    public void openPreview(Player player, Kit kit) {
        if (!plugin.getConfig().getBoolean("settings.kit-previews.enabled", true)) {
            return;
        }
        Component title = LegacyComponentSerializer.legacyAmpersand().deserialize("&8Kit Preview: &e" + kit.getName());
        Inventory inv = Bukkit.createInventory(null, 54, title);

        if (kit.getHelmet() != null) inv.setItem(0, cloneItem(kit.getHelmet()));
        if (kit.getChestplate() != null) inv.setItem(1, cloneItem(kit.getChestplate()));
        if (kit.getLeggings() != null) inv.setItem(2, cloneItem(kit.getLeggings()));
        if (kit.getBoots() != null) inv.setItem(3, cloneItem(kit.getBoots()));
        if (kit.getOffhand() != null) inv.setItem(4, cloneItem(kit.getOffhand()));

        if (kit.getContents() != null) {
            int slot = 9;
            for (ItemStack content : kit.getContents()) {
                if (content != null && slot < inv.getSize()) {
                    inv.setItem(slot++, cloneItem(content));
                }
            }
        }

        previewSessions.put(player.getUniqueId(), new PreviewSession(kit.getName(), inv));
        player.openInventory(inv);
    }

    public boolean isPreviewing(Player player) {
        return previewSessions.containsKey(player.getUniqueId());
    }

    public boolean isPreviewInventory(Player player, Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        PreviewSession session = previewSessions.get(player.getUniqueId());
        return session != null && session.inventory.equals(inventory);
    }

    public void clearPreview(Player player) {
        previewSessions.remove(player.getUniqueId());
    }

    public String getPreviewKitName(Player player) {
        PreviewSession session = previewSessions.get(player.getUniqueId());
        return session != null ? session.kitName : null;
    }

    private record PreviewSession(String kitName, Inventory inventory) {}
}

