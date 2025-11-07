package org.ayosynk.managers;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SnapshotManager {

    private final ArenaLite plugin;

    public SnapshotManager(ArenaLite plugin) {
        this.plugin = plugin;
    }

    public File getSnapshotFile(Arena arena) {
        File dir = new File(plugin.getDataFolder(), "snapshots");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, arena.getName() + ".bin");
    }

    public boolean hasSnapshot(Arena arena) {
        return getSnapshotFile(arena).exists();
    }

    public CompletableFuture<Void> captureSnapshot(Arena arena) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (arena.getWorld() == null || arena.getPos1() == null || arena.getPos2() == null) {
                    throw new IllegalStateException("Arena world/region not set");
                }
                World world = arena.getWorld();
                int minX = Math.min(arena.getPos1().getBlockX(), arena.getPos2().getBlockX());
                int maxX = Math.max(arena.getPos1().getBlockX(), arena.getPos2().getBlockX());
                int minY = Math.min(arena.getPos1().getBlockY(), arena.getPos2().getBlockY());
                int maxY = Math.max(arena.getPos1().getBlockY(), arena.getPos2().getBlockY());
                int minZ = Math.min(arena.getPos1().getBlockZ(), arena.getPos2().getBlockZ());
                int maxZ = Math.max(arena.getPos1().getBlockZ(), arena.getPos2().getBlockZ());

                File target = getSnapshotFile(arena);
                try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(target))))) {
                    out.writeInt(minX);
                    out.writeInt(minY);
                    out.writeInt(minZ);
                    out.writeInt(maxX - minX + 1);
                    out.writeInt(maxY - minY + 1);
                    out.writeInt(maxZ - minZ + 1);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                Block block = world.getBlockAt(x, y, z);
                                Material mat = block.getType();
                                boolean isAir = mat.isAir();
                                out.writeBoolean(isAir);
                                if (!isAir) {
                                    out.writeUTF(mat.name());
                                    out.writeUTF(block.getBlockData().getAsString());
                                }
                            }
                        }
                    }
                }
                future.complete(null);
            } catch (Throwable t) {
                plugin.getLogger().severe("Failed to capture snapshot for arena " + arena.getName() + ": " + t.getMessage());
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public CompletableFuture<Void> restoreSnapshot(Arena arena) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!hasSnapshot(arena)) {
            future.completeExceptionally(new FileNotFoundException("Snapshot not found"));
            return future;
        }
        // Load snapshot meta and enqueue tasks (async), then apply on main thread in throttled batches
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(getSnapshotFile(arena)))))) {
                World world = arena.getWorld();
                if (world == null) throw new IllegalStateException("Arena world is null");

                int baseX = in.readInt();
                int baseY = in.readInt();
                int baseZ = in.readInt();
                int sizeX = in.readInt();
                int sizeY = in.readInt();
                int sizeZ = in.readInt();

                Queue<BlockChange> queue = new ArrayDeque<>();
                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        for (int z = 0; z < sizeZ; z++) {
                            boolean isAir = in.readBoolean();
                            int wx = baseX + x;
                            int wy = baseY + y;
                            int wz = baseZ + z;
                            if (isAir) {
                                queue.add(new BlockChange(wx, wy, wz, null, null));
                            } else {
                                String matName = in.readUTF();
                                String dataStr = in.readUTF();
                                queue.add(new BlockChange(wx, wy, wz, matName, dataStr));
                            }
                        }
                    }
                }

                int blocksPerTick = Math.max(1, plugin.getConfig().getInt("regen.blocks-per-tick", 500));
                int[] processed = {0};

                Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                    int i = 0;
                    while (i < blocksPerTick && !queue.isEmpty()) {
                        BlockChange bc = queue.poll();
                        i++;
                        processed[0]++;
                        int chunkX = bc.x >> 4;
                        int chunkZ = bc.z >> 4;
                        if (!world.isChunkLoaded(chunkX, chunkZ)) {
                            world.loadChunk(chunkX, chunkZ, true);
                        }
                        Block block = world.getBlockAt(bc.x, bc.y, bc.z);
                        if (bc.materialName == null) {
                            block.setType(Material.AIR, false);
                        } else {
                            Material m;
                            try {
                                m = Material.valueOf(bc.materialName);
                            } catch (IllegalArgumentException ex) {
                                m = Material.AIR;
                            }
                            BlockData data = Bukkit.createBlockData(bc.blockDataStr);
                            block.setBlockData(data, false);
                            if (block.getType() != m) {
                                block.setType(m, false);
                            }
                        }
                    }

                    if (queue.isEmpty()) {
                        task.cancel();
                        future.complete(null);
                    }
                }, 1L, 1L);

            } catch (Throwable t) {
                plugin.getLogger().severe("Failed to restore snapshot for arena " + arena.getName() + ": " + t.getMessage());
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private static class BlockChange {
        final int x, y, z;
        final String materialName;
        final String blockDataStr;
        BlockChange(int x, int y, int z, String materialName, String blockDataStr) {
            this.x = x; this.y = y; this.z = z;
            this.materialName = materialName;
            this.blockDataStr = blockDataStr;
        }
    }
}
