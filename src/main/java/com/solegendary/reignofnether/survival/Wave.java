package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Wave {

    private static final Random random = new Random();

    int number;
    int population; // multiplied by number of players
    int highestUnitTier;

    public Wave(int number, int population, int highestUnitTier) {
        this.number = number;
        this.population = population;
        this.highestUnitTier = highestUnitTier;
    }

    public int getNumPortals() {
        return Math.max(1, 1 + number / 3);
    }

    public static Wave getWave(int number) {
        if (number <= 0)
            return WAVES.get(0);
        if (number > WAVES.size())
            return WAVES.get(WAVES.size() - 1);

        return WAVES.get(number - 1);
    }

    public EntityType<? extends Mob> getRandomUnitOfTier(Faction faction, int tier) {
        List<EntityType<? extends Mob>> units;
        switch (faction) {
            case VILLAGERS -> units = ILLAGER_UNITS.get(tier);
            case PIGLINS -> units = PIGLIN_UNITS.get(tier);
            default -> units = MONSTER_UNITS.get(tier);
        }
        return units.get(random.nextInt(units.size()));
    }

    private static final Map<Integer, List<EntityType<? extends Mob>>> MONSTER_UNITS = new HashMap<>();

    static {
        MONSTER_UNITS.put(1, List.of(
                EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get(),
                EntityRegistrar.ZOMBIE_UNIT.get(),
                EntityRegistrar.SKELETON_UNIT.get()
        ));
        MONSTER_UNITS.put(2, List.of(
                EntityRegistrar.HUSK_UNIT.get(),
                EntityRegistrar.STRAY_UNIT.get(),
                EntityRegistrar.SPIDER_UNIT.get()
        ));
        MONSTER_UNITS.put(3, List.of(
                EntityRegistrar.DROWNED_UNIT.get(),
                EntityRegistrar.POISON_SPIDER_UNIT.get()
                //EntityRegistrar.CREEPER_UNIT.get()
                // + Spider Jockeys
        ));
        MONSTER_UNITS.put(4, List.of(
                EntityRegistrar.ZOGLIN_UNIT.get(),
                EntityRegistrar.ENDERMAN_UNIT.get()
                // + Poison Spider Jockeys
        ));
        MONSTER_UNITS.put(5, List.of(
                EntityRegistrar.WARDEN_UNIT.get()
                // + Charged creepers
        ));
    }

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
                //EntityRegistrar.WITCH.get()
                // + low tier enchants
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
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.RAVAGER_UNIT.get()
                // + Illager captain with resistance/strength and enchanted armor
                // + Ravager Artillery
        ));
    }

    private static final Map<Integer, List<EntityType<? extends Mob>>> PIGLIN_UNITS = new HashMap<>();

    static {
        PIGLIN_UNITS.put(1, List.of(
            EntityRegistrar.BRUTE_UNIT.get(),
            EntityRegistrar.HEADHUNTER_UNIT.get()
        ));
        PIGLIN_UNITS.put(2, List.of(
            EntityRegistrar.HOGLIN_UNIT.get()
        ));
        PIGLIN_UNITS.put(3, List.of(
            EntityRegistrar.BLAZE_UNIT.get(),
            EntityRegistrar.HOGLIN_UNIT.get()
            // + Hoglin riders
            // + shields and heavy tridents
        ));
        PIGLIN_UNITS.put(4, List.of(
            EntityRegistrar.WITHER_SKELETON_UNIT.get()
            // + bloodlust
        ));
        PIGLIN_UNITS.put(5, List.of(
            EntityRegistrar.GHAST_UNIT.get()
        ));
    }

    private static final List<Wave> WAVES = List.of(
        new Wave(1, 5, 1),
        new Wave(2, 10, 1),
        new Wave(3, 15, 2),
        new Wave(4, 20, 2),
        new Wave(5, 25, 3),
        new Wave(6, 30, 3),
        new Wave(7, 35, 4),
        new Wave(8, 40, 4),
        new Wave(9, 45, 5),
        new Wave(10, 50, 5), // after this, increase pop geometrically every 2nd wave
        new Wave(11, 60, 5),
        new Wave(12, 70, 5),
        new Wave(13, 85, 5),
        new Wave(14, 100, 5),
        new Wave(15, 120, 5),
        new Wave(16, 140, 5),
        new Wave(17, 165, 5),
        new Wave(18, 190, 5),
        new Wave(19, 220, 5),
        new Wave(20, 250, 5),
        new Wave(21, 285, 5),
        new Wave(22, 320, 5),
        new Wave(23, 360, 5),
        new Wave(24, 400, 5),
        new Wave(25, 445, 5),
        new Wave(26, 490, 5),
        new Wave(27, 540, 5),
        new Wave(28, 590, 5),
        new Wave(29, 645, 5),
        new Wave(30, 700, 5)
    );
}
