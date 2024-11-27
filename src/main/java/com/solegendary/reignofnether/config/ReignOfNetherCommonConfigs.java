package com.solegendary.reignofnether.config;
import net.minecraftforge.common.ForgeConfigSpec;

/*
    Class responsible for defining all configurable ResourceCosts; this occurs during commonsetup
 */
public class ReignOfNetherCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    //TODO: Implement each static class as a Hashmap<String, ResourceCostConfigEntry>

    static {
        BUILDER.push("Configuration File");
        BUILDER.pop();
        BUILDER.comment("Unit cost configurations");
        //*Units*
        //Monsters
        BUILDER.comment("Monsters");
        UnitCosts.CREEPER.define(BUILDER);
        UnitCosts.ZOMBIE.define(BUILDER);
        UnitCosts.ZOMBIE_VILLAGER.define(BUILDER);
        UnitCosts.SKELETON.define(BUILDER);
        UnitCosts.STRAY.define(BUILDER);
        UnitCosts.HUSK.define(BUILDER);
        UnitCosts.DROWNED.define(BUILDER);
        UnitCosts.SPIDER.define(BUILDER);
        UnitCosts.POISON_SPIDER.define(BUILDER);
        UnitCosts.WARDEN.define(BUILDER);
        UnitCosts.ZOMBIE_PIGLIN.define(BUILDER);
        UnitCosts.ZOGLIN.define(BUILDER);
        //Villagers
        BUILDER.comment("Villagers");
        UnitCosts.VILLAGER.define(BUILDER);
        UnitCosts.IRON_GOLEM.define(BUILDER);
        UnitCosts.PILLAGER.define(BUILDER);
        UnitCosts.VINDICATOR.define(BUILDER);
        UnitCosts.WITCH.define(BUILDER);
        UnitCosts.EVOKER.define(BUILDER);
        UnitCosts.RAVAGER.define(BUILDER);
        //Piglins
        BUILDER.comment("Piglins");
        UnitCosts.GRUNT.define(BUILDER);
        UnitCosts.BRUTE.define(BUILDER);
        UnitCosts.HEADHUNTER.define(BUILDER);
        UnitCosts.HOGLIN.define(BUILDER);
        UnitCosts.BLAZE.define(BUILDER);
        UnitCosts.WITHER_SKELETON.define(BUILDER);
        UnitCosts.GHAST.define(BUILDER);

        //*Buildings*
        BUILDER.comment("Building Cost Configurations");
        BuildingCosts.STOCKPILE.define(BUILDER);
        BuildingCosts.OAK_BRIDGE.define(BUILDER);
        BuildingCosts.SPRUCE_BRIDGE.define(BUILDER);
        BuildingCosts.BLACKSTONE_BRIDGE.define(BUILDER);
        //Monsters
        BUILDER.comment("Monsters");
        BuildingCosts.MAUSOLEUM.define(BUILDER);
        BuildingCosts.HAUNTED_HOUSE.define(BUILDER);
        BuildingCosts.PUMPKIN_FARM.define(BUILDER);
        BuildingCosts.SCULK_CATALYST.define(BUILDER);
        BuildingCosts.GRAVEYARD.define(BUILDER);
        BuildingCosts.SPIDER_LAIR.define(BUILDER);
        BuildingCosts.DUNGEON.define(BUILDER);
        BuildingCosts.LABORATORY.define(BUILDER);
        BuildingCosts.DARK_WATCHTOWER.define(BUILDER);
        BuildingCosts.STRONGHOLD.define(BUILDER);
        //Villagers
        BUILDER.comment("Villagers");
        BuildingCosts.TOWN_CENTRE.define(BUILDER);
        BuildingCosts.VILLAGER_HOUSE.define(BUILDER);
        BuildingCosts.WHEAT_FARM.define(BUILDER);
        BuildingCosts.BARRACKS.define(BUILDER);
        BuildingCosts.BLACKSMITH.define(BUILDER);
        BuildingCosts.ARCANE_TOWER.define(BUILDER);
        BuildingCosts.LIBRARY.define(BUILDER);
        BuildingCosts.WATCHTOWER.define(BUILDER);
        BuildingCosts.CASTLE.define(BUILDER);
        BuildingCosts.IRON_GOLEM_BUILDING.define(BUILDER);
        //Piglins
        BUILDER.comment("Piglins");
        BuildingCosts.CENTRAL_PORTAL.define(BUILDER);
        BuildingCosts.BASIC_PORTAL.define(BUILDER);
        BuildingCosts.NETHERWART_FARM.define(BUILDER);
        BuildingCosts.BASTION.define(BUILDER);
        BuildingCosts.HOGLIN_STABLES.define(BUILDER);
        BuildingCosts.FLAME_SANCTUARY.define(BUILDER);
        BuildingCosts.WITHER_SHRINE.define(BUILDER);
        BuildingCosts.FORTRESS.define(BUILDER);

        //*Research*
        BUILDER.comment("Research Cost Configurations");
        ResearchCosts.RESEARCH_GOLEM_SMITHING.define(BUILDER);
        ResearchCosts.RESEARCH_LAB_LIGHTNING_ROD.define(BUILDER);
        ResearchCosts.RESEARCH_RESOURCE_CAPACITY.define(BUILDER);
        ResearchCosts.RESEARCH_SPIDER_JOCKEYS.define(BUILDER);
        ResearchCosts.RESEARCH_POISON_SPIDERS.define(BUILDER);
        ResearchCosts.RESEARCH_HUSKS.define(BUILDER);
        ResearchCosts.RESEARCH_DROWNED.define(BUILDER);
        ResearchCosts.RESEARCH_STRAYS.define(BUILDER);
        ResearchCosts.RESEARCH_LINGERING_POTIONS.define(BUILDER);
        ResearchCosts.RESEARCH_EVOKER_VEXES.define(BUILDER);
        ResearchCosts.RESEARCH_CASTLE_FLAG.define(BUILDER);
        ResearchCosts.RESEARCH_GRAND_LIBRARY.define(BUILDER);
        ResearchCosts.RESEARCH_SILVERFISH.define(BUILDER);
        ResearchCosts.RESEARCH_SCULK_AMPLIFIERS.define(BUILDER);
        ResearchCosts.RESEARCH_RAVAGER_ARTILLERY.define(BUILDER);
        ResearchCosts.RESEARCH_BRUTE_SHIELDS.define(BUILDER);
        ResearchCosts.RESEARCH_HOGLIN_CAVALRY.define(BUILDER);
        ResearchCosts.RESEARCH_HEAVY_TRIDENTS.define(BUILDER);
        ResearchCosts.RESEARCH_BLAZE_FIRE_WALL.define(BUILDER);
        ResearchCosts.RESEARCH_FIRE_RESISTANCE.define(BUILDER);
        ResearchCosts.RESEARCH_WITHER_CLOUDS.define(BUILDER);
        ResearchCosts.RESEARCH_BLOODLUST.define(BUILDER);
        ResearchCosts.RESEARCH_ADVANCED_PORTALS.define(BUILDER);
        ResearchCosts.RESEARCH_CIVILIAN_PORTAL.define(BUILDER);
        ResearchCosts.RESEARCH_MILITARY_PORTAL.define(BUILDER);
        ResearchCosts.RESEARCH_TRANSPORT_PORTAL.define(BUILDER);

        //*Enchantments*
        BUILDER.comment("Enchantment Cost Configurations");
        EnchantmentCosts.ENCHANT_MAIMING.define(BUILDER);
        EnchantmentCosts.ENCHANT_QUICK_CHARGE.define(BUILDER);
        EnchantmentCosts.ENCHANT_SHARPNESS.define(BUILDER);
        EnchantmentCosts.ENCHANT_MULTISHOT.define(BUILDER);
        EnchantmentCosts.ENCHANT_VIGOR.define(BUILDER);
        SPEC = BUILDER.build();
    }

    public static class UnitCosts implements Costs {
        //Monsters
        public static final ResourceCostConfigEntry CREEPER = ResourceCostConfigEntry.Unit(50, 0, 100, 35, 2, "Creeper Config");
        public static final ResourceCostConfigEntry ZOMBIE = ResourceCostConfigEntry.Unit(75, 0, 0, 10, 1, "Zombie Config");
        public static final ResourceCostConfigEntry ZOMBIE_VILLAGER = ResourceCostConfigEntry.Unit(50,0,0,15,1, "Zombie Villager Config");
        public static final ResourceCostConfigEntry SKELETON = ResourceCostConfigEntry.Unit(60,35,0,18,1, "Skeleton Config");
        public static final ResourceCostConfigEntry STRAY = ResourceCostConfigEntry.Unit(60,35,0,18,1, "Stray Config");
        public static final ResourceCostConfigEntry HUSK = ResourceCostConfigEntry.Unit(75,0,0,18,1, "Stray Config");
        public static final ResourceCostConfigEntry DROWNED = ResourceCostConfigEntry.Unit(75,0,0,18,1, "Drowned Config");
        public static final ResourceCostConfigEntry SPIDER = ResourceCostConfigEntry.Unit(90,25,25,25,2, "Spider Config");
        public static final ResourceCostConfigEntry POISON_SPIDER = ResourceCostConfigEntry.Unit(90,25,25,25,2, "Poison Spider Config");
        public static final ResourceCostConfigEntry WARDEN = ResourceCostConfigEntry.Unit(250,0,125,40,4, "Warden Config");

        public static final ResourceCostConfigEntry ZOMBIE_PIGLIN = ResourceCostConfigEntry.Unit(0,0,0,10,1, "Zombie Piglin Config");
        public static final ResourceCostConfigEntry ZOGLIN = ResourceCostConfigEntry.Unit(0,0,0,10,2, "Zoglin Config");

        // Villagers
        public static final ResourceCostConfigEntry VILLAGER = ResourceCostConfigEntry.Unit(50,0,0,15,1, "Villager Config");
        public static final ResourceCostConfigEntry IRON_GOLEM = ResourceCostConfigEntry.Unit(0,50,200,40,4, "Iron Golem Config");
        public static final ResourceCostConfigEntry PILLAGER = ResourceCostConfigEntry.Unit(120,85,0,35,3, "Pillager Config");
        public static final ResourceCostConfigEntry VINDICATOR = ResourceCostConfigEntry.Unit(175,0,0,35,3, "Vindicator Config");
        public static final ResourceCostConfigEntry WITCH = ResourceCostConfigEntry.Unit(100,100,100,35,3, "Witch Config");
        public static final ResourceCostConfigEntry EVOKER = ResourceCostConfigEntry.Unit(150,0,150,35,3, "Evoker Config");
        public static final ResourceCostConfigEntry RAVAGER = ResourceCostConfigEntry.Unit(400,50,150,60,7, "Ravager Config");

        // Piglins
        public static final ResourceCostConfigEntry GRUNT = ResourceCostConfigEntry.Unit(50,0,0,15,1, "Grunt Config");
        public static final ResourceCostConfigEntry BRUTE = ResourceCostConfigEntry.Unit(120,0,0,25,2, "Brute Config");
        public static final ResourceCostConfigEntry HEADHUNTER = ResourceCostConfigEntry.Unit(90,60,0,25,2, "Headhunter Config");
        public static final ResourceCostConfigEntry HOGLIN = ResourceCostConfigEntry.Unit(150,0,75,35,3, "Hoglin Config");
        public static final ResourceCostConfigEntry BLAZE = ResourceCostConfigEntry.Unit(50,50,100,30,2, "Blaze Config");
        public static final ResourceCostConfigEntry WITHER_SKELETON = ResourceCostConfigEntry.Unit(200,0,150,40,4, "Wither Skeleton Config");
        public static final ResourceCostConfigEntry GHAST = ResourceCostConfigEntry.Unit(100,150,250,50,5, "Ghast Config");
    }
    public static class BuildingCosts implements Costs {
        public static final ResourceCostConfigEntry STOCKPILE = ResourceCostConfigEntry.Building(0,75,0, 0, "Stockpile Config");
        public static final ResourceCostConfigEntry OAK_BRIDGE = ResourceCostConfigEntry.Building(0,50,0, 0, "Oak Bridge Config");
        public static final ResourceCostConfigEntry SPRUCE_BRIDGE = ResourceCostConfigEntry.Building(0,50,0, 0, "Spruce Bridge Config");
        public static final ResourceCostConfigEntry BLACKSTONE_BRIDGE = ResourceCostConfigEntry.Building(0,0,50, 0, "Blackstone Bridge Config");

        // Monsters
        public static final ResourceCostConfigEntry MAUSOLEUM = ResourceCostConfigEntry.Building(0,300,150, 10, "Mausoleum Config");
        public static final ResourceCostConfigEntry HAUNTED_HOUSE = ResourceCostConfigEntry.Building(0,100,0, 10, "Haunted House Config");
        public static final ResourceCostConfigEntry PUMPKIN_FARM = ResourceCostConfigEntry.Building(0,200,0, 0, "Pumpkin Farm Config");
        public static final ResourceCostConfigEntry SCULK_CATALYST = ResourceCostConfigEntry.Building(0,125,0, 0, "Sculk Catalyst Config");
        public static final ResourceCostConfigEntry GRAVEYARD = ResourceCostConfigEntry.Building(0,150,0, 0, "Graveyard Config");
        public static final ResourceCostConfigEntry SPIDER_LAIR = ResourceCostConfigEntry.Building(0,150,75, 0, "Spider Lair Config");
        public static final ResourceCostConfigEntry DUNGEON = ResourceCostConfigEntry.Building(0,150,75, 0, "Dungeon Config");
        public static final ResourceCostConfigEntry LABORATORY = ResourceCostConfigEntry.Building(0,250,150, 0, "Laboratory Config");
        public static final ResourceCostConfigEntry DARK_WATCHTOWER = ResourceCostConfigEntry.Building(0,100,100, 0, "Dark Watchtower Config");
        public static final ResourceCostConfigEntry STRONGHOLD = ResourceCostConfigEntry.Building(0,400,300, 0, "Stronghold Config");

        // Villagers
        public static final ResourceCostConfigEntry TOWN_CENTRE = ResourceCostConfigEntry.Building(0,300,150, 10, "Town Centre Config");
        public static final ResourceCostConfigEntry VILLAGER_HOUSE = ResourceCostConfigEntry.Building(0,100,0, 10, "Villager House Config");
        public static final ResourceCostConfigEntry WHEAT_FARM = ResourceCostConfigEntry.Building(0,150,0, 0, "Wheat Farm Config");
        public static final ResourceCostConfigEntry BARRACKS = ResourceCostConfigEntry.Building(0,150,0, 0, "Barracks Config");
        public static final ResourceCostConfigEntry BLACKSMITH = ResourceCostConfigEntry.Building(0,100,300, 0, "Blacksmith Config");
        public static final ResourceCostConfigEntry ARCANE_TOWER = ResourceCostConfigEntry.Building(0,200,100, 0, "Arcane Tower Config");
        public static final ResourceCostConfigEntry LIBRARY = ResourceCostConfigEntry.Building(0,300,100, 0, "Library Config");
        public static final ResourceCostConfigEntry WATCHTOWER = ResourceCostConfigEntry.Building(0,100,100, 0, "Watchtower Config");
        public static final ResourceCostConfigEntry CASTLE = ResourceCostConfigEntry.Building(0,400,300, 0, "Castle Config");
        public static final ResourceCostConfigEntry IRON_GOLEM_BUILDING = ResourceCostConfigEntry.Building(0,50,200, 0, "Iron Golem Building Config");

        // Piglins
        public static final ResourceCostConfigEntry CENTRAL_PORTAL = ResourceCostConfigEntry.Building(0,300,150, 10, "Central Portal Config");
        public static final ResourceCostConfigEntry BASIC_PORTAL = ResourceCostConfigEntry.Building(0, 75, 0, 0, "Basic Portal Config");
        public static final ResourceCostConfigEntry NETHERWART_FARM = ResourceCostConfigEntry.Building(0, 150, 0, 0, "Netherwart Farm Config");
        public static final ResourceCostConfigEntry BASTION = ResourceCostConfigEntry.Building(0, 150, 100, 0, "Bastion Config");
        public static final ResourceCostConfigEntry HOGLIN_STABLES = ResourceCostConfigEntry.Building(0, 250, 0, 0, "Hoglin Stables Config");
        public static final ResourceCostConfigEntry FLAME_SANCTUARY = ResourceCostConfigEntry.Building(0, 300, 150, 0, "Flame Sanctuary Config");
        public static final ResourceCostConfigEntry WITHER_SHRINE = ResourceCostConfigEntry.Building(0, 350, 200, 0, "Wither Shrine Config");
        public static final ResourceCostConfigEntry FORTRESS = ResourceCostConfigEntry.Building(0, 400, 300, 0, "Fortress Config");
    }
    public static class ResearchCosts implements Costs {
        public static final ResourceCostConfigEntry RESEARCH_GOLEM_SMITHING = ResourceCostConfigEntry.Research(0, 150,200, 90, "Golem Smithing Research Config");
        public static final ResourceCostConfigEntry RESEARCH_LAB_LIGHTNING_ROD = ResourceCostConfigEntry.Research(0,0,400, 120, "Lightning Lab Research Config");
        public static final ResourceCostConfigEntry RESEARCH_RESOURCE_CAPACITY = ResourceCostConfigEntry.Research(200,200,0, 90, "Stockpile Resource Capacity Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SPIDER_JOCKEYS = ResourceCostConfigEntry.Research(300,250,0, 100, "Spider Jockey Research Config");
        public static final ResourceCostConfigEntry RESEARCH_POISON_SPIDERS = ResourceCostConfigEntry.Research(400,0,250, 150, "Poison Spider Research Config");
        public static final ResourceCostConfigEntry RESEARCH_HUSKS = ResourceCostConfigEntry.Research(500,0,500, 200, "Husk Research Config");
        public static final ResourceCostConfigEntry RESEARCH_DROWNED = ResourceCostConfigEntry.Research(500,0,500, 200, "Drowned Research Config");
        public static final ResourceCostConfigEntry RESEARCH_STRAYS = ResourceCostConfigEntry.Research(500,500,0, 200, "Stray Research Config");
        public static final ResourceCostConfigEntry RESEARCH_LINGERING_POTIONS = ResourceCostConfigEntry.Research(250,250,250, 140, "Lingering Potion Research Config");
        public static final ResourceCostConfigEntry RESEARCH_EVOKER_VEXES = ResourceCostConfigEntry.Research(500,0,300, 120, "Evoker Vex Research Config");
        public static final ResourceCostConfigEntry RESEARCH_CASTLE_FLAG = ResourceCostConfigEntry.Research(200,150,150, 90, "Captain Banner Research Config");
        public static final ResourceCostConfigEntry RESEARCH_GRAND_LIBRARY = ResourceCostConfigEntry.Research(0,200,100, 140, "Grand Library Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SILVERFISH = ResourceCostConfigEntry.Research(0,300,300, 120, "Silverfish Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SCULK_AMPLIFIERS = ResourceCostConfigEntry.Research(0,200,400, 150, "Sculk Amplifier Research Config");
        public static final ResourceCostConfigEntry RESEARCH_RAVAGER_ARTILLERY = ResourceCostConfigEntry.Research(400,0,350, 140, "Ravager Artillery Research Config");
        public static final ResourceCostConfigEntry RESEARCH_BRUTE_SHIELDS = ResourceCostConfigEntry.Research(0,300,300, 150, "Brute Shields Research Config");
        public static final ResourceCostConfigEntry RESEARCH_HOGLIN_CAVALRY = ResourceCostConfigEntry.Research(300,250,0, 100, "Hoglin Cavalry Research Config");
        public static final ResourceCostConfigEntry RESEARCH_HEAVY_TRIDENTS = ResourceCostConfigEntry.Research(0, 250, 250, 120, "Heavy Tridents Research Config");
        public static final ResourceCostConfigEntry RESEARCH_BLAZE_FIRE_WALL = ResourceCostConfigEntry.Research(400, 0, 300, 150, "Blaze Fire Wall Research Config");
        public static final ResourceCostConfigEntry RESEARCH_FIRE_RESISTANCE = ResourceCostConfigEntry.Research(0, 200, 200, 100, "Fire Resistance Research Config");
        public static final ResourceCostConfigEntry RESEARCH_WITHER_CLOUDS = ResourceCostConfigEntry.Research(250, 0, 350, 150, "Wither Clouds Research Config");
        public static final ResourceCostConfigEntry RESEARCH_BLOODLUST = ResourceCostConfigEntry.Research(250, 250, 250, 150, "Bloodlust Research Config");
        public static final ResourceCostConfigEntry RESEARCH_ADVANCED_PORTALS = ResourceCostConfigEntry.Research(0, 300, 300, 150, "Advanced Portals Research Config");
        public static final ResourceCostConfigEntry RESEARCH_CIVILIAN_PORTAL = ResourceCostConfigEntry.Research(0, 75, 0, 20, "Civilian Portal Research Config");
        public static final ResourceCostConfigEntry RESEARCH_MILITARY_PORTAL = ResourceCostConfigEntry.Research(0, 125, 0, 30, "Military Portal Research Config");
        public static final ResourceCostConfigEntry RESEARCH_TRANSPORT_PORTAL = ResourceCostConfigEntry.Research(0, 175, 0, 40, "Transport Portal Research Config");
    }
    public static class EnchantmentCosts implements Costs {
        public static final ResourceCostConfigEntry ENCHANT_MAIMING = ResourceCostConfigEntry.Enchantment(0,20, 30, "Maiming Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_QUICK_CHARGE = ResourceCostConfigEntry.Enchantment(0,30, 15, "Quick Charge Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_SHARPNESS = ResourceCostConfigEntry.Enchantment(0,40, 60, "Sharpness Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_MULTISHOT = ResourceCostConfigEntry.Enchantment(0,70, 35, "Multishot Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_VIGOR = ResourceCostConfigEntry.Enchantment(0,60, 60, "Vigor Enchantment Config");
    }
    public interface Costs {}
}
