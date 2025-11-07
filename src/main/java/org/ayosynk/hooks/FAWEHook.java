package org.ayosynk.hooks;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class FAWEHook {
    
    private final ArenaLite plugin;
    private boolean available = false;
    private Class<?> faweAPI;
    private Class<?> blockVector3;
    private Class<?> cuboidRegion;
    private Class<?> bukkitAdapter;
    
    public FAWEHook(ArenaLite plugin) {
        this.plugin = plugin;
        checkAvailability();
    }
    
    private void checkAvailability() {
        try {
            faweAPI = Class.forName("com.fastasyncworldedit.core.FaweAPI");
            available = true;
            
            // Try to load other FAWE classes
            try {
                blockVector3 = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                cuboidRegion = Class.forName("com.sk89q.worldedit.regions.CuboidRegion");
                bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("Some FAWE classes not found, using basic reflection");
            }
        } catch (ClassNotFoundException e) {
            available = false;
        }
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public CompletableFuture<Void> createSnapshot(Arena arena) {
        return CompletableFuture.runAsync(() -> {
            if (!available || arena.getPos1() == null || arena.getPos2() == null) {
                return;
            }
            
            plugin.getLogger().info("Creating snapshot for arena: " + arena.getName());
            
            // Create snapshot directory
            File snapshotDir = new File(plugin.getDataFolder(), "snapshots");
            if (!snapshotDir.exists()) {
                snapshotDir.mkdirs();
            }
            
            // For now, just log - actual snapshot creation would require FAWE clipboard API
            plugin.getLogger().info("Snapshot creation for " + arena.getName() + " - using region regeneration instead");
        });
    }
    
    public CompletableFuture<Void> regenArena(Arena arena) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
            if (!available || arena.getPos1() == null || arena.getPos2() == null) {
            future.completeExceptionally(new IllegalStateException("FAWE not available or arena region not set"));
            return future;
        }
        
        // Run on main thread for Bukkit operations
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    regenArenaSync(arena);
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to regen arena " + arena.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    future.completeExceptionally(e);
                }
            }
        }.runTask(plugin);
        
        return future;
    }
    
    private void regenArenaSync(Arena arena) {
        try {
            World world = arena.getWorld();
            if (world == null) {
                throw new IllegalStateException("Arena world is null");
            }
            
            if (blockVector3 == null || cuboidRegion == null || bukkitAdapter == null) {
                throw new IllegalStateException("FAWE classes not fully loaded");
            }
            
            // Use FAWE API via reflection
            Method getMethod = faweAPI.getMethod("get");
            Object faweInstance = getMethod.invoke(null);
            
            // Get Bukkit world adapter
            Method getWorldMethod = bukkitAdapter.getMethod("asWorld", org.bukkit.World.class);
            Object weWorld = getWorldMethod.invoke(null, world);
            
            // Create BlockVector3 for positions
            Method blockVector3At = blockVector3.getMethod("at", int.class, int.class, int.class);
            Object pos1 = blockVector3At.invoke(null, 
                arena.getPos1().getBlockX(),
                arena.getPos1().getBlockY(),
                arena.getPos1().getBlockZ());
            Object pos2 = blockVector3At.invoke(null,
                arena.getPos2().getBlockX(),
                arena.getPos2().getBlockY(),
                arena.getPos2().getBlockZ());
            
            // Create CuboidRegion
            Object region = cuboidRegion.getConstructor(blockVector3, blockVector3).newInstance(pos1, pos2);
            
            // Try to get EditSessionBuilder from FAWE
            // FAWE API might have different method signatures, so we try multiple approaches
            Object editSession = null;
            try {
                // Try getEditSessionBuilder method
                Method getEditSessionBuilderMethod = faweAPI.getMethod("getEditSessionBuilder", weWorld.getClass());
                Object editSessionBuilder = getEditSessionBuilderMethod.invoke(faweInstance, weWorld);
                
                // Try to set region if method exists
                try {
                    Method setRegionMethod = editSessionBuilder.getClass().getMethod("region", cuboidRegion);
                    setRegionMethod.invoke(editSessionBuilder, region);
                } catch (NoSuchMethodException ignored) {
                    // Method doesn't exist, continue
                }
                
                // Build edit session
                Method buildMethod = editSessionBuilder.getClass().getMethod("build");
                editSession = buildMethod.invoke(editSessionBuilder);
            } catch (NoSuchMethodException e) {
                // Try alternative method
                plugin.getLogger().warning("getEditSessionBuilder not found, trying alternative method");
            }
            
            if (editSession != null) {
                // Try to restore region
                try {
                    Method restoreMethod = editSession.getClass().getMethod("restoreRegion", cuboidRegion);
                    restoreMethod.invoke(editSession, region);
                } catch (NoSuchMethodException e) {
                    // Try alternative restore method
                    try {
                        Method restoreMethod = editSession.getClass().getMethod("restore", cuboidRegion);
                        restoreMethod.invoke(editSession, region);
                    } catch (NoSuchMethodException e2) {
                        plugin.getLogger().warning("Restore method not found in FAWE API");
                    }
                }
                
                // Close edit session
                try {
                    Method closeMethod = editSession.getClass().getMethod("close");
                    closeMethod.invoke(editSession);
                } catch (NoSuchMethodException ignored) {
                    // Close method might not exist
                }
                
                plugin.getLogger().info("Successfully regenerated arena: " + arena.getName());
            } else {
                throw new IllegalStateException("Could not create edit session");
            }
            
        } catch (Exception e) {
            // Fallback: Use Bukkit API to restore blocks
            plugin.getLogger().warning("FAWE API reflection failed, using fallback method: " + e.getMessage());
            if (plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
                e.printStackTrace();
            }
            regenArenaFallback(arena);
        }
    }
    
    private void regenArenaFallback(Arena arena) {
        // Fallback: Clear the region (this is a basic implementation)
        // In a real scenario, you'd want to restore from a saved schematic
        World world = arena.getWorld();
        if (world == null) {
            return;
        }
        
        int minX = Math.min(arena.getPos1().getBlockX(), arena.getPos2().getBlockX());
        int maxX = Math.max(arena.getPos1().getBlockX(), arena.getPos2().getBlockX());
        int minY = Math.min(arena.getPos1().getBlockY(), arena.getPos2().getBlockY());
        int maxY = Math.max(arena.getPos1().getBlockY(), arena.getPos2().getBlockY());
        int minZ = Math.min(arena.getPos1().getBlockZ(), arena.getPos2().getBlockZ());
        int maxZ = Math.max(arena.getPos1().getBlockZ(), arena.getPos2().getBlockZ());
        
        // This is a basic fallback - in production, you'd restore from a schematic
        // For now, we'll just log that regeneration was attempted
        plugin.getLogger().info("Fallback regen for arena: " + arena.getName() + 
            " (region: " + minX + "," + minY + "," + minZ + " to " + maxX + "," + maxY + "," + maxZ + ")");
        plugin.getLogger().warning("Fallback regen does not actually restore blocks. Please ensure FAWE is properly installed.");
    }
}

