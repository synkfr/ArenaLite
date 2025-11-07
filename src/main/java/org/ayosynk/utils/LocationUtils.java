package org.ayosynk.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {
    
    public static String serializeLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + 
               loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," +
               loc.getYaw() + "," + loc.getPitch();
    }
    
    public static Location deserializeLocation(String str, World defaultWorld) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        
        String[] parts = str.split(",");
        if (parts.length < 3) {
            return null;
        }
        
        World world = defaultWorld;
        if (parts.length >= 4) {
            world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                world = defaultWorld;
            }
        }
        
        if (world == null) {
            return null;
        }
        
        try {
            double x = Double.parseDouble(parts[parts.length >= 4 ? 1 : 0]);
            double y = Double.parseDouble(parts[parts.length >= 4 ? 2 : 1]);
            double z = Double.parseDouble(parts[parts.length >= 4 ? 3 : 2]);
            
            float yaw = 0;
            float pitch = 0;
            
            if (parts.length >= 5) {
                yaw = Float.parseFloat(parts[parts.length >= 4 ? 4 : 3]);
            }
            if (parts.length >= 6) {
                pitch = Float.parseFloat(parts[parts.length >= 4 ? 5 : 4]);
            }
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

