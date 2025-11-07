package org.ayosynk.models;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kit {
    
    private final String name;
    private String linkedArena;
    private List<ItemStack> contents;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack offhand;
    
    public Kit(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLinkedArena() {
        return linkedArena;
    }
    
    public void setLinkedArena(String linkedArena) {
        this.linkedArena = linkedArena;
    }
    
    public List<ItemStack> getContents() {
        return contents;
    }
    
    public void setContents(List<ItemStack> contents) {
        this.contents = contents;
    }
    
    public ItemStack getHelmet() {
        return helmet;
    }
    
    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }
    
    public ItemStack getChestplate() {
        return chestplate;
    }
    
    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }
    
    public ItemStack getLeggings() {
        return leggings;
    }
    
    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }
    
    public ItemStack getBoots() {
        return boots;
    }
    
    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public ItemStack getOffhand() {
        return offhand;
    }

    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        if (linkedArena != null) {
            map.put("linked-arena", linkedArena);
        }
        // Serialization handled by KitManager
        return map;
    }
}

