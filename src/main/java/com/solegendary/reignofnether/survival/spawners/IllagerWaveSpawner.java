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

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.*;
import static com.solegendary.reignofnether.survival.spawners.WaveSpawner.*;

public class IllagerWaveSpawner {

    private static final Random random = new Random();

    private static final Map<Integer, List<EntityType<? extends Mob>>> ILLAGER_UNITS = new HashMap<>();

    static {
        ILLAGER_UNITS.put(1, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get()
                // no enchants
        ));
        ILLAGER_UNITS.put(2, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.IRON_GOLEM_UNIT.get()
                // + low tier enchants
                // removes tier 1
        ));
        ILLAGER_UNITS.put(3, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.EVOKER_UNIT.get()
                // + high tier enchants on vindicators and pillagers
        ));
        ILLAGER_UNITS.put(4, List.of(
                EntityRegistrar.EVOKER_UNIT.get(),
                EntityRegistrar.RAVAGER_UNIT.get()
                // + evokers can use vexes
        ));
        ILLAGER_UNITS.put(5, List.of(
                EntityRegistrar.RAVAGER_UNIT.get()
        ));
        ILLAGER_UNITS.put(6, List.of(
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
        int enchantLevel = 1;
        if (entity instanceof VindicatorUnit vUnit && tier == 2) {
            enchantment = EnchantMaiming.actualEnchantment;
            enchantLevel = EnchantMaiming.enchantLevel;
        }
        else if (entity instanceof PillagerUnit vUnit && tier == 2) {
            enchantment = EnchantQuickCharge.actualEnchantment;
            enchantLevel = EnchantQuickCharge.enchantLevel;
        }
        else if (entity instanceof VindicatorUnit vUnit && tier >= 3) {
            enchantment = EnchantSharpness.actualEnchantment;
            enchantLevel = EnchantSharpness.enchantLevel;
        }
        else if (entity instanceof PillagerUnit vUnit && tier >= 3) {
            enchantment = EnchantMultishot.actualEnchantment;
            enchantLevel = EnchantMultishot.enchantLevel;
        }
        else if (entity instanceof EvokerUnit vUnit && tier >= 4) {
            enchantment = EnchantVigor.actualEnchantment;
            enchantLevel = EnchantVigor.enchantLevel;
        }
        ItemStack item = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        if (enchantment != null && item != ItemStack.EMPTY) {
            EnchantmentHelper.setEnchantments(new HashMap<>(), item);
            item.enchant(enchantment, enchantLevel);
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

                int tier;
                if (wave.highestUnitTier >= 2 && wave.highestUnitTier < 6)
                    tier = random.nextInt(wave.highestUnitTier - 1) + 2;
                else if (wave.highestUnitTier >= 6)
                    tier = random.nextInt(wave.highestUnitTier - 2) + 1;
                else
                    tier = random.nextInt(wave.highestUnitTier) + 1;

                EntityType<? extends Mob> mobType = IllagerWaveSpawner.getRandomUnitOfTier(tier);

                Entity entity = UnitServerEvents.spawnMob(mobType, level, bp.above(), ENEMY_OWNER_NAME);

                if (wave.highestUnitTier >= 6 && entity instanceof RavagerUnit ravagerUnit) {
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
