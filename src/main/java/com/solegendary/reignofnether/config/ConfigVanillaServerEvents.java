package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

//TODO: Merge into ConfigServerEvents by manually adding event listeners in the main mod class
public class ConfigVanillaServerEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        Player player = evt.getEntity();
        Supplier<ServerPlayer> serverPlayerSupplier = () -> (ServerPlayer) evt.getEntity();
        ReignOfNether.LOGGER.info("onPlayerJoined fired from ConfigVanillaServerEvents");
        ReignOfNether.LOGGER.info(player.level.isClientSide());
        if (player.level.isClientSide()) {
            //rebake from clientside configs
            ResourceCosts.deferredLoadResourceCosts();
        }
        else {
            //rebake from serverside configs
            System.out.println("Attempted to send packet");
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(serverPlayerSupplier), new ClientboundSyncConfigPacket(ConfigServerEvents.costList));
        }
    }
}
