package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class SetFangsCircle extends Ability {

    public static final int CD_MAX_SECONDS = 7;

    private final EvokerUnit evokerUnit;

    public SetFangsCircle(EvokerUnit evokerUnit) {
        super(UnitAction.SET_FANGS_CIRCLE,
            evokerUnit.level,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            EvokerUnit.FANGS_RANGE_CIRCLE,
            0,
            true
        );
        this.evokerUnit = evokerUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton("Evoker Fangs (Circular)",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/shears.png"),
            hotkey,
            () -> !evokerUnit.isUsingLineFangs,
            () -> false,
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.SET_FANGS_CIRCLE),
            null,
            List.of(FormattedCharSequence.forward(
                    I18n.get("abilities.reignofnether.evoker_fangs_circular"),
                    Style.EMPTY.withBold(true)
                ),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.evoker_fangs_circular.tooltip1",
                    EvokerUnit.FANGS_DAMAGE * 2,
                    CD_MAX_SECONDS
                ) + EvokerUnit.FANGS_RANGE_CIRCLE, MyRenderer.iconStyle),
                FormattedCharSequence.forward(
                    I18n.get("abilities.reignofnether.evoker_fangs_circular.tooltip2"),
                    Style.EMPTY
                ),
                FormattedCharSequence.forward(
                    I18n.get("abilities.reignofnether.evoker_fangs_circular.tooltip3"),
                    Style.EMPTY
                )
            ),
            this
        );
    }

    public void setCooldownSingle(float cooldown) {
        super.setCooldown(cooldown);
    }

    @Override
    public void setCooldown(float cooldown) {
        if (evokerUnit.hasVigorEnchant())
            cooldown *= EnchantVigor.cooldownMultiplier;

        super.setCooldown(cooldown);
        for (Ability ability : this.evokerUnit.getAbilities())
            if (ability instanceof SetFangsLine ab) {
                ab.setCooldownSingle(cooldown);
            }
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        evokerUnit.isUsingLineFangs = false;
    }

    @Override
    public boolean canBypassCooldown() {
        return true;
    }

    @Override
    public boolean shouldResetBehaviours() {
        return false;
    }
}
