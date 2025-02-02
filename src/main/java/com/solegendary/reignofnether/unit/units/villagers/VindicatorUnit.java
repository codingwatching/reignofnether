package com.solegendary.reignofnether.unit.units.villagers;

import com.solegendary.reignofnether.ability.abilities.EnchantMaiming;
import com.solegendary.reignofnether.ability.abilities.EnchantSharpness;
import com.solegendary.reignofnether.ability.abilities.PromoteIllager;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.UnitAnimationAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.packets.UnitAnimationClientboundPacket;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VindicatorUnit extends Vindicator implements Unit, AttackerUnit {
    // region
    private final ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    public ArrayList<Checkpoint> getCheckpoints() { return checkpoints; };

    GarrisonGoal garrisonGoal;
    public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }
    public boolean canGarrison() { return getGarrisonGoal() != null; }

    UsePortalGoal usePortalGoal;
    public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }
    public boolean canUsePortal() { return getUsePortalGoal() != null; }

    public Faction getFaction() {return Faction.VILLAGERS;}
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
            SynchedEntityData.defineId(VindicatorUnit.class, EntityDataSerializers.STRING);

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
    public float getAttackRange() {return attackRange;}
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitAttackDamage() {return attackDamage + (hasSharpnessEnchant() ? 2 : 0);}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    @Nullable
    public int getPopCost() {return ResourceCosts.VINDICATOR.population;}
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // endregion

    final static public float attackDamage = 6.0f;
    final static public float attacksPerSecond = 0.5f;
    final static public float maxHealth = 65.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.28f;
    final static public float attackRange = 2; // only used by ranged units or melee building attackers
    final static public float aggroRange = 10;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;

    public int maxResources = 100;

    private MeleeAttackUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public VindicatorUnit(EntityType<? extends Vindicator> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    // all for animation syncing...
    @Override
    public void setUnitAttackTarget(@Nullable LivingEntity target) {
        AttackerUnit.super.setUnitAttackTarget(target);
        if (!this.level.isClientSide()) {
            if (target != null)
                UnitAnimationClientboundPacket.sendEntityPacket(UnitAnimationAction.NON_KEYFRAME_START, this, target);
            else
                UnitAnimationClientboundPacket.sendBasicPacket(UnitAnimationAction.NON_KEYFRAME_STOP, this);
        }
    }
    @Override
    public void setAttackBuildingTarget(BlockPos preselectedBlockPos) {
        AttackerUnit.super.setAttackBuildingTarget(preselectedBlockPos);
        if (!this.level.isClientSide())
             UnitAnimationClientboundPacket.sendBlockPosPacket(UnitAnimationAction.NON_KEYFRAME_START, this, preselectedBlockPos);
    }
    @Override
    public void resetBehaviours() {
        if (!this.level.isClientSide())
            UnitAnimationClientboundPacket.sendBasicPacket(UnitAnimationAction.NON_KEYFRAME_STOP, this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, VindicatorUnit.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, VindicatorUnit.attackDamage)
                .add(Attributes.ARMOR, VindicatorUnit.armorValue)
                .add(Attributes.MAX_HEALTH, VindicatorUnit.maxHealth)
                .add(Attributes.FOLLOW_RANGE, Unit.getFollowRange());
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);
        PromoteIllager.checkAndApplyBuff(this);
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new MeleeAttackUnitGoal(this, false);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.targetSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    /*
    @Override
    public void setupEquipmentAndUpgradesClient() {
        if (hasAnyEnchant())
            return;

        // weapon is purely visual, damage is based solely on entity attribute ATTACK_DAMAGE
        Item axe = Items.IRON_AXE;
        //if (ResearchClient.hasResearch(ResearchVindicatorAxes.itemName))
        //    axe = Items.DIAMOND_AXE;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(axe));
    }
     */

    @Override
    public void setupEquipmentAndUpgradesServer() {
        if (hasAnyEnchant())
            return;

        // weapon is purely visual, damage is based solely on entity attribute ATTACK_DAMAGE
        Item axe = Items.IRON_AXE;
        int damageMod = 0;
        //if (ResearchServerEvents.playerHasResearch(this.getOwnerName(), ResearchVindicatorAxes.itemName)) {
        //    axe = Items.DIAMOND_AXE;
        //    damageMod = 2;
        //}
        ItemStack axeStack = new ItemStack(axe);
        AttributeModifier mod = new AttributeModifier(UUID.randomUUID().toString(), damageMod, AttributeModifier.Operation.ADDITION);
        axeStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, mod, EquipmentSlot.MAINHAND);

        this.setItemSlot(EquipmentSlot.MAINHAND, axeStack);
    }

    public boolean hasAnyEnchant() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return !itemStack.getAllEnchantments().isEmpty();
    }

    public boolean hasMaimingEnchant() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemStack.getAllEnchantments().containsKey(EnchantMaiming.actualEnchantment);
    }

    public boolean hasSharpnessEnchant() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemStack.getAllEnchantments().containsKey(EnchantSharpness.actualEnchantment);
    }

    public Enchantment getEnchant() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        Optional<Enchantment> enchant = itemStack.getAllEnchantments().keySet().stream().findFirst();
        return enchant.orElse(null);
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        boolean hurt = super.doHurtTarget(pEntity);
        if (hurt && hasMaimingEnchant() && pEntity instanceof LivingEntity le)
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EnchantMaiming.SLOWNESS_DURATION, 1));
        return hurt;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        return pSpawnData;
    }
}
