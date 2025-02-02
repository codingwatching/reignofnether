package com.solegendary.reignofnether.hud;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.buildings.villagers.*;
import com.solegendary.reignofnether.gamemode.ClientGameModeHelper;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerboundPacket;
import com.solegendary.reignofnether.unit.units.monsters.ZombieVillagerUnit;
import com.solegendary.reignofnether.unit.units.piglins.GruntUnit;
import com.solegendary.reignofnether.unit.units.villagers.VillagerUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class HudSandboxClientEvents {

    // NONE == neutral
    private static Faction buildingFaction = Faction.NONE;

    public static List<AbilityButton> getNeutralBuildingButtons() {
        return List.of(
            TownCentre.getBuildButton(Keybindings.keyQ)
        );
    }

    public static List<AbilityButton> getBuildingButtons() {
        return switch (buildingFaction) {
            case VILLAGERS -> VillagerUnit.getBuildingButtons();
            case MONSTERS -> ZombieVillagerUnit.getBuildingButtons();
            case PIGLINS -> GruntUnit.getBuildingButtons();
            case NONE -> getNeutralBuildingButtons();
        };
    }

    private static String getFactionName() {
        return switch (buildingFaction) {
            case VILLAGERS -> "Villagers";
            case MONSTERS -> "Monsters";
            case PIGLINS -> "Piglins";
            case NONE -> "Neutral";
        };
    }

    public static Button getToggleBuildingFactionButton() {
        return new Button(
                "Toggle Faction Buildings",
                Button.itemIconSize,
                new ResourceLocation("minecraft", "textures/block/crafting_table_front.png"),
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    switch (buildingFaction) {
                        case VILLAGERS -> buildingFaction = Faction.MONSTERS;
                        case MONSTERS -> buildingFaction = Faction.PIGLINS;
                        case PIGLINS -> buildingFaction = Faction.NONE;
                        case NONE -> buildingFaction = Faction.VILLAGERS;
                    }
                },
                ClientGameModeHelper::cycleGameMode,
                List.of(
                        FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_faction1", getFactionName()), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_faction2"), Style.EMPTY)
                )
        );
    }

    public static Button getToggleBuildingCheatsButton() {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player == null)
            return null;
        boolean hasCheats = ResearchClient.hasCheat("warpten") &&
                            ResearchClient.hasCheat("modifythephasevariance");
        String playerName = Minecraft.getInstance().player.getName().getString();
        return new Button(
                "Toggle Building Cheats",
                Button.itemIconSize,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/command_block_side.png"),
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    if (hasCheats) {
                        ResearchServerboundPacket.removeCheat(playerName, "warpten");
                        ResearchServerboundPacket.removeCheat(playerName, "modifythephasevariance");
                    } else {
                        ResearchServerboundPacket.addCheat(playerName, "warpten");
                        ResearchServerboundPacket.addCheat(playerName, "modifythephasevariance");
                    }
                },
                ClientGameModeHelper::cycleGameMode,
                List.of(hasCheats ? FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_cheats_on"), Style.EMPTY) :
                                    FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_cheats_off"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_cheats1"), Style.EMPTY)
                )
        );
    }
}
