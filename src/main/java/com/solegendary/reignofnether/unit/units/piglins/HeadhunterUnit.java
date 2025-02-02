package com.solegendary.reignofnether.unit.units.piglins;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.Bloodlust;
import com.solegendary.reignofnether.ability.abilities.MountHoglin;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchHeavyTridents;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadhunterUnit extends PiglinBrute implements Unit, AttackerUnit, RangedAttackerUnit {
    // region
    private final ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    public ArrayList<Checkpoint> getCheckpoints() { return checkpoints; };

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
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}
    public MountGoal getMountGoal() {return mountGoal;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    private ReturnResourcesGoal returnResourcesGoal;
    public MountGoal mountGoal;

    public LivingEntity getFollowTarget() { return followTarget; }
    public boolean getHoldPosition() { return holdPosition; }
    public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

    // if true causes moveGoal and attackGoal to work together to allow attack moving
    // moves to a block but will chase/attack nearby monsters in range up to a certain distance away
    private LivingEntity followTarget = null; // if nonnull, continuously moves to the target
    private boolean holdPosition = false;
    private BlockPos attackMoveTarget = null;

    // which player owns this unit? this format ensures its synched to client without having to use packets
    public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
    public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
    public static final EntityDataAccessor<String> ownerDataAccessor =
            SynchedEntityData.defineId(HeadhunterUnit.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
    }

    // combat stats
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    @Nullable
    public int getPopCost() {return ResourceCosts.HEADHUNTER.population;}
    public boolean getWillRetaliate() {return willRetaliate;}
    public float getAttacksPerSecond() {
        if (bloodlustTicks > 0)
            return attacksPerSecond * BLOODLUST_MULTIPLIER;
        return attacksPerSecond;
    }
    public float getAggroRange() {return aggroRange;}
    public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
    public float getAttackRange() {return attackRange;}
    public float getUnitAttackDamage() {return attackDamage;}
    public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}
    public Goal getAttackGoal() { return attackGoal; }
    public Goal getAttackBuildingGoal() { return null; }
    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    private UnitBowAttackGoal<? extends LivingEntity> attackGoal;

    // endregion

    public int getAttackCooldown() {
        if (bloodlustTicks > 0)
            return (int) (20 / (attacksPerSecond * BLOODLUST_MULTIPLIER));
        return (int) (20 / attacksPerSecond);
    }

    final static public float BLOODLUST_MULTIPLIER = 1.5f;

    final static public float attackDamage = 6.0f;
    final static public float attacksPerSecond = 0.3f;
    final static public float attackRange = 12; // only used by ranged units or melee building attackers
    final static public float aggroRange = 12;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;

    final static public float maxHealth = 40.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.25f;
    public int maxResources = 100;

    public int bloodlustTicks = 0;

    public int fogRevealDuration = 0; // set > 0 for the client who is attacked by this unit
    public int getFogRevealDuration() { return fogRevealDuration; }
    public void setFogRevealDuration(int duration) { fogRevealDuration = duration; }

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public HeadhunterUnit(EntityType<? extends PiglinBrute> entityType, Level level) {
        super(entityType, level);

        MountHoglin mountHoglinAbility = new MountHoglin(this);
        this.abilities.add(mountHoglinAbility);
        Bloodlust bloodlust = new Bloodlust(this);
        this.abilities.add(bloodlust);
        if (level.isClientSide()) {
            this.abilityButtons.add(mountHoglinAbility.getButton(Keybindings.keyQ));
            this.abilityButtons.add(bloodlust.getButton(Keybindings.keyW));
        }
    }

    @Override
    public void resetBehaviours() {
        this.mountGoal.stop();
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, HeadhunterUnit.attackDamage)
                .add(Attributes.MOVEMENT_SPEED, HeadhunterUnit.movementSpeed)
                .add(Attributes.MAX_HEALTH, HeadhunterUnit.maxHealth)
                .add(Attributes.FOLLOW_RANGE, Unit.getFollowRange())
                .add(Attributes.ARMOR, HeadhunterUnit.armorValue);
    }

    @Override
    public boolean isLeftHanded() { return false; }
    @Override // prevent vanilla logic for picking up items
    protected void pickUpItem(ItemEntity pItemEntity) { }
    @Override
    public boolean isConverting() { return false; }
    @Override
    protected void customServerAiStep() { }
    @Override
    public LivingEntity getTarget() {
        return this.targetGoal.getTarget();
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);
        this.mountGoal.tick();

        // need to do this outside the goal so it ticks down while not attacking
        // only needed for attack goals created by reignofnether like RangedBowAttackUnitGoal
        if (attackGoal != null)
            attackGoal.tickCooldown();

        if (bloodlustTicks > 0)
            bloodlustTicks -= 1;
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new UnitBowAttackGoal<>(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
        this.mountGoal = new MountGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.goalSelector.addGoal(2, mountGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    public void performUnitRangedAttack(LivingEntity pTarget, float velocity) {
        ThrownTrident $$2 = new ThrownTrident(this.level, this, new ItemStack(Items.TRIDENT));
        double $$3 = pTarget.getX() - this.getX();
        double $$4 = pTarget.getY(0.3333333333333333) - $$2.getY();
        double $$5 = pTarget.getZ() - this.getZ();
        double $$6 = Math.sqrt($$3 * $$3 + $$5 * $$5);

        if (pTarget.getEyeHeight() <= 1.0f)
            $$4 -= (1.0f - pTarget.getEyeHeight());

        $$2.shoot($$3, $$4 + $$6 * 0.20000000298023224, $$5, 1.6F, 0);
        this.playSound(SoundEvents.DROWNED_SHOOT, 3.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity($$2);

        if (!level.isClientSide() && pTarget instanceof Unit unit)
            FogOfWarClientboundPacket.revealRangedUnit(unit.getOwnerName(), this.getId());
    }

    @Override
    public void setupEquipmentAndUpgradesClient() {

    }

    @Override
    public void setupEquipmentAndUpgradesServer() {
        ItemStack tridentStack = new ItemStack(Items.TRIDENT);
        AttributeModifier mod = new AttributeModifier(UUID.randomUUID().toString(), 0, AttributeModifier.Operation.ADDITION);
        tridentStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, mod, EquipmentSlot.MAINHAND);

        if (ResearchServerEvents.playerHasResearch(getOwnerName(), ResearchHeavyTridents.itemName))
            tridentStack.enchant(Enchantments.UNBREAKING, 1);

        this.setItemSlot(EquipmentSlot.MAINHAND, tridentStack);
    }
}
