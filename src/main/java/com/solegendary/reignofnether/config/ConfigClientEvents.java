package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ConfigClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    //Load config data from server
    public static void loadConfigData(ClientboundSyncResourceCostPacket msg, Supplier<NetworkEvent.Context> ctx) {
        String key = msg.getId();
        if(ResourceCost.ENTRIES.containsKey(key)) {
            ResourceCost rescost = ResourceCost.ENTRIES.get(key);
            //jank, but this is how we rebake using the values sent from the packet currently
            //we can clean this up later

            rescost.wood = msg.getWood();
            rescost.food = msg.getFood();
            rescost.ore = msg.getOre();
            rescost.ticks = msg.getTicks();
            rescost.population = msg.getPopulation();

            int woodDiff = msg.getWood() - rescost.wood;
            int foodDiff = msg.getFood() - rescost.food;
            int oreDiff = msg.getOre() - rescost.ore;
            int ticksDiff = msg.getTicks() - rescost.ticks;
            int popDiff = msg.getPopulation() - rescost.population;

            if (MC.player != null &&
                (woodDiff != 0 ||
                foodDiff != 0 ||
                oreDiff != 0 ||
                ticksDiff != 0 ||
                popDiff != 0)
            ) {
                String text = "Changed costs for: " + rescost.id;

                if (foodDiff != 0)
                    text += " \uE000  " + foodDiff;
                if (woodDiff != 0)
                    text += " \uE001  " + woodDiff;
                if (oreDiff != 0)
                    text += " \uE002  " + oreDiff;
                if (ticksDiff != 0)
                    text += " \uE004  " + ticksDiff / 20;
                if (popDiff != 0)
                    text += " \uE003  " + popDiff;

                MC.player.sendSystemMessage(Component.literal(text));
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        ReignOfNether.LOGGER.info("onPlayerJoined fired from ClientConfigEvents");
        //If we own this singleplayer world
        if (evt.getEntity().getServer().isSingleplayerOwner(evt.getEntity().getGameProfile())) {
            //rebake from clientsideside configs
            ReignOfNether.LOGGER.info("Attempting to rebake from client..");
            for(ResourceCostConfigEntry entry : ResourceCostConfigEntry.ENTRIES) {
                String key = entry.id;
                if(ResourceCost.ENTRIES.containsKey(key)) {
                    ResourceCost rescost = ResourceCost.ENTRIES.get(key);
                    ReignOfNether.LOGGER.info("ID found: " + key + ", replacing resourcecost " + ResourceCost.ENTRIES.get(key));
                    rescost.bakeValues(entry);
                }
            }
            ResourceCosts.deferredLoadResourceCosts();
        }
    }
}