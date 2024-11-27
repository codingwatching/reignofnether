package com.solegendary.reignofnether.config;

import ca.weblite.objc.Client;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.votesystem.networking.ClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/*
    Clientbound packet to synchronize serverside config options with the client
    so that the GUI and other elements can properly reflect the values present on the server.
    TODO: Most of this is commented out because I was worried the codec classes were breaking
    TODO: (admittedly, it was 2am me grasping at straws as to why things weren't working)
    TODO: So, once we manage to figure out how to make visually reflected changes to ResourceCosts on the client
    TODO: from ClientConfigEvents, we can uncomment and work with resourceCosts
 */
public class ClientboundSyncConfigPacket {
    //private final List<ResourceCost> resourceCosts;
    public ClientboundSyncConfigPacket(List<ResourceCost> resourceCosts) {
        //this.resourceCosts = resourceCosts;
    }
    public ClientboundSyncConfigPacket(FriendlyByteBuf buf) {
        /*
        this.resourceCosts = buf.readList(buffer -> {
            int food = buf.readInt();
            int wood = buf.readInt();
            int ore = buf.readInt();
            int ticks = buf.readInt();
            int population = buf.readInt();
            //not actually all units but this is analogous to the private constructor
            return ResourceCost.Unit(food, wood, ore, ticks, population);
        });
         */
    }
    public void encode(FriendlyByteBuf buf) {
        /*
        buf.writeCollection(this.resourceCosts, (buffer, rsc) -> {
            int food = rsc.food;
            int wood = rsc.wood;
            int ore = rsc.ore;
            int ticks = rsc.ticks;
            int population = rsc.population;
            buffer.writeInt(food);
            buffer.writeInt(wood);
            buffer.writeInt(ore);
            buffer.writeInt(ticks);
            buffer.writeInt(population);
        });
         */
    }
    public static ClientboundSyncConfigPacket decode(FriendlyByteBuf buf) {
        return new ClientboundSyncConfigPacket(buf);
    }

    public static boolean handle(ClientboundSyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConfigEvents.loadConfigData(msg, ctx));
            success.set(true);
        });
        context.setPacketHandled(true);
        return success.get();
    }

    /*
    public List<ResourceCost> getResourceCosts() {
        return this.resourceCosts;
    }
     */
}
