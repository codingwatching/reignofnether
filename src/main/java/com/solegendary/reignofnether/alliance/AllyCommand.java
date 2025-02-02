package com.solegendary.reignofnether.alliance;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.solegendary.reignofnether.sounds.SoundAction;
import com.solegendary.reignofnether.sounds.SoundClientEvents;
import com.solegendary.reignofnether.sounds.SoundClientboundPacket;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.commands.arguments.EntityArgument;

public class AllyCommand {

    public static final Map<String, String> pendingAlliances = new HashMap<>();
    public static final Set<UUID> pendingDisbands = new HashSet<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ally")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(AllyCommand::ally)));

        dispatcher.register(Commands.literal("allyconfirm")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(AllyCommand::allyConfirm)));

        dispatcher.register(Commands.literal("disband")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(AllyCommand::disband)));
    }

    private static int ally(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer allyPlayer = EntityArgument.getPlayer(context, "player");
        String playerName = player.getName().getString();
        String allyPlayerName = allyPlayer.getName().getString();

        if (player.equals(allyPlayer)) {
            player.sendSystemMessage(Component.translatable("alliance.reignofnether.ally_self", playerName));
            return 0;
        }
        pendingAlliances.put(allyPlayerName, playerName);
        context.getSource().sendSuccess(Component.translatable("alliance.reignofnether.sent_request", allyPlayerName), false);
        SoundClientboundPacket.playSoundForPlayer(SoundAction.CHAT, allyPlayerName);
        allyPlayer.sendSystemMessage(Component.translatable("alliance.reignofnether.ally_confirm", playerName, playerName));
        SoundClientboundPacket.playSoundForPlayer(SoundAction.CHAT, playerName);

        return Command.SINGLE_SUCCESS;
    }

    private static int allyConfirm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer requesterPlayer = EntityArgument.getPlayer(context, "player");
        String playerName = player.getName().getString();
        String requesterPlayerName = requesterPlayer.getName().getString();

        if (pendingAlliances.getOrDefault(playerName, "").equals(requesterPlayerName)) {
            AllianceSystem.addAlliance(playerName, requesterPlayerName);
            pendingAlliances.remove(playerName);

            context.getSource().sendSuccess(Component.translatable("alliance.reignofnether.now_allied", requesterPlayerName), false);
            SoundClientboundPacket.playSoundForPlayer(SoundAction.ALLY, requesterPlayerName);
            requesterPlayer.sendSystemMessage(Component.translatable("alliance.reignofnether.ally_accepted", playerName));
            SoundClientboundPacket.playSoundForPlayer(SoundAction.ALLY, playerName);
        } else {
            context.getSource().sendFailure(Component.translatable("alliance.reignofnether.no_request", requesterPlayerName));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int disband(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer allyPlayer = EntityArgument.getPlayer(context, "player");
        String playerName = player.getName().getString();
        String allyPlayerName = allyPlayer.getName().getString();

        if (player.equals(allyPlayer)) {
            context.getSource().sendFailure(Component.translatable("alliance.reignofnether.disband_self"));
            return 0;
        }

        UUID playerId = player.getUUID();
        if (pendingDisbands.contains(playerId)) {
            context.getSource().sendFailure(Component.translatable("alliance.reignofnether.disband_pending", allyPlayerName));
            return 0;
        }

        pendingDisbands.add(playerId);
        scheduler.schedule(() -> {
            if (pendingDisbands.remove(playerId)) {
                AllianceSystem.removeAlliance(playerName, allyPlayerName);

                player.sendSystemMessage(Component.translatable("alliance.reignofnether.disbanded", allyPlayerName));
                SoundClientboundPacket.playSoundForPlayer(SoundAction.ENEMY, playerName);
                allyPlayer.sendSystemMessage(Component.translatable("alliance.reignofnether.disbanded", playerName));
                SoundClientboundPacket.playSoundForPlayer(SoundAction.ENEMY, allyPlayerName);
            }
        }, 30, TimeUnit.SECONDS);

        context.getSource().sendSuccess(Component.translatable("alliance.reignofnether.disbanding", allyPlayerName), false);
        SoundClientboundPacket.playSoundForPlayer(SoundAction.ENEMY, playerName);
        allyPlayer.sendSystemMessage(Component.translatable("alliance.reignofnether.disbanding", playerName));
        SoundClientboundPacket.playSoundForPlayer(SoundAction.ENEMY, allyPlayerName);

        return Command.SINGLE_SUCCESS;
    }
}
