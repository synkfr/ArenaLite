package org.ayosynk.models;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class Arena {
    
    private final String name;
    private World world;
    private Location spawn;
    private Location pos1;
    private Location pos2;
    private boolean regenEnabled;
    private int regenInterval; // in seconds
    private long lastRegen;
    private boolean buildEnabled;
    
    public Arena(String name) {
        this.name = name;
        this.regenEnabled = false;
        this.regenInterval = 300;
        this.lastRegen = System.currentTimeMillis();
        this.buildEnabled = true;
    }
    
    public String getName() {
        return name;
    }
    
    public World getWorld() {
        return world;
    }
    
    public void setWorld(World world) {
        this.world = world;
    }
    
    public Location getSpawn() {
        return spawn;
    }
    
    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }
    
    public Location getPos1() {
        return pos1;
    }
    
    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }
    
    public Location getPos2() {
        return pos2;
    }
    
    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }
    
    public boolean isRegenEnabled() {
        return regenEnabled;
    }
    
    public void setRegenEnabled(boolean regenEnabled) {
        this.regenEnabled = regenEnabled;
    }
    
    public int getRegenInterval() {
        return regenInterval;
    }
    
    public void setRegenInterval(int regenInterval) {
        this.regenInterval = regenInterval;
    }
    
    public long getLastRegen() {
        return lastRegen;
    }
    
    public void setLastRegen(long lastRegen) {
        this.lastRegen = lastRegen;
    }

    public boolean isBuildEnabled() {
        return buildEnabled;
    }

    public void setBuildEnabled(boolean buildEnabled) {
        this.buildEnabled = buildEnabled;
    }
    
    public boolean isInRegion(Location loc) {
        if (pos1 == null || pos2 == null || world == null) {
            return false;
        }
        
        if (!loc.getWorld().equals(world)) {
            return false;
        }
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        if (world != null) map.put("world", world.getName());
        if (spawn != null) {
            map.put("spawn", spawn.getX() + "," + spawn.getY() + "," + spawn.getZ() + "," + spawn.getYaw() + "," + spawn.getPitch());
        }
        if (pos1 != null) {
            map.put("pos1", pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ());
        }
        if (pos2 != null) {
            map.put("pos2", pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ());
        }
        map.put("regen", Map.of(
            "enabled", regenEnabled,
            "interval", regenInterval
        ));
        map.put("build-enabled", buildEnabled);
        return map;
    }
}

