package com.solegendary.reignofnether.config;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

//TODO: Merge into ConfigServerEvents by manually adding event listeners in the main mod class
public class ConfigVanillaServerEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        Supplier<ServerPlayer> serverPlayer = () -> (ServerPlayer) evt.getEntity();
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(serverPlayer), new ClientboundSyncConfigPacket(ConfigServerEvents.costList));
        System.out.println("Attempted to send packet");
    }

}
