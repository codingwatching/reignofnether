package com.solegendary.reignofnether.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class ResourceCostConfigEntry {
    private ForgeConfigSpec.ConfigValue<Integer> FOOD;
    private ForgeConfigSpec.ConfigValue<Integer> WOOD;
    private ForgeConfigSpec.ConfigValue<Integer> ORE;
    private ForgeConfigSpec.ConfigValue<Integer> SECONDS;
    private ForgeConfigSpec.ConfigValue<Integer> POPULATION;

    private final int DEFAULT_FOOD;
    private final int DEFAULT_WOOD;
    private final int DEFAULT_ORE;
    private final int DEFAULT_SECONDS;
    private final int DEFAULT_POPULATION;
    //TODO: Use translateable component, add to lang file
    private final String COMMENT;

    private ResourceCostConfigEntry(int food, int wood, int ore, int seconds, int population, String comment) {
        this.DEFAULT_FOOD = food;
        this.DEFAULT_WOOD = wood;
        this.DEFAULT_ORE = ore;
        this.DEFAULT_SECONDS = seconds;
        this.DEFAULT_POPULATION = population;
        this.COMMENT = comment;
    }
    public static ResourceCostConfigEntry Unit(int food, int wood, int ore, int seconds, int population, String comment) { // buildings
        return new ResourceCostConfigEntry(food, wood, ore, seconds, population, comment);
    }
    public static ResourceCostConfigEntry Research(int food, int wood, int ore, int seconds, String comment) { // buildings
        return new ResourceCostConfigEntry(food, wood, ore, seconds, 0, comment);
    }
    public static ResourceCostConfigEntry Building(int food, int wood, int ore, int supply, String comment) { // buildings
        return new ResourceCostConfigEntry(food, wood, ore, 0, supply, comment);
    }
    public static ResourceCostConfigEntry Enchantment(int food, int wood, int ore, String comment) { // buildings
        return new ResourceCostConfigEntry(food, wood, ore, 0, 0, comment);
    }

    //Defines each config value for the given ResourceCostConfigEntry
    public void define(ForgeConfigSpec.Builder builder) {
        builder.push(this.COMMENT);
        this.FOOD = builder.define("Food cost", this.DEFAULT_FOOD);
        this.WOOD = builder.define("Wood cost", this.DEFAULT_WOOD);
        this.ORE = builder.define("Ore cost", this.DEFAULT_ORE);
        this.SECONDS = builder.define("Time to create", this.DEFAULT_SECONDS);
        this.POPULATION = builder.define("Population value", this.DEFAULT_POPULATION);
        builder.pop();
    }

    public Integer getFood() {return this.FOOD.get();}
    public Integer getWood() {return this.WOOD.get();}
    public Integer getOre() {return this.ORE.get();}
    public Integer getSeconds() {return this.SECONDS.get();}
    public Integer getPopulation() {return this.POPULATION.get();}
}
