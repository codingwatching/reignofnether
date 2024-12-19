package com.solegendary.reignofnether.unit.units.piglins;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.ConsumeMagmaCube;
import com.solegendary.reignofnether.blocks.BlockServerEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.registrars.BlockRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchCubeMagma;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.controls.SlimeUnitMoveControl;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class MagmaCubeUnit extends MagmaCube implements Unit, AttackerUnit {
    // region
    private final ArrayList<BlockPos> checkpoints = new ArrayList<>();
    private int checkpointTicksLeft = UnitClientEvents.CHECKPOINT_TICKS_MAX;
    public ArrayList<BlockPos> getCheckpoints() { return checkpoints; };
    public int getCheckpointTicksLeft() { return checkpointTicksLeft; }
    public void setCheckpointTicksLeft(int ticks) { checkpointTicksLeft = ticks; }
    private boolean isCheckpointGreen = true;
    public boolean isCheckpointGreen() { return isCheckpointGreen; };
    public void setIsCheckpointGreen(boolean green) { isCheckpointGreen = green; };
    private int entityCheckpointId = -1;
    public int getEntityCheckpointId() { return entityCheckpointId; };
    public void setEntityCheckpointId(int id) { entityCheckpointId = id; };

    GarrisonGoal garrisonGoal;
    public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }
    public boolean canGarrison() { return getGarrisonGoal() != null; }

    UsePortalGoal usePortalGoal;
    public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }
    public boolean canUsePortal() { return getUsePortalGoal() != null; }

    public Faction getFaction() {return Faction.PIGLINS;}
    public List<AbilityButton> getAbilityButtons() {return abilityButtons;};
    public List<Ability> getAbilities() {return abilities;}
    public List<ItemStack> getItems() {return items;};
    public MoveToTargetBlockGoal getMoveGoal() {return moveGoal;}
    public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() {return targetGoal;}
    public Goal getAttackBuildingGoal() {return attackBuildingGoal;}
    public Goal getAttackGoal() {return attackGoal;}
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    private ReturnResourcesGoal returnResourcesGoal;

    public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
    public LivingEntity getFollowTarget() { return followTarget; }
    public boolean getHoldPosition() { return holdPosition; }
    public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

    // if true causes moveGoal and attackGoal to work together to allow attack moving
    // moves to a block but will chase/attack nearby monsters in range up to a certain distance away
    private BlockPos attackMoveTarget = null;
    private LivingEntity followTarget = null; // if nonnull, continuously moves to the target
    private boolean holdPosition = false;

    // which player owns this unit? this format ensures its synched to client without having to use packets
    public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
    public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
    public static final EntityDataAccessor<String> ownerDataAccessor =
            SynchedEntityData.defineId(MagmaCubeUnit.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
    }

    // combat stats
    public boolean getWillRetaliate() {return willRetaliate;}
    public int getAttackCooldown() {return (int) (20 / attacksPerSecond);}
    public float getAttacksPerSecond() {return attacksPerSecond;}
    public float getAggroRange() {return aggroRange;}
    public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
    public float getAttackRange() { return ((getSize() + 1) * 0.5f); }
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitArmorValue() {return armorValue;}
    @Nullable
    public int getPopCost() {
        return getSize();
    }
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // endregion

    final static public int STARTING_SIZE = 2;
    final static public int MAX_SIZE = 6;

    final static public float attackDamagePerSize = 2.0f;
    final static public float maxHealthCap = 300;
    final static public float attacksPerSecond = 0.5f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.6f; // needs to be 2x other units
    final static public float aggroRange = 10;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;

    final static private int SET_FIRE_TICKS_MAX = 20;
    private int setFireTicks = 0;

    private boolean forceTiny = false; // prevent split on death

    @Override
    public int getSize() {
        if (forceTiny)
            return 1;
        else
            return super.getSize();
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        forceTiny = true;
        super.remove(pReason);
        forceTiny = false;
    }

    public boolean autocastingConsume() {
        for (Ability ability : abilities)
            if (ability instanceof ConsumeMagmaCube consume)
                return consume.autocast;
        return false;
    }

    public float getUnitAttackDamage() {
        return attackDamagePerSize * getSize();
    }
    public float getUnitMaxHealth() {
        if (getSize() <= 1)
            return 15;
        else
            return 15 + ((getSize() - 1) * 30);
    }

    public float getKnockbackResistance() {
        return getSize() * (1.0f / 6);
    }

    public int maxResources = 0;

    private MeleeAttackUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public MagmaCubeUnit consumeTarget = null;

    public MagmaCubeUnit(EntityType<? extends MagmaCube> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SlimeUnitMoveControl(this);

        ConsumeMagmaCube ab1 = new ConsumeMagmaCube(level);
        this.abilities.add(ab1);
        if (level.isClientSide())
            this.abilityButtons.add(ab1.getButton(Keybindings.keyQ));
    }

    @Override
    public void resetBehaviours() {
        consumeTarget = null;
        for (Ability ability : abilities)
            if (ability instanceof ConsumeMagmaCube consume)
                consume.autocast = false;
    }

    @Override
    public void setSize(int pSize, boolean pResetHealth) {
        int i = Mth.clamp(pSize, 1, MAX_SIZE);
        this.entityData.set(ID_SIZE, i);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getUnitMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getMovementSpeed());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getUnitAttackDamage());
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(getKnockbackResistance());
        if (pResetHealth)
            this.setHealth(this.getMaxHealth());
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    protected boolean isDealsDamage() {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, MagmaCubeUnit.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, MagmaCubeUnit.attackDamagePerSize)
                .add(Attributes.ARMOR, MagmaCubeUnit.armorValue)
                .add(Attributes.MAX_HEALTH, MagmaCubeUnit.maxHealthCap)
                .add(Attributes.FOLLOW_RANGE, Unit.FOLLOW_RANGE_IMPROVED);
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);

        if (autocastingConsume() && getSize() < MAX_SIZE && getTargetGoal().getTarget() == null) {

            Vector3d unitPosition = new Vector3d(position().x, position().y, position().z);
            List<MagmaCubeUnit> nearbyEntities = MiscUtil.getEntitiesWithinRange(unitPosition, aggroRange, MagmaCubeUnit.class, level);

            double closestDist = aggroRange;
            MagmaCubeUnit closestTarget = null;

            for (MagmaCubeUnit cube : nearbyEntities) {
                if (cube.getOwnerName().equals(getOwnerName()) && cube != this) {
                    double dist = position().distanceTo(cube.position());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestTarget = cube;
                    }
                }
            }
            if (closestTarget != null) {
                consumeTarget = closestTarget;
                setUnitAttackTarget(closestTarget);
            }
        }
        if (!level.isClientSide()) {
            setFireTicks += 1;
            if (setFireTicks >= SET_FIRE_TICKS_MAX) {
                setFireTicks = 0;
                createMagma();
                //createFire();
            }
        }
    }

    // create magma on hitting the ground - for some reason only detected clientside
    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        if (!level.isClientSide() && pOnGround && !wasOnGround)
            createMagma();
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockSlimeGoal(this, false, 2);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.attackGoal = new MeleeAttackUnitGoal(this, false);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.targetSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setSize(STARTING_SIZE, true);
        return pSpawnData;
    }

    private static final int FIRE_DURATION_PER_SIZE = 40;

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        boolean result = super.doHurtTarget(pEntity);
        if (result &&
                pEntity == consumeTarget) {
            this.setSize(Math.min(MAX_SIZE, getSize() + consumeTarget.getSize() / 2), false);
            this.heal((consumeTarget.getHealth() / 2) + 15);
            pEntity.kill();
            consumeTarget = null;
            return true;
        } else if (getSize() >= 2) {
            pEntity.setRemainingFireTicks(FIRE_DURATION_PER_SIZE * getSize());
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (getSize() >= 2 && pSource.getEntity() instanceof AttackerUnit aUnit && aUnit.getAttackGoal() instanceof MeleeAttackUnitGoal)
            pSource.getEntity().setRemainingFireTicks((FIRE_DURATION_PER_SIZE * getSize()) / 2);

        if (pSource == DamageSource.ON_FIRE ||
            pSource == DamageSource.IN_FIRE ||
            pSource == DamageSource.LAVA)
            return false;

        return super.hurt(pSource, pAmount);
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
