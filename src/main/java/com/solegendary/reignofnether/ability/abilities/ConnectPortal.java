package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ConnectPortal extends Ability {

    private static final int CD_MAX = 0;
    private static final int RANGE = 0;

    Building building;

    public ConnectPortal(Building building) {
        super(UnitAction.CONNECT_PORTAL, building.getLevel(), CD_MAX, RANGE, 0, true);
        this.building = building;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton("Connect Portal",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/portal.png"),
            hotkey,
            () -> false,
            () -> {
                if (building instanceof Portal portal) {
                    return portal.portalType != Portal.PortalType.TRANSPORT;
                }
                return true;
            },
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.CONNECT_PORTAL),
            null,
            List.of(FormattedCharSequence.forward(
                    I18n.get("abilities.reignofnether.connect_portal"),
                    Style.EMPTY.withBold(true)
                ),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.connect_portal.tooltip1"), Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.connect_portal.tooltip2"), Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Building buildingUsing, BlockPos targetBp) {

        if (building instanceof Portal portal && portal.portalType == Portal.PortalType.TRANSPORT) {
            portal.disconnectPortal();

            Building targetBuilding = BuildingUtils.findBuilding(level.isClientSide(), targetBp);
            if (targetBuilding instanceof Portal targetPortal && targetPortal.portalType == Portal.PortalType.TRANSPORT
                && targetBuilding != building && targetBuilding.isBuilt
                && targetBuilding.ownerName.equals(building.ownerName)) {
                targetPortal.disconnectPortal();
                targetPortal.destination = portal.centrePos;
                portal.destination = targetPortal.centrePos;
            } else if (level.isClientSide()) {
                HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.connect_portal.error1"));
            }
        }
    }
}
