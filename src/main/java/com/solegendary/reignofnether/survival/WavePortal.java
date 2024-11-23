package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.PIGLIN_OWNER_NAME;
import static com.solegendary.reignofnether.survival.SurvivalServerEvents.VILLAGER_OWNER_NAME;
import static com.solegendary.reignofnether.survival.SurvivalSpawner.getModifiedPopCost;
import static com.solegendary.reignofnether.survival.SurvivalSpawner.placeIceOrMagma;

public class WavePortal {

    private static final int SPAWN_TICKS_MAX = 200;
    private static int spawnTicks = 0;

    public final Portal portal;
    public final Wave wave;
    private int initialSpawnPop;

    private BlockPos lastOnPos;

    public WavePortal(Portal portal, Wave wave) {
        this.portal = portal;
        this.wave = wave;
        this.initialSpawnPop = wave.population / Math.max(1, wave.number / 2);
    }

    public Portal getPortal() {
        return portal;
    }

    public void tick(long ticksToAdd) {
        if (!portal.isBuilt)
            portal.buildNextBlock((ServerLevel) portal.getLevel(), portal.ownerName);
        else if (initialSpawnPop > 0) {
            doSpawn();
        } else if (spawnTicks >= SPAWN_TICKS_MAX) {
            spawnTicks = 0;
            doSpawn();
            spawnTicks += ticksToAdd;
        }
    }

    public void doSpawn() {
        Random random = new Random();
        int tier = random.nextInt(wave.highestUnitTier) + 1;
        EntityType<? extends Unit> mobType = (EntityType<? extends Unit>) wave.getRandomUnitOfTier(Faction.PIGLINS, tier);

        ServerLevel level = (ServerLevel) portal.getLevel();
        Entity entity = portal.produceUnit(level, mobType, PIGLIN_OWNER_NAME, true);

        if (entity instanceof Unit unit)
            initialSpawnPop -= getModifiedPopCost(unit);
    }
}
