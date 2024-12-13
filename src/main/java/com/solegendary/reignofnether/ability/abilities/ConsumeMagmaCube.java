package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.monsters.SculkCatalyst;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.MagmaCubeUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.solegendary.reignofnether.unit.UnitClientEvents.sendUnitCommand;

public class ConsumeMagmaCube extends Ability {

    private static final int CD_MAX = 0;
    private static final int RANGE = 2;

    public ConsumeMagmaCube() {
        super(UnitAction.CONSUME_MAGMA_CUBE, CD_MAX, RANGE, 0, true, true);
        canAutocast = true;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton("Consume Magma Cube",
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/magma_cube.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.CONSUME_MAGMA_CUBE || autocast,
            () -> false,
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.CONSUME_MAGMA_CUBE),
            () -> sendUnitCommand(UnitAction.AUTOCAST),
            List.of(FormattedCharSequence.forward(I18n.get("abilities.reignofnether.consume_magma_cube"),
                    Style.EMPTY.withBold(true)
                ),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.consume_magma_cube.tooltip1"), Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.consume_magma_cube.tooltip2"), Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.consume_magma_cube.tooltip3"), Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) {
        if (unitUsing instanceof MagmaCubeUnit unit &&
            targetEntity instanceof MagmaCubeUnit unitTarget &&
            unit.getOwnerName().equals(unitTarget.getOwnerName())) {
            unit.setUnitAttackTargetForced(unitTarget);
            unit.consumeTarget = unitTarget;
        } else if (level.isClientSide()) {
            if (unitUsing instanceof MagmaCubeUnit unit &&
                unit.getSize() >= MagmaCubeUnit.MAX_SIZE &&
                unit.getHealth() >= unit.getMaxHealth()) {
                HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.consume_magma_cube.error1"));
            } else
                HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.consume_magma_cube.error2"));
        }
    }
}
