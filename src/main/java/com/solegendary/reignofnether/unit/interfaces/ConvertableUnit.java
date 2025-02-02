package com.solegendary.reignofnether.unit.interfaces;

import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ConvertableUnit {

    public boolean shouldDiscard();
    public void setShouldDiscard(boolean shouldDiscard);

    // returns the new unit's id
    public default LivingEntity convertToUnit(EntityType<? extends Unit> entityType) {
        Unit oldUnit = (Unit) this;
        LivingEntity oldEntity = (LivingEntity) this;
        if (oldEntity.getLevel().isClientSide())
            return null;

        ServerLevel level = (ServerLevel) oldEntity.getLevel();
        LivingEntity newEntity = (LivingEntity) entityType.create(level);

        if (newEntity == null)
            return null;

        float maxHealthDiff = newEntity.getMaxHealth() - oldEntity.getMaxHealth();

        newEntity.setHealth(Math.max(1, oldEntity.getHealth() + maxHealthDiff));
        for (MobEffectInstance effect : oldEntity.getActiveEffects())
            newEntity.addEffect(effect);

        newEntity.copyPosition(oldEntity);
        ((Unit) newEntity).setOwnerName(oldUnit.getOwnerName());
        level.addFreshEntity(newEntity);

        for (ItemStack item : oldUnit.getItems())
            ((Unit) newEntity).getItems().add(item);

        UnitSyncClientboundPacket.sendSyncResourcesPacket((Unit) newEntity);

        Entity vehicle = oldEntity.getVehicle();
        if (vehicle != null) {
            oldEntity.stopRiding();
            newEntity.startRiding(vehicle, true);
        }
        if (oldEntity.isVehicle()) {
            Entity passenger = oldEntity.getFirstPassenger();
            if (passenger != null) {
                passenger.stopRiding();
                passenger.startRiding(newEntity, true);
            }
        }
        newEntity.setYRot(oldEntity.getYRot());

        // discard with a reflected packet so the client has a chance to sync goals, command groups and selections
        //oldEntity.discard();
        return newEntity;
    }
}
