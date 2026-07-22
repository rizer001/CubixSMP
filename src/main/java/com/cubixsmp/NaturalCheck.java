package com.cubixsmp;

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
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Идём вниз от сломанного блока через брёвна И воздушные карманы
     *       (AIR-карман = ранее сломанное бревно в этой же колонне при валке
     *       снизу вверх). Ищем натуральную почву/камень как основание дерева.</li>
     *   <li>Когда нашли основание — поднимаемся обратно по колонне вверх и
     *       проверяем каждое бревно на соседство с листвой. Если у какого-то
     *       бревна колонны рядом есть листва — это настоящее дерево. Если
     *       листвы нигде нет — это скорее всего построенная игроком колонна
     *       брёвен, и XP давать не надо.</li>
     * </ol>
     * <p>
     * Баг #2 (фикс ниже): ранее валка деревьев работала только сверху вниз.
     * При обратной валке (снизу вверх) снизу уже был AIR, а старый код
     * воспринимал AIR как конец колонны и возвращал false — XP не давались.
     */
    // Максимальная высота обхода колонны брёвен. 64 — с запасом для
    // гигантских 2x2 jungle trees (до ~50 блоков в ваниле).
    private static final int MAX_COLUMN_WALK = 64;

    public boolean isNaturalLog(Block block) {
        Block current = block;
        for (int i = 0; i < MAX_COLUMN_WALK; i++) {
            Block below = current.getRelative(BlockFace.DOWN);
            Material belowType = below.getType();

            if (isNaturalSoil(belowType) || isNaturalStone(belowType)) {
                // Нашли натуральное основание. Теперь проверим, что над ним
                // действительно есть листва (иначе это просто столб брёвен).
                return hasLeavesInLogColumn(block);
            }

            // AIR-карман = ранее сломанное бревно в этой же колонке — пропускаем
            if (belowType == Material.AIR || belowType == Material.CAVE_AIR) {
                current = below;
                continue;
            }

            // Если снизу не бревно и не воздух — это не колонна дерева
            if (!isLogLike(belowType)) {
                return false;
            }

            current = below;
        }
        return false;
    }

    /**
     * Walk up the column from the broken block, checking each block (logs and
     * air gaps from already-broken logs above) for adjacent leaves or wart blocks.
     * Returns true if any block in the walked-up portion of the column has
     * leaves nearby — this confirms a real tree.
     * <p>
     * Работает корректно при любом порядке валки: верхняя листва обычно
     * сохраняется ещё долго после того, как все брёвна под ней сломаны.
     */
    private boolean hasLeavesInLogColumn(Block start) {
        Block current = start;
        for (int i = 0; i < MAX_COLUMN_WALK; i++) {
            if (hasLeavesAdjacent(current)) return true;

            Block above = current.getRelative(BlockFace.UP);
            Material aboveType = above.getType();

            // Останавливаемся только если над нами не бревно и не воздух
            if (!isLogLike(aboveType) && aboveType != Material.AIR && aboveType != Material.CAVE_AIR) {
                break;
            }
            current = above;
        }
        return false;
    }

    private boolean isLogLike(Material mat) {
        String name = mat.name();
        // MANGROVE_ROOTS — часть колонны мангрового дерева (между нижним
        // MANGROVE_LOG и почвой/водой); без этого мангры не дают XP
        return name.endsWith("_LOG") || name.endsWith("_WOOD")
                || name.endsWith("_STEM") || name.endsWith("_HYPHAE")
                || name.equals("MANGROVE_ROOTS");
    }

    private boolean hasLeavesAdjacent(Block block) {
        for (BlockFace face : new BlockFace[]{
                BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST
        }) {
            Material adj = block.getRelative(face).getType();
            if (adj.name().endsWith("_LEAVES") || adj.name().endsWith("_WART_BLOCK")) {
                return true;
            }
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
