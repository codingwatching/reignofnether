package com.solegendary.reignofnether.unit;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.alliance.AllianceSystem;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.buildings.monsters.SculkCatalyst;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.villagers.IronGolemBuilding;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchFireResistance;
import com.solegendary.reignofnether.research.researchItems.ResearchHeavyTridents;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.resources.ResourceSource;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.ConvertableUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.packets.UnitConvertClientboundPacket;
import com.solegendary.reignofnether.unit.packets.UnitIdleWorkerClientBoundPacket;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.unit.packets.UnitSyncWorkerClientBoundPacket;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import com.solegendary.reignofnether.unit.units.monsters.DrownedUnit;
import com.solegendary.reignofnether.unit.units.monsters.SlimeUnit;
import com.solegendary.reignofnether.unit.units.piglins.*;
import com.solegendary.reignofnether.unit.units.villagers.*;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.solegendary.reignofnether.player.PlayerServerEvents.isRTSPlayer;

public class UnitServerEvents {

    public static boolean IMPROVED_PATHFINDING = true;

    private static final int UNIT_SYNC_TICKS_MAX = 20; // how often we send out unit syncing packets
    private static int unitSyncTicks = UNIT_SYNC_TICKS_MAX;

    // max possible pop you can have regardless of buildings, adjustable via /gamerule maxPopulation
    public static int maxPopulation = ResourceCosts.DEFAULT_MAX_POPULATION;

    // actioned only when the associated unit is idle, one at a time
    private static final List<UnitActionItem> unitActionSlowQueue = Collections.synchronizedList(new ArrayList<>());
    // actioned ASAP regardless of what the unit was doing
    private static final List<UnitActionItem> unitActionFastQueue = Collections.synchronizedList(new ArrayList<>());

    private static final ArrayList<LivingEntity> allUnits = new ArrayList<>();

    private static final ArrayList<Pair<Integer, ChunkAccess>> forcedUnitChunks = new ArrayList<>();

    public static ArrayList<LivingEntity> getAllUnits() {
        return allUnits;
    }

    public static final ArrayList<UnitSave> savedUnits = new ArrayList<>();
    public static final ArrayList<TargetResourcesSave> savedTargetResources = new ArrayList<>();

