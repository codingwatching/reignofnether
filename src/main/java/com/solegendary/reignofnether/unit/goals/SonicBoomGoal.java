package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.unit.UnitAnimationAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitAnimationClientboundPacket;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;

public class SonicBoomGoal extends AbstractCastTargetedSpellGoal {

    public SonicBoomGoal(Mob mob, int channelTicks, int range,
                         Consumer<LivingEntity> onEntityCast, Consumer<Building> onBuildingCast) {
        super(mob, channelTicks, range, onEntityCast, null, onBuildingCast);
    }

    @Override
    public void startCasting() {
        super.startCasting();
        if (!this.mob.level.isClientSide())
            UnitAnimationClientboundPacket.sendBasicPacket(UnitAnimationAction.NON_KEYFRAME_START, this.mob);
    }

    @Override
    public void stopCasting() {
        if (!this.mob.level.isClientSide() && ticksCasting < channelTicks)
            UnitAnimationClientboundPacket.sendBasicPacket(UnitAnimationAction.NON_KEYFRAME_STOP, this.mob);
        super.stopCasting();
        if (this.mob.level.isClientSide() && !Keybindings.shiftMod.isDown())
            ((Unit) this.mob).getCheckpoints().clear();
    }

    @Override
    protected boolean isInRange() {
        int finalRange = range;
        if (isCasting())
            finalRange += 10;

        if (moveTarget != null && MyMath.distance(
                this.mob.getX(), this.mob.getZ(),
                moveTarget.getX(), moveTarget.getZ()) <= finalRange)
            return true;
        if (castTarget != null && MyMath.distance(
                this.mob.getX(), this.mob.getZ(),
                castTarget.getX(), castTarget.getZ()) <= finalRange)
            return true;
        return false;
    }

    @Override
    public void stop() {
        // hack fix to stop a weird bug where it gets stopped unexpectedly (serverside)
        // happens when needing to move towards the target first
        if (this.ticksCasting <= 5 && isInRange())
            return;
        super.stop();
    }
}
