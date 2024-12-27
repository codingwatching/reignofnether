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

    public int number;
    public int population; // multiplied by number of players
    public int highestUnitTier;

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

    private static final List<Wave> WAVES = List.of(
        new Wave(1, 5, 1),    // every tier increase, raise rate of population increase by +1
        new Wave(2, 10, 1),
        new Wave(3, 15, 1),
        new Wave(4, 21, 2),
        new Wave(5, 27, 2),
        new Wave(6, 33, 2),
        new Wave(7, 40, 3),
        new Wave(8, 47, 3),
        new Wave(9, 54, 3),
        new Wave(10, 62, 4),
        new Wave(11, 70, 4),
        new Wave(12, 78, 4),
        new Wave(13, 87, 5),
        new Wave(14, 96, 5),
        new Wave(15, 105, 5),
        new Wave(16, 115, 6),
        new Wave(17, 125, 6),
        new Wave(18, 135, 6),    // after this wave, start raising population geometrically
        new Wave(19, 150, 6),
        new Wave(20, 170, 6),
        new Wave(21, 195, 6),
        new Wave(22, 225, 6),
        new Wave(23, 260, 6),
        new Wave(24, 295, 6),
        new Wave(25, 335, 6),
        new Wave(26, 380, 6),
        new Wave(27, 430, 6),
        new Wave(28, 485, 6),
        new Wave(29, 545, 6),
        new Wave(30, 610, 6)
    );
}
