package com.solegendary.reignofnether.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ReignOfNetherClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> TEST_VALUE;

    static {
        BUILDER.push("Configuration File");
        BUILDER.pop();

        TEST_VALUE = BUILDER.comment("A test value")
                .define("Test Value: ", 50);

        SPEC = BUILDER.build();
    }
}
