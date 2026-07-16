package com.cubixlevels;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;

/**
 * Utility to check if a block or mob is "natural" — spawned by world generation
 * or natural growth, not by a player or spawner.
 */
public class NaturalCheck {

    // ─── Reflection handle for Paper's getEntitySpawnReason ─────
    private static Method spawnReasonMethod;
    static {
        try {
            spawnReasonMethod = Entity.class.getMethod("getEntitySpawnReason");
        } catch (NoSuchMethodException e) {
            spawnReasonMethod = null;
        }
    }

    /**
     * Check if a block was naturally generated (not placed by a player).
     */
    public boolean isNaturalOre(Block block) {
        for (BlockFace face : new BlockFace[]{
                BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
                BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
        }) {
            Material relative = block.getRelative(face).getType();
            if (isNaturalStone(relative) || isDeepslate(relative)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a log was naturally generated (not player-placed).
     */
    public boolean isNaturalLog(Block block) {
        Block below = block.getRelative(BlockFace.DOWN);
        if (isNaturalSoil(below.getType()) || isNaturalStone(below.getType())) {
            for (BlockFace face : new BlockFace[]{
                    BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH,
                    BlockFace.EAST, BlockFace.WEST
            }) {
                if (block.getRelative(face).getType().name().endsWith("_LEAVES")
                        || block.getRelative(face).getType().name().endsWith("_WART_BLOCK")) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Check if a mob spawned naturally (not from a spawner, egg, command).
     * Uses Paper's getEntitySpawnReason() via reflection for compatibility.
     * Falls back to spawner-proximity check if reflection fails.
     */
    public boolean isNaturalMob(Entity entity) {
        // Try Paper's getEntitySpawnReason() via reflection
        if (spawnReasonMethod != null) {
            try {
                Object reason = spawnReasonMethod.invoke(entity);
                String name = reason.toString();
                return switch (name) {
                    case "NATURAL", "DEFAULT", "CHUNK_GEN", "STRUCTURE",
                         "BREEDING", "EGG", "MOUNT", "TRIGGERED",
                         "PATROL", "RAID", "REINFORCEMENTS", "NETHER_PORTAL",
                         "LIGHTNING", "DROWNED", "JOCKEY" -> true;
                    default -> false;
                };
            } catch (Exception ignored) {}
        }

        // Fallback: spawner-proximity check
        return !hasSpawnerNearby(entity);
    }

    private boolean hasSpawnerNearby(Entity entity) {
        var loc = entity.getLocation();
        EntityType entityType = entity.getType();
        var world = loc.getWorld();
        if (world == null) return false;

        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                var chunk = world.getChunkAt(cx + dx, cz + dz);
                if (!chunk.isLoaded()) continue;
                for (var state : chunk.getTileEntities()) {
                    if (state instanceof CreatureSpawner spawner) {
                        if (spawner.getSpawnedType() == entityType) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // ─── Block type helpers ───────────────────

    private boolean isNaturalStone(Material mat) {
        return mat == Material.STONE
                || mat == Material.ANDESITE
                || mat == Material.DIORITE
                || mat == Material.GRANITE
                || mat == Material.TUFF
                || mat == Material.CALCITE
                || mat == Material.DRIPSTONE_BLOCK
                || mat == Material.NETHERRACK
                || mat == Material.BLACKSTONE
                || mat == Material.BASALT
                || mat == Material.END_STONE;
    }

    private boolean isDeepslate(Material mat) {
        return mat == Material.DEEPSLATE || mat.name().endsWith("_DEEPSLATE");
    }

    private boolean isNaturalSoil(Material mat) {
        return mat == Material.GRASS_BLOCK
                || mat == Material.DIRT
                || mat == Material.COARSE_DIRT
                || mat == Material.ROOTED_DIRT
                || mat == Material.MOSS_BLOCK
                || mat == Material.MYCELIUM
                || mat == Material.PODZOL
                || mat == Material.SAND
                || mat == Material.RED_SAND
                || mat == Material.MUD
                || mat == Material.MUDDY_MANGROVE_ROOTS;
    }
}
