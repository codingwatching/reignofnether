package com.solegendary.reignofnether.survival.spawners;

import com.solegendary.reignofnether.ability.abilities.*;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchEvokerVexes;
import com.solegendary.reignofnether.research.researchItems.ResearchHeavyTridents;
import com.solegendary.reignofnether.research.researchItems.ResearchSoulFireballs;
import com.solegendary.reignofnether.survival.Wave;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.VindicatorUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.*;
import static com.solegendary.reignofnether.survival.spawners.WaveSpawner.*;

public class IllagerWaveSpawner {

    private static final Random random = new Random();

    private static final Map<Integer, List<EntityType<? extends Mob>>> ILLAGER_UNITS = new HashMap<>();

    static {
        ILLAGER_UNITS.put(1, List.of(
                EntityRegistrar.RAVAGER_UNIT.get(),
                EntityRegistrar.EVOKER_UNIT.get()
                //EntityRegistrar.MILITIA_UNIT.get(),
                //EntityRegistrar.VINDICATOR_UNIT.get(),
                //EntityRegistrar.PILLAGER_UNIT.get()
                // no enchants
        ));
        ILLAGER_UNITS.put(2, List.of(
                EntityRegistrar.MILITIA_UNIT.get(),
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get()
                // + 50% chance of low tier enchants
        ));
        ILLAGER_UNITS.put(3, List.of(
                EntityRegistrar.MILITIA_UNIT.get(),
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.IRON_GOLEM_UNIT.get()
                // + low tier enchants
        ));
        ILLAGER_UNITS.put(4, List.of(
                EntityRegistrar.MILITIA_UNIT.get(),
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.IRON_GOLEM_UNIT.get(),
                EntityRegistrar.EVOKER_UNIT.get()
                // + evokers can use vexes
                // 50% chance of low or high tier enchants
        ));
        ILLAGER_UNITS.put(5, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.IRON_GOLEM_UNIT.get(),
                EntityRegistrar.EVOKER_UNIT.get(),
                EntityRegistrar.RAVAGER_UNIT.get()
                // + high tier enchants
        ));
        ILLAGER_UNITS.put(6, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.IRON_GOLEM_UNIT.get(),
                EntityRegistrar.EVOKER_UNIT.get(),
                EntityRegistrar.RAVAGER_UNIT.get()
                // + Ravager Artillery with illager captain rider
        ));
    }

    public static EntityType<? extends Mob> getRandomUnitOfTier(int tier) {
        List<EntityType<? extends Mob>> units = ILLAGER_UNITS.get(tier);
        return units.get(random.nextInt(units.size()));
    }

    public static void checkAndApplyUpgrades(int tier) {
        if (tier >= 6 && !ResearchServerEvents.playerHasResearch(ENEMY_OWNER_NAME, ResearchEvokerVexes.itemName))
            ResearchServerEvents.addResearch(ENEMY_OWNER_NAME, ResearchEvokerVexes.itemName);
    }

    public static void checkAndApplyEnchants(LivingEntity entity, int tier) {
        Enchantment enchantment = null;

        if (entity instanceof VindicatorUnit vUnit && (tier == 2 || tier == 3)) {
            enchantment = EnchantMaiming.actualEnchantment;
        }
        else if (entity instanceof VindicatorUnit vUnit && (tier == 4 || tier == 5)) {
            enchantment = EnchantSharpness.actualEnchantment;
        }
        else if (entity instanceof PillagerUnit vUnit && (tier == 3 || tier == 4)) {
            enchantment = EnchantQuickCharge.actualEnchantment;
        }
        else if (entity instanceof PillagerUnit vUnit && (tier == 5 || tier == 6)) {
            enchantment = EnchantMultishot.actualEnchantment;
        }
        else if (entity instanceof EvokerUnit vUnit && tier >= 6) {
            enchantment = EnchantVigor.actualEnchantment;
        }

        ItemStack item = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        if (enchantment != null && item != ItemStack.EMPTY) {
            EnchantmentHelper.setEnchantments(new HashMap<>(), item);
            item.enchant(enchantment, enchantment == Enchantments.SHARPNESS ? 2 : 1);
        }
    }

    // spawn illagers from one direction
    public static void spawnIllagerWave(ServerLevel level, Wave wave) {
        checkAndApplyUpgrades(wave.highestUnitTier);

        final int pop = wave.population * PlayerServerEvents.rtsPlayers.size();
        int remainingPop = wave.population * PlayerServerEvents.rtsPlayers.size();
        List<BlockPos> spawnBps = getValidSpawnPoints(remainingPop, level, true);
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

                EntityType<? extends Mob> mobType = IllagerWaveSpawner.getRandomUnitOfTier(wave.highestUnitTier);

                Entity entity = UnitServerEvents.spawnMob(mobType, level, bp.above(), ENEMY_OWNER_NAME);

                if (random.nextBoolean() && wave.highestUnitTier >= 6 && entity instanceof RavagerUnit ravagerUnit) {
                    Entity entityPassenger = UnitServerEvents.spawnMob(EntityRegistrar.PILLAGER_UNIT.get(),
                            level, bp.above(), ENEMY_OWNER_NAME);
                    if (entityPassenger instanceof Unit unit) {
                        entityPassenger.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
                        entityPassenger.startRiding(ravagerUnit);
                        remainingPop -= getModifiedPopCost(unit);
                    }
                }

                if (entity instanceof Unit unit) {
                    checkAndApplyEnchants((LivingEntity) unit, wave.highestUnitTier);
                    placeIceOrMagma(bp, level);
                    remainingPop -= getModifiedPopCost(unit);
                    spawnsThisDir += 1;
                }
            }
        }
        if (remainingPop > 0) {
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + remainingPop + "/" + pop + " population worth of monster units");
        }
        lastFaction = Faction.VILLAGERS;
    }
}
