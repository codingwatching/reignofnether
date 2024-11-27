package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.monsters.CreeperProd;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientConfigEvents {
    private static final Minecraft MC = Minecraft.getInstance();
    //Load config data from server
    public static void loadConfigData(ClientboundSyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        System.out.println("PACKET SENT");
        //This doesn't seem to do anything
        ResourceCosts.CREEPER = ResourceCost.Unit(5,5,5,5,5);
        CreeperProd.cost = ResourceCosts.CREEPER;
        //TODO: use msg.getResourceCosts() and manually assign each ResourceCost to associated ResourceCosts field
        //List<ResourceCost> resourceCosts = msg.getResourceCosts();
        //System.out.println("Resource Costs: " + resourceCosts);
    }
}