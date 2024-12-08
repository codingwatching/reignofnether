package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.client.Minecraft;
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

    //TODO: We should NEVER be replacing the ResourceCost references stored in ResourceCosts; instead, we need to implement methods in
    //TODO: ResourceCost that allow us to change the object values without replacing the reference the variable holds!
    //TODO: From there, we should dynamically iterate through a Hashmap containing references to ResourceCosts and ResourceCostConfigEntries
    //TODO: and *update* the values of the resourcecost from the resourcecostconfigentries.
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
            rescost.ticks = msg.getTicks() * ResourceCost.TICKS_PER_SECOND;
            rescost.population = msg.getPopulation();
        }

        //TODO: Move to separate packet; this fires like 100 billion times as-is
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