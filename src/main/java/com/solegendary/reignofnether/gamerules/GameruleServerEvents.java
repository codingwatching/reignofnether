package com.solegendary.reignofnether.gamerules;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.solegendary.reignofnether.gamemode.GameModeClientboundPacket;
import com.solegendary.reignofnether.player.PlayerClientboundPacket;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;

public class GameruleServerEvents {

    @SubscribeEvent
    public static void onCommandUsed(CommandEvent evt) {
        List<ParsedCommandNode<CommandSourceStack>> nodes = evt.getParseResults().getContext().getNodes();
        if (nodes.size() <= 1)
            return;
        if (!nodes.get(0).getNode().getName().equals("gamerule"))
            return;

        if (nodes.size() >= 3 &&
            nodes.get(1).getNode().getName().equals("disallowWaveSurvival")) {

            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                boolean value = (boolean) args.get("value").getResult();
                if (value)
                    GameModeClientboundPacket.disallowSurvival();
                else
                    GameModeClientboundPacket.allowSurvival();
            }
        }
        else if (nodes.size() >= 2 &&
                nodes.get(1).getNode().getName().equals("maxPopulation")) {

            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                UnitServerEvents.maxPopulation = (int) args.get("value").getResult();
                PlayerClientboundPacket.syncMaxPopulation(UnitServerEvents.maxPopulation);
            }
        }
    }
}
