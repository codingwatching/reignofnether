package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.resources.ResourceCost;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*
    Small event listener that sends out a clientbound packet to synchronize serverside config options with the client
    so that the GUI and other elements can properly reflect the values present on the server.
 */

@Mod.EventBusSubscriber(modid = ReignOfNether.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ConfigServerEvents {
    public static List<ResourceCost> costList;
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading evt) {
        //Replacing this logic with *more* reflection instead of hardcoding could save us trouble in the future
        //by removing the time of day it'd take to manually add another Costs class here
        //but for now this is enough reflection for one day
        System.out.println("Loaded configs serverside // ROBERT FIND MEEEEE");
        List<ResourceCost> cl = new ArrayList<ResourceCost>();
        appendResourceCosts(ReignOfNetherCommonConfigs.UnitCosts.class, cl);
        appendResourceCosts(ReignOfNetherCommonConfigs.BuildingCosts.class, cl);
        appendResourceCosts(ReignOfNetherCommonConfigs.ResearchCosts.class, cl);
        appendResourceCosts(ReignOfNetherCommonConfigs.EnchantmentCosts.class, cl);
        costList = cl;
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading evt) {
        //Ditto above
        System.out.println("Reloaded configs serverside // ROBERT FIND MEEEEE");
        List<ResourceCost> cl = new ArrayList<ResourceCost>();
        appendResourceCosts(ReignOfNetherCommonConfigs.UnitCosts.class, cl);
        appendResourceCosts(ReignOfNetherCommonConfigs.BuildingCosts.class, cl);
        appendResourceCosts(ReignOfNetherCommonConfigs.ResearchCosts.class, cl);
        appendResourceCosts(ReignOfNetherCommonConfigs.EnchantmentCosts.class, cl);
        costList = cl;
    }


    //Helper method to prepare data for FriendlyByteBuf to send config data from server to client
    //The Cursed Reflection Tech
    //Also, I don't like the syntax here, id much rather return the passed list for further operations
    //Might move this logic to a related arraylist wrapper class later to remedy that
    private static void appendResourceCosts(Class<? extends ReignOfNetherCommonConfigs.Costs> costs, List<ResourceCost> list) {
        List<ResourceCost> costList = new ArrayList<ResourceCost>();
        Field[] costFields = costs.getFields();
        for (Field costField : costFields) {
            try {
                //ew
                //TODO: FIX VARIABLE NAME PLEASE
                ResourceCostConfigEntry rcce = (ResourceCostConfigEntry) costField.get(null);
                ResourceCost cost = ResourceCost.fromConfigEntry(rcce);
                costList.add(cost);
            }
            catch (IllegalAccessException e) {
                // Handle exception here
            }
        }
        list.addAll(costList);
    }
}
