package com.solegendary.reignofnether.hud.buttons;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

import static com.solegendary.reignofnether.hud.HudClientEvents.hudSelectedBuilding;
import static com.solegendary.reignofnether.unit.UnitClientEvents.getPlayerToEntityRelationship;
import static com.solegendary.reignofnether.unit.UnitClientEvents.idleWorkerIds;

public class HelperButtons {

    private static final Minecraft MC = Minecraft.getInstance();

    public static final int ICON_SIZE = 14;

    public static final Button chatButton = new Button(
            "Chat",
            ICON_SIZE,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/book.png"),
            (Keybinding) null,
            () -> false,
            () -> false,
            () -> true,
            () -> {
                MC.setScreen(new ChatScreen(""));
            },
            null,
            List.of(FormattedCharSequence.forward(I18n.get("hud.helperbuttons.reignofnether.chat"), Style.EMPTY))
    );

    private static int idleWorkerIndex = 0;

    public static final Button idleWorkerButton = new Button(
            "Idle workers (CTRL-click to select all)",
            ICON_SIZE,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/villager.png"),
            Keybindings.keyJ,
            () -> false,
            idleWorkerIds::isEmpty,
            () -> true,
            () -> {
                if (MC.level == null)
                    return;

                if (Keybindings.ctrlMod.isDown()) {
                    UnitClientEvents.clearSelectedUnits();
                    for (int id : idleWorkerIds) {
                        if (idleWorkerIndex < idleWorkerIds.size()) {
                            Entity entity = MC.level.getEntity(idleWorkerIds.get(idleWorkerIndex));
                            if (entity instanceof WorkerUnit) {
                                UnitClientEvents.addSelectedUnit((LivingEntity) entity);
                            }
                            idleWorkerIndex += 1;
                        } else
                            idleWorkerIndex = 0; // Reset to zero if out of bounds
                    }
                } else {
                    if (idleWorkerIndex >= idleWorkerIds.size())
                        idleWorkerIndex = 0; // Reset to zero if out of bounds

                    Entity entity = MC.level.getEntity(idleWorkerIds.get(idleWorkerIndex));
                    if (entity instanceof WorkerUnit) {
                        OrthoviewClientEvents.centreCameraOnPos(entity.getX(), entity.getZ());
                        UnitClientEvents.clearSelectedUnits();
                        UnitClientEvents.addSelectedUnit((LivingEntity) entity);
                    }
                    idleWorkerIndex += 1;

                    // Reset idleWorkerIndex if it exceeds the size after increment
                    if (idleWorkerIndex >= idleWorkerIds.size())
                        idleWorkerIndex = 0;
                }
            },
            null,
            List.of(FormattedCharSequence.forward(I18n.get("hud.helperbuttons.reignofnether.idle_workers"), Style.EMPTY))
    );

    public static final Button buildingCancelButton = new Button(
            "Cancel",
            ICON_SIZE,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/barrier.png"),
            Keybindings.cancelBuild,
            () -> false,
            () -> {
                if (hudSelectedBuilding == null)
                    return false;
                return BuildingUtils.getTotalCompletedBuildingsOwned(true, hudSelectedBuilding.ownerName) == 0;
            },
            () -> true,
            () -> {
                BuildingServerboundPacket.cancelBuilding(hudSelectedBuilding.minCorner);
                hudSelectedBuilding = null;
            },
            null,
            List.of(FormattedCharSequence.forward(I18n.get("hud.helperbuttons.reignofnether.cancel"), Style.EMPTY))
    );

    public static final Button armyButton = new Button(
            "Select all military units",
            ICON_SIZE,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/sword_and_bow.png"),
            Keybindings.keyK,
            () -> false,
            () -> {
                List<LivingEntity> militaryUnits = UnitClientEvents.getAllUnits().stream()
                        .filter(u -> !(u instanceof WorkerUnit) && getPlayerToEntityRelationship(u) == Relationship.OWNED).toList();
                return militaryUnits.isEmpty();
            },
            () -> true,
            () -> {
                List<LivingEntity> militaryUnits = UnitClientEvents.getAllUnits().stream()
                        .filter(u -> !(u instanceof WorkerUnit) && getPlayerToEntityRelationship(u) == Relationship.OWNED).toList();
                UnitClientEvents.clearSelectedUnits();
                for (LivingEntity militaryUnit : militaryUnits)
                    UnitClientEvents.addSelectedUnit(militaryUnit);
                HudClientEvents.setLowestCdHudEntity();
            },
            null,
            List.of(FormattedCharSequence.forward(I18n.get("hud.helperbuttons.reignofnether.select_all_military_units"), Style.EMPTY))
    );
}