    private static final int SAVE_TICKS_MAX = 1200;
    private static int saveTicks = 0;
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END)
            return;
        saveTicks += 1;
        if (saveTicks >= SAVE_TICKS_MAX) {
            ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);
            if (level != null) {
                saveUnits(level);
                saveTicks = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);
        if (level != null) {
            saveUnits(level);
            saveGatherTargets(level);
        }
    }

    public static void saveUnits(ServerLevel level) {
        UnitSaveData data = UnitSaveData.getInstance(level);
        data.units.clear();
        getAllUnits().forEach(e -> {
            if (e instanceof Unit unit) {
                // Save unit data as usual
                data.units.add(new UnitSave(e.getName().getString(), unit.getOwnerName(), e.getStringUUID()));
            }
        });
        data.save();
        level.getDataStorage().save();
        ReignOfNether.LOGGER.info("Saved " + getAllUnits().size() + " units");
    }

    public static void saveGatherTargets(ServerLevel level) {
        TargetResourcesSaveData data = TargetResourcesSaveData.getInstance(level);
        data.targetData.clear();
        AtomicInteger numWorkersSaved = new AtomicInteger();
        getAllUnits().forEach(e -> { // if currently gathering, save that gather data
            if (e instanceof WorkerUnit wUnit) {
                if (wUnit.getGatherResourceGoal().data.hasData()) {
                    wUnit.getGatherResourceGoal().data.unitUUID = e.getStringUUID();
                    data.targetData.add(wUnit.getGatherResourceGoal().data);
                    numWorkersSaved.addAndGet(1);
                } else if (wUnit.getGatherResourceGoal().saveData.hasData()) {
                    wUnit.getGatherResourceGoal().saveData.unitUUID = e.getStringUUID();
                    data.targetData.add(wUnit.getGatherResourceGoal().saveData);
                    numWorkersSaved.addAndGet(1);
                }
            }
        });
        data.save();
        level.getDataStorage().save();
        ReignOfNether.LOGGER.info("Saved " + numWorkersSaved + " gatherTargets");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);

        synchronized (savedUnits) {
            if (level != null) {
                UnitSaveData data = UnitSaveData.getInstance(level);
                savedUnits.addAll(data.units); // actually assign the data in TickEvent as entities don't exist here yet
                ReignOfNether.LOGGER.info("Loaded " + data.units.size() + " units in serverevents");
            }
        }
        synchronized (savedTargetResources) {
            if (level != null) {
                TargetResourcesSaveData data = TargetResourcesSaveData.getInstance(level);
                savedTargetResources.addAll(data.targetData); // actually assign the data in TickEvent as entities don't exist here yet
                ReignOfNether.LOGGER.info("Loaded " + data.targetData.size() + " gatherTargets in serverevents");
            }
        }
    }

    // convert all entities that match the condition to the given unit type
    public static void convertAllToUnit(
        String ownerName,
        ServerLevel level,
        Predicate<LivingEntity> entityCondition,
        EntityType<? extends Unit> entityType
    ) {
        ArrayList<Integer> oldIds = new ArrayList<>();
        ArrayList<Integer> newIds = new ArrayList<>();
        ArrayList<LivingEntity> unitsToConvert = new ArrayList<>();

        for (LivingEntity unit : UnitServerEvents.getAllUnits())
            if (entityCondition.test(unit)) {
                unitsToConvert.add(unit);
            }

        for (LivingEntity unit : unitsToConvert) {
            if (unit instanceof ConvertableUnit cUnit) {
                oldIds.add(unit.getId());
                LivingEntity newEntity = cUnit.convertToUnit(entityType);
                if (newEntity != null)
                    newIds.add(newEntity.getId());
            }
        }
        if (oldIds.size() == newIds.size() && oldIds.size() > 0) {
            UnitConvertClientboundPacket.syncConvertedUnits(ownerName, oldIds, newIds);
        }
    }

    public static int getCurrentPopulation(ServerLevel level, String ownerName) {
        int currentPopulation = 0;
        for (LivingEntity entity : allUnits)
            if (entity instanceof Unit unit) {
                if (unit.getOwnerName().equals(ownerName)) {
                    currentPopulation += unit.getPopCost();
                }
            }
        for (Building building : BuildingServerEvents.getBuildings())
            if (building.ownerName.equals(ownerName)) {
                if (building instanceof ProductionBuilding prodBuilding) {
                    for (ProductionItem prodItem : prodBuilding.productionQueue)
                        currentPopulation += prodItem.popCost;
                } else if (building instanceof IronGolemBuilding) {
                    currentPopulation += ResourceCosts.IRON_GOLEM.population;
                }
            }
        return currentPopulation;
    }

    public static void addActionItem(
            String ownerName,
            UnitAction action,
            int unitId,
            int[] unitIds,
            BlockPos preselectedBlockPos,
            BlockPos selectedBuildingPos
    ) {
        addActionItem(ownerName, action, unitId, unitIds, preselectedBlockPos, selectedBuildingPos, false);
    }

    public static void addActionItem(
        String ownerName,
        UnitAction action,
        int unitId,
        int[] unitIds,
        BlockPos preselectedBlockPos,
        BlockPos selectedBuildingPos,
        boolean shiftQueue
    ) {
        if (shiftQueue) {
            synchronized(unitActionSlowQueue) {
                for (int actionableUnitId : unitIds) {
                    unitActionSlowQueue.add(
                        new UnitActionItem(ownerName,
                                action,
                                unitId,
                                new int[] {actionableUnitId},
                                preselectedBlockPos,
                                selectedBuildingPos
                        )
                    );
                    System.out.println("added item to shiftQueue: " + action.name() + "|" + actionableUnitId + "|" + preselectedBlockPos);
                }
            }
        } else {
            synchronized(unitActionSlowQueue) {
                for (int actionableUnitId : unitIds)
                    if (unitActionSlowQueue.removeIf(uai -> uai.getUnitIds().length > 0 && uai.getUnitIds()[0] == actionableUnitId))
                        System.out.println("removed item from shiftQueue: " + action.name() + "|" + actionableUnitId + "|" + preselectedBlockPos);
            }
            synchronized (unitActionFastQueue) {
                UnitActionItem uai = new UnitActionItem(ownerName,
                        action,
                        unitId,
                        unitIds,
                        preselectedBlockPos,
                        selectedBuildingPos
                );
                if (!(!unitActionFastQueue.isEmpty() && unitActionFastQueue.get(0).equals(uai) && action == UnitAction.MOVE))
                    unitActionFastQueue.add(uai);
            }
        }
    }
    public static Relationship getUnitToEntityRelationship(Unit unit, Entity entity) {
        String ownerName1 = unit.getOwnerName();
        String ownerName2 = "";

        if (entity instanceof Player player) {
            ownerName2 = player.getName().getString();
        } else if (entity instanceof Unit) {
            ownerName2 = ((Unit) entity).getOwnerName();
        } else {
            return Relationship.NEUTRAL;
        }

        // Check if the owners are allied first
        if (AllianceSystem.isAllied(ownerName1, ownerName2)) {
            return Relationship.FRIENDLY;
        }
        // If not allied, check if the owners are the same
        if (ownerName1.equals(ownerName2)) {
            return Relationship.FRIENDLY;
        } else {
            return Relationship.HOSTILE;
        }
    }
    // similar to UnitClientEvents getUnitRelationship: given a Unit and Entity, what is the relationship between them
    public static Relationship getUnitToBuildingRelationship(Unit unit, Building building) {
        String unitOwnerName = unit.getOwnerName();
        String buildingOwnerName = building.ownerName;

        if (buildingOwnerName.isEmpty()) {
            return Relationship.NEUTRAL;
        }
        if (unitOwnerName.equals(buildingOwnerName)) {
            return Relationship.OWNED;
        } else if (AllianceSystem.isAllied(unitOwnerName, buildingOwnerName)) {
            return Relationship.FRIENDLY;
        } else {
            return Relationship.HOSTILE;
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent evt) {

        if (evt.getEntity() instanceof Unit && evt.getEntity() instanceof Mob mob) {
            mob.setBaby(false);
            mob.setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
            mob.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 1.0f);
            mob.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 1.0f);
            mob.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            mob.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            mob.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            mob.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        }

        if (evt.getEntity() instanceof Unit unit && evt.getEntity() instanceof LivingEntity entity
            && !evt.getLevel().isClientSide) {
            allUnits.add(entity);

            synchronized (savedUnits) {
                savedUnits.removeIf(su -> {
                    if (su.uuid.equals(entity.getStringUUID())) {
                        unit.setOwnerName(su.ownerName);
                        UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
                        UnitSyncClientboundPacket.sendSyncOwnerNamePacket(unit);
                        ReignOfNether.LOGGER.info(
                                "loaded unit in serverevents: " + su.ownerName + "|" + su.name + "|" + su.uuid);
                        return true;
                    }
                    return false;
                });
            }
            if (unit instanceof WorkerUnit wUnit) {
                synchronized (savedTargetResources) {
                    savedTargetResources.removeIf(sr -> {
                        if (sr.unitUUID.equals(entity.getStringUUID())) {
                            wUnit.getGatherResourceGoal().saveData = sr;
                            wUnit.getGatherResourceGoal().loadState();
                            ReignOfNether.LOGGER.info(
                                    "loaded gatherTarget in serverevents: " + sr.gatherTarget);
                            return true;
                        }
                        return false;
                    });
                }
            }

            ((Unit) entity).setupEquipmentAndUpgradesServer();

            ChunkAccess chunk = evt.getLevel().getChunk(entity.getOnPos());
            ForgeChunkManager.forceChunk((ServerLevel) evt.getLevel(),
                ReignOfNether.MOD_ID,
                entity,
                chunk.getPos().x,
                chunk.getPos().z,
                true,
                true
            );
            forcedUnitChunks.add(new Pair<>(entity.getId(), chunk));
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent evt) {
        if (evt.getEntity() instanceof Unit && evt.getEntity() instanceof LivingEntity entity
            && !evt.getLevel().isClientSide) {

            allUnits.removeIf(e -> e.getId() == entity.getId());
            UnitSyncClientboundPacket.sendLeavePacket(entity);

            //ChunkAccess chunk = evt.getLevel().getChunk(entity.getOnPos());
            //ForgeChunkManager.forceChunk((ServerLevel) evt.getLevel(), ReignOfNether.MOD_ID, entity, chunk.getPos()
            // .x, chunk.getPos().z, false, true);
            //forcedUnitChunks.removeIf(p -> p.getFirst() == entity.getId());
        }

        // if a player has no more units, then they are defeated
        if (evt.getEntity() instanceof Unit unit) {
            int unitsOwned = allUnits.stream()
                .filter(u -> (u instanceof Unit unit1 && unit1.getOwnerName().equals(unit.getOwnerName())))
                .toList()
                .size();
            if (!PlayerServerEvents.isSandboxPlayer(unit.getOwnerName()) &&
                unitsOwned == 0 && isRTSPlayer(unit.getOwnerName())
                && BuildingUtils.getTotalCompletedBuildingsOwned(false, unit.getOwnerName()) == 0) {
                PlayerServerEvents.defeat(unit.getOwnerName(), Component.translatable("server.reignofnether.lost_all").getString());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent evt) {

        // Convert nearby blocks arond a death into something that is sculk convertible
        // supposed to add to sculk_spreadable.json tag under the data/minecraft/tags/blocks but doesn't work for
        // some reason
        for (Building building : BuildingServerEvents.getBuildings()) {
            if (building instanceof SculkCatalyst sc && evt.getEntity().distanceToSqr(Vec3.atCenterOf(sc.centrePos))
                < SculkCatalyst.ESTIMATED_RANGE * SculkCatalyst.ESTIMATED_RANGE) {
                Level level = evt.getEntity().getLevel();
                BlockPos bp = evt.getEntity().getOnPos();

                if (level.getBlockState(bp).getBlock() == Blocks.DIRT_PATH) {
                    level.setBlockAndUpdate(bp, Blocks.DIRT.defaultBlockState());
                }
                if (level.getBlockState(bp.above()).getMaterial() == Material.PLANT) {
                    level.destroyBlock(bp.above(), false);
                }

                for (int x = -3; x <= 3; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -3; z <= 3; z++) {
                            BlockPos bp2 = bp.offset(x, y, z);
                            BlockState bs = level.getBlockState(bp2);
                            if (bp2.distManhattan(bp) > 3) {
                                continue;
                            }
                            if (bs.getBlock() == Blocks.DIRT_PATH) {
                                level.setBlockAndUpdate(bp2, Blocks.DIRT.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
        // drop all resources held
        if (evt.getEntity() instanceof Unit unit) {
            List<ItemStack> itemStacks = unit.getItems();
            for (ItemStack itemStack : itemStacks)
                evt.getEntity().spawnAtLocation(itemStack);
        }

        // for some reason, if we discard() creepers en masse via exploding them,
        // /rts-reset fails to run
        if (evt.getEntity() instanceof CreeperUnit creeperUnit &&
            !PlayerServerEvents.rtsPlayers.isEmpty()) {
            creeperUnit.explodeCreeper();
        }

        boolean drownedInfected = evt.getEntity().getActiveEffectsMap().containsKey(MobEffects.HUNGER);
        boolean slimeInfected = evt.getEntity().getActiveEffectsMap().containsKey(MobEffects.CONFUSION);

        if (evt.getEntity().getLastHurtByMob() instanceof Unit unit && (drownedInfected || slimeInfected)) {

            EntityType<? extends Unit> entityType = null;

            if (drownedInfected) {
                if (evt.getEntity() instanceof GruntUnit || evt.getEntity() instanceof BruteUnit
                        || evt.getEntity() instanceof HeadhunterUnit) {
                    entityType = EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get();
                } else if (evt.getEntity() instanceof HoglinUnit) {
                    entityType = EntityRegistrar.ZOGLIN_UNIT.get();
                } else if (evt.getEntity() instanceof VillagerUnit) {
                    entityType = EntityRegistrar.ZOMBIE_VILLAGER_UNIT.get();
                } else if (evt.getEntity() instanceof VindicatorUnit || evt.getEntity() instanceof PillagerUnit
                        || evt.getEntity() instanceof EvokerUnit || evt.getEntity() instanceof WitchUnit) {
                    entityType = EntityRegistrar.DROWNED_UNIT.get();
                }
            }
            if (slimeInfected && entityType == null) {
                entityType = EntityRegistrar.SLIME_UNIT.get();
            }

            if (entityType != null && evt.getEntity().getLevel() instanceof ServerLevel serverLevel) {
                Entity entity = entityType.spawn(serverLevel,
                    null,
                    null,
                    evt.getEntity().getOnPos(),
                    MobSpawnType.SPAWNER,
                    true,
                    false
                );
                if (entity instanceof SlimeUnit sUnit && evt.getEntity() instanceof Unit originalEntity) {
                    sUnit.setSize(Mth.clamp(originalEntity.getPopCost() - 1, 1, 5), true);
                }
                if (entity instanceof Unit convertedUnit) {
                    convertedUnit.setOwnerName(unit.getOwnerName());
                    entity.setYRot(evt.getEntity().getYRot());
                }
            }
        }

        if (evt.getSource().getEntity() instanceof VillagerUnit vUnit &&
            ResourceSources.isHuntableAnimal(evt.getEntity())) {
            vUnit.incrementHunterExp();
            if (!(evt.getEntity() instanceof Chicken))
                vUnit.incrementHunterExp();
        }
    }

    // animal hunting
    @SubscribeEvent
    public static void onDropItem(LivingDropsEvent evt) {
        if (ResourceSources.isHuntableAnimal(evt.getEntity()) && !evt.getSource().isMagic() && evt.getSource()
            .getEntity() instanceof Unit unit && evt.getSource().getEntity() instanceof WorkerUnit && evt.getSource()
            .getEntity() instanceof Mob mob && mob.canPickUpLoot() && !Unit.atMaxResources(unit)) {

            evt.setCanceled(true);
            for (ItemStack itemStack : ResourceSources.getFoodItemsFromAnimal((Animal) evt.getEntity())) {
                ResourceSource res = ResourceSources.getFromItem(itemStack.getItem());
                if (res != null)
                    unit.getItems().add(itemStack);
            }
            if (Unit.atThresholdResources(unit)) {
                unit.getReturnResourcesGoal().returnToClosestBuilding();
            }
        }
    }


    // for some reason we have to use the level in the same tick as the unit actions or else level.getEntity returns
    // null
    // remember to always reset targets so that users' actions always overwrite any existing action
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.level.isClientSide() || evt.level.dimension() != Level.OVERWORLD) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) evt.level;

        unitSyncTicks -= 1;
        if (unitSyncTicks <= 0) {
            unitSyncTicks = UNIT_SYNC_TICKS_MAX;
            UnitIdleWorkerClientBoundPacket.sendIdleWorkerPacket();

            for (LivingEntity entity : allUnits) {
                if (entity instanceof Unit unit) {
                    UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
                    UnitSyncClientboundPacket.sendSyncStatsPacket(entity);
                    if (entity instanceof VillagerUnit vUnit && vUnit.isVeteran())
                        UnitSyncClientboundPacket.makeVillagerVeteran(vUnit);
                }
                if (entity instanceof WorkerUnit) {
                    UnitSyncWorkerClientBoundPacket.sendSyncWorkerPacket(entity);
                }

                // remove old chunk // add current chunk
                boolean chunkNeedsUpdate = false;
                ChunkAccess newChunk = evt.level.getChunk(entity.getOnPos());

                for (Pair<Integer, ChunkAccess> forcedChunk : forcedUnitChunks) {
                    int id = forcedChunk.getFirst();
                    ChunkAccess chunk = forcedChunk.getSecond();
                    if (id == entity.getId() && (
                        chunk.getPos().x != newChunk.getPos().x || chunk.getPos().z != newChunk.getPos().z
                    )) {
                        ForgeChunkManager.forceChunk((ServerLevel) evt.level,
                            ReignOfNether.MOD_ID,
                            entity,
                            chunk.getPos().x,
                            chunk.getPos().z,
                            false,
                            true
                        );
                        chunkNeedsUpdate = true;
                    }
                }
                if (chunkNeedsUpdate) {
                    forcedUnitChunks.removeIf(p -> p.getFirst() == entity.getId());
                    ForgeChunkManager.forceChunk((ServerLevel) evt.level,
                        ReignOfNether.MOD_ID,
                        entity,
                        newChunk.getPos().x,
                        newChunk.getPos().z,
                        true,
                        true
                    );
                    forcedUnitChunks.add(new Pair<>(entity.getId(), newChunk));
                    //ReignOfNether.LOGGER.info("Updated forced chunk for entity: " + entity.getId() + " at: " +
                    // newChunk.getPos().x + "," + newChunk.getPos().z);
                }
            }
        }
        synchronized (unitActionSlowQueue) {
            UnitActionItem actionedItem = null;

            for (UnitActionItem uai : unitActionSlowQueue) {
                if (uai.getUnitIds().length > 0) {
                    Entity entity = evt.level.getEntity(uai.getUnitIds()[0]);
                    if (entity instanceof Unit unit && unit.isIdle()) {
                        uai.action(evt.level);
                        actionedItem = uai;
                        System.out.println("actioned item from queue: " + uai.getAction().name() + "|" + uai.getUnitIds()[0] + "|" + uai.getPreselectedBlockPos());
                        break;
                    }
                }
            }
            if (actionedItem != null && unitActionSlowQueue.remove(actionedItem))
                System.out.println("removed item from queue: " + actionedItem.getAction().name() + "|" + actionedItem.getUnitIds()[0] + "|" + actionedItem.getPreselectedBlockPos());
        }
        synchronized (unitActionFastQueue) {
            for (UnitActionItem actionItem : unitActionFastQueue)
                actionItem.action(evt.level);
            unitActionFastQueue.clear();
        }
    }

    @SubscribeEvent
    // assign unit owner when spawned with an egg based on whoever is closest
    public static void onMobSpawn(LivingSpawnEvent.SpecialSpawn evt) {
        if (!evt.getSpawnReason().equals(MobSpawnType.SPAWN_EGG)) {
            return;
        }

        Entity entity = evt.getEntity();
        if (evt.getEntity() instanceof Unit) {

            Vec3 pos = entity.position();
            List<Player> nearbyPlayers = MiscUtil.getEntitiesWithinRange(new Vector3d(pos.x, pos.y, pos.z),
                10,
                Player.class,
                evt.getEntity().level
            );

            float closestPlayerDist = 10;
            Player closestPlayer = null;
            for (Player player : nearbyPlayers) {
                if (player.distanceTo(entity) < closestPlayerDist && isRTSPlayer(player.getName().getString())) {
                    closestPlayerDist = player.distanceTo(entity);
                    closestPlayer = player;
                }
            }
            if (closestPlayer != null) {
                ((Unit) entity).setOwnerName(closestPlayer.getName().getString());
            }
        }
    }

    private static boolean shouldIgnoreKnockback(LivingDamageEvent evt) {
        Entity projectile = evt.getSource().getDirectEntity();
        Entity shooter = evt.getSource().getEntity();

        if (shooter instanceof HeadhunterUnit headhunterUnit && projectile instanceof ThrownTrident) {
            return !ResearchServerEvents.playerHasResearch(headhunterUnit.getOwnerName(),
                ResearchHeavyTridents.itemName
            );
        }
        if (shooter instanceof SlimeUnit slimeUnit && slimeUnit.isTiny())
            return true;
        if (projectile instanceof Fireball && shooter instanceof BlazeUnit)
            return true;
        if (projectile instanceof AbstractArrow)
            return true;

        return evt.getSource().isMagic() && evt.getSource() instanceof IndirectEntityDamageSource
            && (!(shooter instanceof EvokerUnit));
    }

    public static Entity spawnMob(
            EntityType<? extends Mob> entityType, ServerLevel level, Vec3i pos, String ownerName
    ) {
        ArrayList<Entity> entities = UnitServerEvents.spawnMobs(entityType, level, pos,1, ownerName);
        if (entities.isEmpty())
            return null;
        else
            return entities.get(0);
    }

    public static ArrayList<Entity> spawnMobs(
        EntityType<? extends Mob> entityType, ServerLevel level, Vec3i pos, int qty, String ownerName
    ) {
        ArrayList<Entity> entities = new ArrayList<>();
        if (level != null) {
            for (int i = 0; i < qty; i++) {
                Entity entity = entityType.create(level);
                if (entity != null) {
                    entity.moveTo(pos.above().getX() + i, pos.above().getY(), pos.above().getZ());
                    entities.add(entity);
                    if (entity instanceof Unit unit) {
                        unit.setOwnerName(ownerName);
                    }
                    level.addFreshEntity(entity);
                }
            }
        }
        return entities;
    }

    @SubscribeEvent
    public static void onEntityDamaged(LivingDamageEvent evt) {
        if (shouldIgnoreKnockback(evt)) {
            knockbackIgnoreIds.add(evt.getEntity().getId());
        }

        // halve friendly fire from your own/friendly creepers (but still cause knockback)
        if (evt.getSource().getEntity() instanceof CreeperUnit creeperUnit &&
                getUnitToEntityRelationship(creeperUnit, evt.getEntity()) == Relationship.FRIENDLY) {
            evt.setAmount(evt.getAmount() / 2);

            if (evt.getEntity() instanceof CreeperUnit)
                evt.setAmount(evt.getAmount() / 2);
        }

        if (ResourceSources.isHuntableAnimal(evt.getEntity()) && (
            evt.getSource().getEntity() instanceof MilitiaUnit
        )) {
            evt.setAmount(1);
            return;
        }

        if (ResourceSources.isHuntableAnimal(evt.getEntity()) && (
            evt.getSource().getEntity() instanceof VillagerUnit vUnit &&
            vUnit.getUnitProfession() == VillagerUnitProfession.HUNTER
        )) {
            evt.setAmount(2);
            return;
        }

        if (evt.getEntity() instanceof Unit && (
            evt.getSource() == DamageSource.SWEET_BERRY_BUSH || evt.getSource() == DamageSource.CACTUS
        )) {
            evt.setCanceled(true);
            return;
        }

        // halve direct ghast damage since they get bonus damage from launching units into the air
        if (evt.getSource().getEntity() instanceof GhastUnit) {
            // (unless its to a garrisoned unit)
            if (!(evt.getEntity() instanceof Unit unit && GarrisonableBuilding.getGarrison(unit) != null)) {
                evt.setAmount(evt.getAmount() / 2);
            }
        }

        // ensure projectiles from units do the damage of the unit, not the item
        if (evt.getSource().isProjectile() && evt.getSource().getEntity() instanceof AttackerUnit attackerUnit) {
            evt.setAmount(attackerUnit.getUnitAttackDamage());
        }

        // ignore added weapon damage for workers
        if (evt.getSource().getEntity() instanceof WorkerUnit && evt.getSource()
            .getEntity() instanceof AttackerUnit attackerUnit) {
            evt.setAmount(attackerUnit.getUnitAttackDamage());
        }

        if (evt.getEntity() instanceof BruteUnit brute && brute.isHoldingUpShield && (evt.getSource().isProjectile())) {
            evt.setAmount(evt.getAmount() / 4);
        }

        if (evt.getSource() == DamageSource.LIGHTNING_BOLT) {
            if (evt.getEntity() instanceof CreeperUnit) {
                evt.setCanceled(true);
            } else {
                evt.setAmount(evt.getAmount() / 2);
            }
        }

        // ignore added weapon damage for workers
        if (evt.getSource().getEntity() instanceof WorkerUnit && evt.getSource()
            .getEntity() instanceof AttackerUnit attackerUnit) {
            evt.setAmount(attackerUnit.getUnitAttackDamage());
        }

        if (evt.getEntity() instanceof BruteUnit brute && brute.isHoldingUpShield && (evt.getSource().isProjectile())) {
            evt.setAmount(evt.getAmount() / 3);
        }
        
        if (evt.getSource() == DamageSource.LIGHTNING_BOLT) {
            if (evt.getEntity() instanceof CreeperUnit) {
                evt.setCanceled(true);
            } else {
                evt.setAmount(evt.getAmount() / 2);
            }
        }

        if (evt.getEntity() instanceof Unit && (evt.getSource() == DamageSource.IN_WALL)) {
            evt.setCanceled(true);
        }

        // prevent friendly fire damage from ranged units (unless specifically targeted)
        if (evt.getSource().isProjectile() && evt.getSource().getEntity() instanceof Unit unit) {
            if (getUnitToEntityRelationship(unit, evt.getEntity()) == Relationship.FRIENDLY
                && unit.getTargetGoal().getTarget() != evt.getEntity()) {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent evt) {
        if (evt.getEntity() instanceof CreeperUnit creeperUnit) {
            creeperUnit.setSecondsOnFire(0);
        }
    }

    // prevent friendly fire from ranged units (unless specifically targeted)
    // (just allows piercing, damage is cancelled in LivingDamageEvent)
    @SubscribeEvent
    public static void onProjectileHit(ProjectileImpactEvent evt) {
        Entity owner = evt.getProjectile().getOwner();
        Entity hit = null;
        if (evt.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
            hit = ((EntityHitResult) evt.getRayTraceResult()).getEntity();
        }

        // prevent fireballs actually directly hitting anything, except other ghasts
        //  instead just relying on splash damage and fire creation
        if (owner instanceof GhastUnit && hit != null) {
            if (!(hit instanceof GhastUnit)) {
                evt.setCanceled(true);
            }
        }

        if (owner instanceof Unit unit && hit != null) {
            if (getUnitToEntityRelationship(unit, hit) == Relationship.FRIENDLY
                && unit.getTargetGoal().getTarget() != hit) {
                // for some reason, if we try to cancel a pierced arrow, it loops here forever
                if (evt.getProjectile() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
                    return;
                }
                evt.setCanceled(true);
            }
        }
    }

    public static ArrayList<Integer> knockbackIgnoreIds = new ArrayList<>();

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent evt) {
        if (evt.getEntity() instanceof GhastUnit)
            evt.setCanceled(true);
        else if (evt.getEntity() instanceof BruteUnit bruteUnit && bruteUnit.isHoldingUpShield)
            evt.setCanceled(true);
        else if (knockbackIgnoreIds.removeIf(i -> i == evt.getEntity().getId()))
            evt.setCanceled(true);
    }

    // make creepers explode from other explosions, like TNT
    /*
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate evt) {
        for (Entity entity : evt.getAffectedEntities())
            if (entity instanceof CreeperUnit cUnit) {
                UnitActionClientboundPacket.reflectUnitAction(cUnit.getOwnerName(),
                    UnitAction.EXPLODE,
                    new int[] { cUnit.getId() }
                );
            }
    }
     */

    public static void debug1() {

    }

    public static void debug2() {
    }
}
