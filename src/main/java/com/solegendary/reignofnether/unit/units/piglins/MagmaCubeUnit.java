package com.solegendary.reignofnether.unit.units.piglins;

import com.solegendary.reignofnether.blocks.BlockServerEvents;
import com.solegendary.reignofnether.registrars.BlockRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchCubeMagma;
import com.solegendary.reignofnether.unit.goals.AbstractMeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class MagmaCubeUnit extends SlimeUnit implements Unit, AttackerUnit {

    final static public int MAX_SIZE = 6;
    final static private int SET_FIRE_TICKS_MAX = 20;
    private int setFireTicks = 0;
    protected boolean shouldSplitOnDeath = false; // prevent split on death without changing size

    @Override protected ParticleOptions getParticleType() {
        return ParticleTypes.FLAME;
    }
    @Override public boolean isOnFire() {
        return false;
    }

    public MagmaCubeUnit consumeTarget = null;

    public MagmaCubeUnit(EntityType<? extends SlimeUnit> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected int getMaxHealthForSize(int size) {
        if (size >= 6)
            return 160;
        else if (size == 5)
            return 130;
        else if (size == 4)
            return 100;
        else if (size == 3)
            return 70;
        else if (size == 2)
            return 40;
        else
            return 20;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_HURT_SMALL : SoundEvents.MAGMA_CUBE_HURT;
    }
    protected SoundEvent getDeathSound() {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_DEATH_SMALL : SoundEvents.MAGMA_CUBE_DEATH;
    }
    protected SoundEvent getSquishSound() {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_SQUISH_SMALL : SoundEvents.MAGMA_CUBE_SQUISH;
    }

    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            setFireTicks += 1;
            if (setFireTicks >= SET_FIRE_TICKS_MAX) {
                setFireTicks = 0;
                createMagma();
            }
        }
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        super.checkFallDamage(pY, pOnGround, pState, pPos);
        if (!level.isClientSide() && pOnGround && !wasOnGround)
            createMagma();
    }

    private static final int FIRE_DURATION_PER_SIZE = 40;

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        boolean result = super.doHurtTarget(pEntity);
        if (result && getSize() >= 2) {
            pEntity.setRemainingFireTicks(FIRE_DURATION_PER_SIZE * getSize());
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (getSize() >= 2 && pSource.getEntity() instanceof AttackerUnit aUnit && aUnit.getAttackGoal() instanceof AbstractMeleeAttackUnitGoal)
            pSource.getEntity().setRemainingFireTicks((FIRE_DURATION_PER_SIZE * getSize()) / 2);

        if (pSource == DamageSource.ON_FIRE ||
            pSource == DamageSource.IN_FIRE ||
            pSource == DamageSource.LAVA)
            return false;

        boolean result = super.hurt(pSource, pAmount);

        int newSize = getSizeForHealth(getHealth());
        if (newSize != getSize())
            setSize(newSize, false);

        return result;
    }

    private static final int MAGMA_DURATION = 100;

    public void createMagma() {
        if (getSize() < 4 || level.isClientSide())
            return;

        if (!ResearchServerEvents.playerHasResearch(getOwnerName(), ResearchCubeMagma.itemName))
            return;

        BlockState bsToPlace = BlockRegistrar.WALKABLE_MAGMA_BLOCK.get().defaultBlockState();
        BlockPos bpOn = getOnPos();
        if (level.getBlockState(bpOn).isAir())
            return;

        ArrayList<BlockPos> bps = new ArrayList<>();
        if (getSize() >= 2) {
            bps.add(bpOn);
        }
        if (getSize() >= 3) {
            bps.add(bpOn.north());
            bps.add(bpOn.east());
            bps.add(bpOn.south());
            bps.add(bpOn.west());
        }
        if (getSize() >= 4) {
            bps.add(bpOn.north().east());
            bps.add(bpOn.south().west());
            bps.add(bpOn.north().west());
            bps.add(bpOn.south().east());
        }
        if (getSize() >= 5) {
            bps.add(bpOn.north().north());
            bps.add(bpOn.south().south());
            bps.add(bpOn.east().east());
            bps.add(bpOn.west().west());
        }
        if (getSize() >= 6) {
            bps.add(bpOn.north().north().east());
            bps.add(bpOn.south().south().east());
            bps.add(bpOn.north().north().west());
            bps.add(bpOn.south().south().west());
            bps.add(bpOn.east().east().south());
            bps.add(bpOn.west().west().south());
            bps.add(bpOn.east().east().north());
            bps.add(bpOn.west().west().north());
        }

        // Frostwalker effect provided in LivingEntityMixin, but it only happens on changing block positions on the ground
        for (BlockPos bp : bps) {
            BlockState bsOld = level.getBlockState(bp);
            if (bsOld.getMaterial().isSolidBlocking()) {
                BlockServerEvents.addTempBlock((ServerLevel) level, bp,
                    BlockRegistrar.WALKABLE_MAGMA_BLOCK.get().defaultBlockState(), bsOld, MAGMA_DURATION);
            }
        }
    }
    public void createFire() {
        if (getSize() < MAX_SIZE || level.isClientSide())
            return;

        ArrayList<BlockPos> bps = new ArrayList<>();
        for (int x = -4; x < 4; x++)
            for (int y = -4; y < 4; y++)
                for (int z = -4; z < 4; z++)
                    if (level.getBlockState(getOnPos().offset(x,y,z)).getBlock() ==
                            BlockRegistrar.WALKABLE_MAGMA_BLOCK.get() &&
                            level.getBlockState(getOnPos().offset(x,y+1,z)).isAir())
                        bps.add(getOnPos().offset(x,y,z));
        Collections.shuffle(bps);
        if (bps.size() >= 1)
            level.setBlockAndUpdate(bps.get(0).above(), Blocks.FIRE.defaultBlockState());
    }
}
