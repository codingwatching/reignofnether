package com.solegendary.reignofnether.player;

import com.solegendary.reignofnether.gamemode.ClientGameModeHelper;
import com.solegendary.reignofnether.gamemode.GameMode;
import com.solegendary.reignofnether.gamemode.GameModeClientboundPacket;
import com.solegendary.reignofnether.gamemode.GameModeServerboundPacket;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.sounds.SoundAction;
import com.solegendary.reignofnether.sounds.SoundClientboundPacket;
import com.solegendary.reignofnether.survival.SurvivalClientEvents;
import com.solegendary.reignofnether.survival.SurvivalServerEvents;
import com.solegendary.reignofnether.survival.SurvivalServerboundPacket;
import com.solegendary.reignofnether.survival.WaveDifficulty;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class PlayerServerboundPacket {
    PlayerAction action;
    public int playerId;
    public double x;
    public double y;
    public double z;

    public static void teleportPlayer(Double x, Double y, Double z) {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null) {
            PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(PlayerAction.TELEPORT,
                MC.player.getId(),
                x,
                y,
                z
            ));
        }
    }

    public static void enableOrthoview() {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null) {
            PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(PlayerAction.ENABLE_ORTHOVIEW,
                MC.player.getId(),
                0d,
                0d,
                0d
            ));
        }
    }

    public static void disableOrthoview() {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null) {
            PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(PlayerAction.DISABLE_ORTHOVIEW,
                MC.player.getId(),
                0d,
                0d,
                0d
            ));
        }
    }

    public static void startRTS(Faction faction, Double x, Double y, Double z) {

        Minecraft MC = Minecraft.getInstance();

        if (MC.player != null && MC.level != null) {
            BlockState bs = MC.level.getBlockState(new BlockPos(x, y, z));
            if (bs.getMaterial().isLiquid() && faction != Faction.NONE) {
                HudClientEvents.showTemporaryMessage(I18n.get("hud.reignofnether.invalid_start_location"));
                return;
            }
            PlayerAction playerAction = switch (faction) {
                case VILLAGERS -> PlayerAction.START_RTS_VILLAGERS;
                case MONSTERS -> PlayerAction.START_RTS_MONSTERS;
                case PIGLINS -> PlayerAction.START_RTS_PIGLINS;
                case NONE -> PlayerAction.START_RTS_SANDBOX;
            };
            PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(playerAction, MC.player.getId(), x, y, z));
            GameModeServerboundPacket.setAndLockAllClientGameModes(ClientGameModeHelper.gameMode);
            if (ClientGameModeHelper.gameMode == GameMode.SURVIVAL) {
                SurvivalServerboundPacket.startSurvivalMode(SurvivalClientEvents.difficulty);

                CompletableFuture.delayedExecutor(3000, TimeUnit.MILLISECONDS).execute(() -> {
                    WaveDifficulty diff = SurvivalClientEvents.difficulty;
                    String diffMsg = I18n.get("hud.gamemode.reignofnether.survival4",
                            diff, SurvivalClientEvents.getMinutesPerDay()).toLowerCase();
                    diffMsg = diffMsg.substring(0,1).toUpperCase() + diffMsg.substring(1);
                    MC.player.sendSystemMessage(Component.literal(""));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.survival1").withStyle(Style.EMPTY.withBold(true)));
                    MC.player.sendSystemMessage(Component.literal(diffMsg));
                    MC.player.sendSystemMessage(Component.literal(new String(new char[diffMsg.length()]).replace("\0", "-")));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.survival2"));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.survival3"));
                    MC.player.sendSystemMessage(Component.literal(""));
                });
            } else if (ClientGameModeHelper.gameMode == GameMode.CLASSIC) {
                CompletableFuture.delayedExecutor(3000, TimeUnit.MILLISECONDS).execute(() -> {
                    MC.player.sendSystemMessage(Component.literal(""));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.classic1").withStyle(Style.EMPTY.withBold(true)));
                    MC.player.sendSystemMessage(Component.literal("--------"));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.classic2"));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.classic3"));
                    MC.player.sendSystemMessage(Component.literal(""));
                });
            } else if (ClientGameModeHelper.gameMode == GameMode.SANDBOX) {
                CompletableFuture.delayedExecutor(3000, TimeUnit.MILLISECONDS).execute(() -> {
                    MC.player.sendSystemMessage(Component.literal(""));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.sandbox1").withStyle(Style.EMPTY.withBold(true)));
                    MC.player.sendSystemMessage(Component.literal("--------"));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.sandbox2"));
                    MC.player.sendSystemMessage(Component.translatable("hud.gamemode.reignofnether.sandbox3"));
                    MC.player.sendSystemMessage(Component.literal(""));
                });
            }
        }
    }

    public static void resetRTS() {
        PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(PlayerAction.RESET_RTS, 0, 0d, 0d, 0d));
    }

    public static void surrender() {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null) {
            PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(
                PlayerAction.DEFEAT,
                MC.player.getId(),
                0d,
                0d,
                0d
            ));
        }
    }

    public static void lockRTS() {
        PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(PlayerAction.LOCK_RTS, 0, 0d, 0d, 0d));
    }

    public static void unlockRTS() {
        PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(PlayerAction.UNLOCK_RTS, 0, 0d, 0d, 0d));
    }

    public static void enableRTSSyncing() {
        PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(
            PlayerAction.ENABLE_RTS_SYNCING,
            0,
            0d,
            0d,
            0d
        ));
    }

    public static void disableRTSSyncing() {
        PacketHandler.INSTANCE.sendToServer(new PlayerServerboundPacket(
            PlayerAction.DISABLE_RTS_SYNCING,
            0,
            0d,
            0d,
            0d
        ));
    }

    // packet-handler functions
    public PlayerServerboundPacket(PlayerAction action, int playerId, Double x, Double y, Double z) {
        this.action = action;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PlayerServerboundPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(PlayerAction.class);
        this.playerId = buffer.readInt();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        buffer.writeInt(this.playerId);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            switch (action) {
                case TELEPORT -> PlayerServerEvents.movePlayer(this.playerId, this.x, this.y, this.z);
                case ENABLE_ORTHOVIEW -> PlayerServerEvents.enableOrthoview(this.playerId);
                case DISABLE_ORTHOVIEW -> PlayerServerEvents.disableOrthoview(this.playerId);
                case START_RTS_VILLAGERS ->
                    PlayerServerEvents.startRTS(this.playerId, new Vec3(this.x, this.y, this.z), Faction.VILLAGERS);
                case START_RTS_MONSTERS ->
                    PlayerServerEvents.startRTS(this.playerId, new Vec3(this.x, this.y, this.z), Faction.MONSTERS);
                case START_RTS_PIGLINS ->
                    PlayerServerEvents.startRTS(this.playerId, new Vec3(this.x, this.y, this.z), Faction.PIGLINS);
                case START_RTS_SANDBOX ->
                    PlayerServerEvents.startRTS(this.playerId, new Vec3(this.x, this.y, this.z), Faction.NONE);
                case DEFEAT -> PlayerServerEvents.defeat(this.playerId, Component.translatable("server.reignofnether.surrendered").getString());
                case RESET_RTS -> PlayerServerEvents.resetRTS();
                case LOCK_RTS -> PlayerServerEvents.setRTSLock(true);
                case UNLOCK_RTS -> PlayerServerEvents.setRTSLock(false);
                case ENABLE_RTS_SYNCING -> PlayerServerEvents.setRTSSyncingEnabled(true);
                case DISABLE_RTS_SYNCING -> PlayerServerEvents.setRTSSyncingEnabled(false);
            }
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}