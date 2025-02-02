package com.solegendary.reignofnether.gamemode;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.player.RTSPlayer;
import com.solegendary.reignofnether.registrars.GameRuleRegistrar;
import com.solegendary.reignofnether.survival.SurvivalServerEvents;
import com.solegendary.reignofnether.util.Faction;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GameModeServerEvents {

    private static GameMode getGameMode() {
        for (RTSPlayer rtsPlayer : PlayerServerEvents.rtsPlayers)
            if (rtsPlayer.faction == Faction.NONE)
                return GameMode.SANDBOX;

        if (SurvivalServerEvents.isEnabled())
            return GameMode.SURVIVAL;

        return GameMode.CLASSIC;
    }

    private static boolean isGameModeLocked() {
        return !PlayerServerEvents.rtsPlayers.isEmpty();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (isGameModeLocked())
            GameModeClientboundPacket.setAndLockAllClientGameModes(getGameMode());
        if (evt.getEntity().getLevel().getGameRules().getRule(GameRuleRegistrar.DISALLOW_WAVE_SURVIVAL).get())
            GameModeClientboundPacket.disallowSurvival();
    }
}
