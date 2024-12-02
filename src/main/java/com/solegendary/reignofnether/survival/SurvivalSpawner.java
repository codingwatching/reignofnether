package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.monsters.PumpkinFarm;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.BlockRegistrar;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.*;

public class SurvivalSpawner {

    private static final int MAX_SPAWN_RANGE = 80;
    private static final int MIN_SPAWN_RANGE = 60;
    private static final int SAMPLE_POINTS_PER_BUILDING = 100;
    private static final int MAX_FAILED_BUILDINGS = 10;

    private static final int MIN_VALID_BUILDINGS = 5; // once we sort the buildings by distance to centroid, how many buildings do we pick to spawn around?
    private static final float PERCENT_VALID_BUILDINGS = 0.1f; // % of all buildings added to MIN_VALID_BUILDINGS

    private static final Random random = new Random();

    private static boolean isEnemyOwner(String ownerName) {
        return ENEMY_OWNER_NAMES.contains(ownerName);
    }

    public static int getModifiedPopCost(Unit unit) {
        return Math.max(1, unit.getPopCost() - 1);
    }

    public static void placeIceOrMagma(BlockPos bp, Level level) {
        BlockState bs = level.getBlockState(bp);
        BlockState bsToPlace;

        if (bs.getMaterial() == Material.LAVA)
            bsToPlace = BlockRegistrar.WALKABLE_MAGMA_BLOCK.get().defaultBlockState();
        else if (bs.getMaterial() == Material.WATER)
            bsToPlace = Blocks.FROSTED_ICE.defaultBlockState();
        else
            return;

        level.setBlockAndUpdate(bp, bsToPlace);

        List<BlockPos> bps = List.of(bp.north(), bp.east(), bp.south(), bp.west(),
                bp.north().east(),
                bp.south().west(),
                bp.north().east(),
                bp.south().west());

        // Frostwalker effect provided in LivingEntityMixin, but it only happens on changing block positions on the ground
        for (BlockPos pos : bps) {
            BlockState bsAdj = level.getBlockState(pos);
            if (bsAdj.getMaterial().isLiquid() ||
                    bsAdj.getMaterial() == Material.WATER_PLANT ||
                    bsAdj.getMaterial() == Material.REPLACEABLE_WATER_PLANT)
                level.setBlockAndUpdate(pos, bsToPlace);
        }
    }

