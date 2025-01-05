package com.solegendary.reignofnether.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

// lines and boxes drawn to entities and blocks to indicate unit intent like movement
public class Checkpoint {

    public static final int CHECKPOINT_TICKS_MAX = 200;
    public static final int CHECKPOINT_TICKS_FADE = 15;

    public BlockPos bp;
    public Entity entity;
    public boolean isGreen;

    int ticksLeft = CHECKPOINT_TICKS_MAX;

    public Checkpoint(BlockPos bp, boolean isGreen) {
        this.bp = bp;
        this.entity = null;
        this.isGreen = isGreen;
    }

    public Checkpoint(Entity entity, boolean isGreen) {
        this.bp = null;
        this.entity = entity;
        this.isGreen = isGreen;
    }

    public void tick() {
        if (ticksLeft > 0)
            ticksLeft -= 1;
    }

    public boolean isForEntity() {
        return entity == null;
    }

    public Vec3 getPos() {
        if (isForEntity())
            return entity.position();
        else
            return Vec3.atCenterOf(bp);
    }
}
