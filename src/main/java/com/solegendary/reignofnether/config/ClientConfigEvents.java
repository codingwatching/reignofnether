package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.buildings.monsters.Dungeon;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.monsters.CreeperProd;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientConfigEvents {
    private static final Minecraft MC = Minecraft.getInstance();
    //Load config data from server
    public static void loadConfigData(ClientboundSyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        //This doesn't seem to do anything
        MC.player.sendSystemMessage(Component.literal("Packet sent"));
        ResourceCosts.CREEPER = ResourceCost.Unit(20,20,20,20,20);
        CreeperProd.cost = ResourceCosts.CREEPER;

        List<Building> buildingList = BuildingClientEvents.getBuildings();
        for (Building building : buildingList) {
            building.rebakeButtons();
        }
        //TODO: use msg.getResourceCosts() and manually assign each ResourceCost to associated ResourceCosts field
        //List<ResourceCost> resourceCosts = msg.getResourceCosts();
        //System.out.println("Resource Costs: " + resourceCosts);
    }
}