    // spawn monsters evenly spread out across from all directions
    public static void spawnMonsterWave(ServerLevel level, Wave wave) {
        final int pop = wave.population * PlayerServerEvents.rtsPlayers.size();
        int remainingPop = wave.population * PlayerServerEvents.rtsPlayers.size();

        List<BlockPos> bps = SurvivalSpawner.getValidSpawnPoints(remainingPop, level, true);

        for (BlockPos bp : bps) {
            BlockState bs = level.getBlockState(bp);

            int tier = random.nextInt(wave.highestUnitTier) + 1;
            EntityType<? extends Mob> mobType = wave.getRandomUnitOfTier(Faction.MONSTERS, tier);

            bp = bp.above();

            ArrayList<Entity> entities = UnitServerEvents.spawnMobs(mobType, level,
                    mobType.getDescription().getString().contains("spider") ? bp.above().above(): bp.above(),
                    1, MONSTER_OWNER_NAME);

            for (Entity entity : entities) {
                placeIceOrMagma(bp, level);
                if (entity instanceof Unit unit)
                    remainingPop -= getModifiedPopCost(unit);
            }
            if (remainingPop <= 0)
                break;
        }
        if (remainingPop > 0) {
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + remainingPop + "/" + pop + " population worth of monster units");
        }
    }

    // spawn illagers from one direction
    public static void spawnIllagerWave(ServerLevel level, Wave wave) {
        final int pop = wave.population * PlayerServerEvents.rtsPlayers.size();
        int remainingPop = wave.population * PlayerServerEvents.rtsPlayers.size();
        List<BlockPos> spawnBps = SurvivalSpawner.getValidSpawnPoints(remainingPop, level, true);
        int spawnsThisDir = 0;
        int spawnUntilNextTurn = -2;

        if (!spawnBps.isEmpty()) {
            BlockPos bp = spawnBps.get(0).above();
            Direction dir = Direction.EAST;

            while (remainingPop > 0) {
                switch (dir) {
                    case NORTH -> {
                        bp = bp.north();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.EAST;
                        }
                    }
                    case EAST -> {
                        bp = bp.east();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.SOUTH;
                        }
                    }
                    case SOUTH -> {
                        bp = bp.south();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.WEST;
                        }
                    }
                    case WEST -> {
                        bp = bp.west();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.NORTH;
                        }
                    }
                }

                int tier = random.nextInt(wave.highestUnitTier) + 1;
                EntityType<? extends Mob> mobType = wave.getRandomUnitOfTier(Faction.VILLAGERS, tier);

                ArrayList<Entity> entities = UnitServerEvents.spawnMobs(mobType, level, bp.above(), 1, VILLAGER_OWNER_NAME);

                for (Entity entity : entities) {
                    placeIceOrMagma(bp, level);
                    if (entity instanceof Unit unit) {
                        remainingPop -= getModifiedPopCost(unit);
                        spawnsThisDir += 1;
                    }
                }
            }
        }
        if (remainingPop > 0) {
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + remainingPop + "/" + pop + " population worth of monster units");
        }
    }

    // spawn portals which spawn half of the wave immediately, and trickle in constantly
    public static void spawnPiglinWave(ServerLevel level, Wave wave) {
        int numPortals = wave.getNumPortals();
        int failedPortalPlacements = 0;
        ArrayList<BlockPos> portalBps = new ArrayList<>();

        for (int i = 0; i < numPortals; i++) {

            BlockPos spawnBp = null;
            int attempts = 0;
            boolean tooCloseToAnotherPortal;

            do {
                tooCloseToAnotherPortal = false;
                List<BlockPos> spawnBps = SurvivalSpawner.getValidSpawnPoints(1, level, false);
                if (!spawnBps.isEmpty())
                    spawnBp = spawnBps.get(0);
                attempts += 1;

                for (BlockPos bp : portalBps)
                    if (spawnBp != null && bp.distSqr(spawnBp) < 25)
                        tooCloseToAnotherPortal = true;
                for (BlockPos bp : portals.stream().map(p -> p.portal.originPos).toList())
                    if (spawnBp != null && bp.distSqr(spawnBp) < 25)
                        tooCloseToAnotherPortal = true;

            } while((spawnBp == null || tooCloseToAnotherPortal) && attempts < 100);

            if (spawnBp != null) {
                portalBps.add(spawnBp);
                Building building = BuildingServerEvents.placeBuilding(Portal.buildingName,
                        new BlockPos(spawnBp).above(),
                        Rotation.NONE,
                        PIGLIN_OWNER_NAME,
                        new int[] {},
                        false,
                        false
                );
            } else
                failedPortalPlacements += 1;
        }
        if (failedPortalPlacements > 0)
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + failedPortalPlacements + " portals!");
    }

    public static List<BlockPos> getValidSpawnPoints(int amount, Level level, boolean allowLiquid) {
        List<Building> buildings = BuildingServerEvents.getBuildings()
                .stream().filter(b -> !ENEMY_OWNER_NAMES.contains(b.ownerName) && !b.ownerName.isBlank())
                .toList();

        Random random = new Random();
        if (buildings.isEmpty())
            return List.of();

        Vec3 centroid = new Vec3(0,0,0);

        for (Building building : buildings) {
            centroid = centroid.add(Vec3.atCenterOf(building.centrePos));
        }
        double invBs = 1f / buildings.size();
        final Vec3 fCentroid = centroid.multiply(new Vec3(invBs, invBs, invBs));

        // calculate all valid buildings to spawn around based on distance from the centroid
        List<Building> sortedBuildings = buildings.stream().sorted(
            Comparator.comparing((Building b) -> b.centrePos.distToCenterSqr(fCentroid.x, fCentroid.y, fCentroid.z)).reversed()
        ).toList();

        int numValidBuildings = (int) (MIN_VALID_BUILDINGS + (buildings.size() * PERCENT_VALID_BUILDINGS));
        List<Building> validBuildings = sortedBuildings.subList(0, Math.min(sortedBuildings.size(), numValidBuildings));

        int spawnAttemptsThisBuilding = 0;
        BlockState spawnBs;
        BlockPos spawnBp;
        double distSqrToNearestBuilding = 999999;
        double distSqrToNearestPortal = 999999;
        int failedBuildings = 0;
        ArrayList<BlockPos> validSpawns = new ArrayList<>();

        outerloop:
        do {
            do {
                Building building = validBuildings.get(random.nextInt(validBuildings.size()));

                int x = building.centrePos.getX() + random.nextInt(-MAX_SPAWN_RANGE, MAX_SPAWN_RANGE);
                int z = building.centrePos.getZ() + random.nextInt(-MAX_SPAWN_RANGE, MAX_SPAWN_RANGE);
                int y = level.getChunkAt(new BlockPos(x, 0, z)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

                spawnBp = MiscUtil.getHighestNonAirBlock(level, new BlockPos(x, y, z), true);
                spawnBs = level.getBlockState(spawnBp);
                spawnAttemptsThisBuilding += 1;
                if (spawnAttemptsThisBuilding > 100) {
                    ReignOfNether.LOGGER.warn("Gave up trying to find a suitable spawn!");
                    failedBuildings += 1;
                    if (failedBuildings > MAX_FAILED_BUILDINGS)
                        break outerloop;
                    else
                        continue;
                }
                Vec3 vec3 = new Vec3(x, y, z);
                Building b = BuildingUtils.findClosestBuilding(false, vec3, (b1) -> !b1.ownerName.equals(PIGLIN_OWNER_NAME));
                Building p = BuildingUtils.findClosestBuilding(false, vec3, (b1) -> b1.ownerName.equals(PIGLIN_OWNER_NAME));

                if (b != null)
                    distSqrToNearestBuilding = b.centrePos.distToCenterSqr(vec3);
                if (p != null)
                    distSqrToNearestPortal = p.centrePos.distToCenterSqr(vec3);

            } while (spawnBs.getMaterial() == Material.LEAVES
                    || spawnBs.getMaterial() == Material.WOOD
                    || distSqrToNearestBuilding < (MIN_SPAWN_RANGE * MIN_SPAWN_RANGE)
                    || distSqrToNearestPortal < (10 * 10)
                    || (spawnBs.getMaterial().isLiquid() && !allowLiquid)
                    || BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), spawnBp)
                    || BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), spawnBp.above()));

            validSpawns.add(spawnBp);
            amount -= 1;

        } while(amount > 0);

        Collections.shuffle(validSpawns);

        if (validSpawns.isEmpty())
            PlayerServerEvents.sendMessageToAllPlayers("WARNING: could not find any valid spawn locations!");

        return validSpawns;
    }
}
