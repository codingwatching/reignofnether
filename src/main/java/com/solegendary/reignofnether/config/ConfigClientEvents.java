package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.units.monsters.CreeperProd;
import net.minecraft.client.Minecraft;
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

    //TODO: We should NEVER be replacing the ResourceCost references stored in ResourceCosts; instead, we need to implement methods in
    //TODO: ResourceCost that allow us to change the object values without replacing the reference the variable holds!
    //TODO: From there, we should dynamically iterate through a Hashmap containing references to ResourceCosts and ResourceCostConfigEntries
    //TODO: and *update* the values of the resourcecost from the resourcecostconfigentries.
    private static final Minecraft MC = Minecraft.getInstance();
    //Load config data from server
    public static void loadConfigData(ClientboundSyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        MC.player.sendSystemMessage(Component.literal("Packet sent"));
        //ResourceCosts.CREEPER = ResourceCost.Unit(25,25,25,25,25);
        //CreeperProd.cost = ResourceCosts.CREEPER;

        List<Building> buildingList = BuildingClientEvents.getBuildings();
        for (Building building : buildingList) {
            building.rebakeButtons();
        }
        //TODO: use msg.getResourceCosts() and manually assign each ResourceCost to associated ResourceCosts field
        //List<ResourceCost> resourceCosts = msg.getResourceCosts();
        //System.out.println("Resource Costs: " + resourceCosts);
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        ReignOfNether.LOGGER.info("onPlayerJoined fired from ClientConfigEvents");
        //If we own this singleplayer world
        if (evt.getEntity().getServer().isSingleplayerOwner(evt.getEntity().getGameProfile())) {
            //rebake from clientsideside configs
            ReignOfNether.LOGGER.info("Attempting to rebake from client..");
            ReignOfNether.LOGGER.info("DEBUG - LIST OF RESOURCE COSTS: ");
            for(ResourceCostConfigEntry entry : ResourceCostConfigEntry.ENTRIES) {
                ReignOfNether.LOGGER.info(entry.id);
            }
            ReignOfNether.LOGGER.info(ResourceCostConfigEntry.ENTRIES);
            for (String key : ResourceCost.ENTRIES.keySet()) {
                System.out.println("Key: " + key + ", Value: " + ResourceCost.ENTRIES.get(key));
            }
            ResourceCosts.deferredLoadResourceCosts();
            //CreeperProd.cost = ResourceCosts.CREEPER;
            List<Building> buildingList = BuildingClientEvents.getBuildings();
            List<LivingEntity> unitList = UnitClientEvents.getAllUnits();
            for (Building building : buildingList) {
                building.rebakeButtons();
            }
            for (LivingEntity unit : unitList) {
                //unit.rebakeButtons();
            }
        }
    }
}