package com.solegendary.reignofnether.unit.units.villagers;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.CallToArmsUnit;
import com.solegendary.reignofnether.building.buildings.villagers.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchResourceCapacity;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.*;
import com.solegendary.reignofnether.unit.packets.UnitConvertClientboundPacket;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.unit.units.villagers.VillagerUnitProfession.*;

public class VillagerUnit extends Vindicator implements Unit, WorkerUnit, AttackerUnit, ArmSwingingUnit, VillagerDataHolder, ConvertableUnit {
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
    public BuildRepairGoal getBuildRepairGoal() {return buildRepairGoal;}
    public GatherResourcesGoal getGatherResourceGoal() {return gatherResourcesGoal;}
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    public BuildRepairGoal buildRepairGoal;
    public GatherResourcesGoal gatherResourcesGoal;
    private ReturnResourcesGoal returnResourcesGoal;
    private MeleeAttackUnitGoal attackGoal;
    public CallToArmsGoal callToArmsGoal;

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
            SynchedEntityData.defineId(VillagerUnit.class, EntityDataSerializers.STRING);

    // combat stats
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    @Nullable
    public int getPopCost() {return ResourceCosts.VILLAGER.population;}
    public boolean getWillRetaliate() {return willRetaliate;}
    public int getAttackCooldown() {return (int) (20 / attacksPerSecond);}
    public float getAttacksPerSecond() {return attacksPerSecond;}
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

    // ConvertableUnit
    public boolean converted = false;
    private boolean shouldDiscard = false;
    public boolean shouldDiscard() { return shouldDiscard; }
    public void setShouldDiscard(boolean discard) { this.shouldDiscard = discard; }

    // endregion

    public BlockState getReplantBlockState() {
        if (getUnitProfession() == FARMER && !isVeteran)
            return Blocks.CARROTS.defaultBlockState();
        else if (getUnitProfession() == FARMER && isVeteran)
            return Blocks.POTATOES.defaultBlockState();
        else
            return Blocks.WHEAT.defaultBlockState();
    }

    final static public float attackDamage = 1.0f;
    final static public float attacksPerSecond = 0.5f;
    final static public float attackRange = 2; // only used by ranged units or melee building attackers
    final static public float aggroRange = 0;
    final static public boolean willRetaliate = false; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = false;

    final static public float maxHealth = 25.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.25f;
    public int maxResources = 100;

    public VillagerUnitProfession getUnitProfession() {
        VillagerProfession profession = getProfession();
        if (VillagerProfession.FARMER.equals(profession)) {
            return FARMER;
        } else if (VillagerProfession.FLETCHER.equals(profession)) {
            return VillagerUnitProfession.LUMBERJACK;
        } else if (VillagerProfession.TOOLSMITH.equals(profession)) {
            return VillagerUnitProfession.MINER;
        } else if (VillagerProfession.MASON.equals(profession)) {
            return VillagerUnitProfession.MASON;
        } else if (VillagerProfession.WEAPONSMITH.equals(profession)) {
            return VillagerUnitProfession.HUNTER;
        }
        return VillagerUnitProfession.NONE;
    }

    public boolean isVeteran = false;
    public boolean isVeteran() { return isVeteran; }
    public void makeVeteran() {
        isVeteran = true;
        UnitSyncClientboundPacket.makeVillagerVeteran(this);
    }

    public boolean hasSpeedCheat() {
        return !this.level.isClientSide() && ResearchServerEvents.playerHasCheat(getOwnerName(), "operationcwal");
    }

    // equal to 4 full farm clears
    // bonus == plants carrots instead of wheat (+25% food), potatoes for veteran (+50% food)
    final static public int FARMER_EXP_REQ = 80;
    public int farmerExp = 0; // farm food blocks gathered
    public void incrementFarmerExp() {
        farmerExp += hasSpeedCheat() ? 10 : 1;
        if (farmerExp >= (FARMER_EXP_REQ / 2) && !hasUnitProfession()) {
            setProfession(VillagerProfession.FARMER);
        }
        else if (farmerExp >= FARMER_EXP_REQ && !isVeteran && getUnitProfession() == FARMER)
            makeVeteran();
    }

    // equal to ~4mins of log chopping, excludes leaves
    final static public float LUMBERJACK_SPEED_MULT = 1.25f;
    final static public float LUMBERJACK_SPEED_MULT_VETERAN = 1.5f;
    final static public int LUMBERJACK_EXP_REQ = 30;
    public int lumberjackExp = 0;
    public void incrementLumberjackExp() {
        lumberjackExp += hasSpeedCheat() ? 10 : 1;
        if (lumberjackExp >= (LUMBERJACK_EXP_REQ / 2) && !hasUnitProfession())
            setProfession(VillagerProfession.FLETCHER);
        else if (lumberjackExp >= LUMBERJACK_EXP_REQ && !isVeteran && getUnitProfession() == LUMBERJACK)
            makeVeteran();
    }

    // ~5mins of gathering
    final static public float MINER_SPEED_MULT = 1.25f;
    final static public float MINER_SPEED_MULT_VETERAN = 1.5f;
    final static public int MINER_EXP_REQ = 6;
    public int minerExp = 0; // ore blocks gathered
    public void incrementMinerExp() {
        minerExp += hasSpeedCheat() ? 10 : 1;
        if (minerExp >= (MINER_EXP_REQ / 2) && !hasUnitProfession())
            setProfession(VillagerProfession.TOOLSMITH);
        else if (minerExp >= MINER_EXP_REQ && !isVeteran && getUnitProfession() == MINER)
            makeVeteran();
    }

    // blocks built or repaired, excluding first capitol
    // ~5mins of building
    // counted as +1 worker when building/repairing (+2 for veteran)
    final static public int MASON_EXP_REQ = 600;
    public int masonExp = 0;
    public void incrementMasonExp() {
        masonExp += hasSpeedCheat() ? 10 : 1;
        if (masonExp >= (MASON_EXP_REQ / 2) && !hasUnitProfession())
            setProfession(VillagerProfession.MASON);
        else if (masonExp >= MASON_EXP_REQ && !isVeteran && getUnitProfession() == MASON)
            makeVeteran();
    }

    // chickens only worth 1, other animals worth 2
    // does 2 damage to huntable animals (3 for veteran)
    final static public int HUNTER_EXP_REQ = 8;
    public int hunterExp = 0;
    public void incrementHunterExp() {
        hunterExp += hasSpeedCheat() ? 10 : 1;
        if (hunterExp >= (HUNTER_EXP_REQ / 2) && !hasUnitProfession())
            setProfession(VillagerProfession.WEAPONSMITH);
        else if (hunterExp >= HUNTER_EXP_REQ && !isVeteran && getUnitProfession() == HUNTER)
            makeVeteran();
    }

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    private boolean isSwingingArmOnce = false;
    private int swingTime = 0;

    public int getSwingTime() {
        return swingTime;
    }

    public void setSwingTime(int time) {
        this.swingTime = time;
    }

    public boolean isSwingingArmOnce() { return isSwingingArmOnce; }

    public void setSwingingArmOnce(boolean swing) {
        isSwingingArmOnce = swing;
    }

    public boolean isSwingingArmRepeatedly() {
        return (this.getGatherResourceGoal().isGathering() || this.getBuildRepairGoal().isBuilding());
    }

    public static List<AbilityButton> getBuildingButtons() {
        return List.of(
            TownCentre.getBuildButton(Keybindings.keyQ),
            OakStockpile.getBuildButton(Keybindings.keyW),
            VillagerHouse.getBuildButton(Keybindings.keyE),
            WheatFarm.getBuildButton(Keybindings.keyR),
            Watchtower.getBuildButton(Keybindings.keyT),
            Barracks.getBuildButton(Keybindings.keyY),
            Blacksmith.getBuildButton(Keybindings.keyU),
            ArcaneTower.getBuildButton(Keybindings.keyI),
            Library.getBuildButton(Keybindings.keyO),
            Castle.getBuildButton(Keybindings.keyP),
            IronGolemBuilding.getBuildButton(Keybindings.keyL),
            OakBridge.getBuildButton(Keybindings.keyC)
        );
    }

    public VillagerUnit(EntityType<? extends Vindicator> entityType, Level level) {
        super(entityType, level);

        CallToArmsUnit callToArms = new CallToArmsUnit(level);
        this.abilities.add(callToArms);

        if (level.isClientSide()) {
            this.abilityButtons.addAll(getBuildingButtons());
            this.abilityButtons.add(callToArms.getButton(Keybindings.keyV));
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, VillagerUnit.attackDamage)
                .add(Attributes.MOVEMENT_SPEED, VillagerUnit.movementSpeed)
                .add(Attributes.MAX_HEALTH, VillagerUnit.maxHealth)
                .add(Attributes.FOLLOW_RANGE, Unit.getFollowRange())
                .add(Attributes.ARMOR, VillagerUnit.armorValue);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT;}
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }
    @Override
    protected SoundEvent getHurtSound(DamageSource p_34103_) { return SoundEvents.VILLAGER_HURT; }
    @Override
    public boolean isLeftHanded() { return false; }
    @Override // prevent vanilla logic for picking up items
    protected void pickUpItem(ItemEntity pItemEntity) { }

    public void tick() {
        if (shouldDiscard)
            this.discard();
        else {
            this.setCanPickUpLoot(true);
            super.tick();
            Unit.tick(this);
            AttackerUnit.tick(this);
            WorkerUnit.tick(this);
            this.callToArmsGoal.tick();
        }
    }

    public void convertToMilitia() {
        if (!converted) {
            LivingEntity newEntity = this.convertToUnit(EntityRegistrar.MILITIA_UNIT.get());
            if (newEntity instanceof MilitiaUnit mUnit) {
                mUnit.resourcesSaveData = this.gatherResourcesGoal.permSaveData;
                mUnit.profession = this.getProfession();
                mUnit.isVeteran = this.isVeteran;
                mUnit.farmerExp = this.farmerExp;
                mUnit.lumberjackExp = this.lumberjackExp;
                mUnit.minerExp = this.minerExp;
                mUnit.masonExp = this.masonExp;
                mUnit.hunterExp = this.hunterExp;

                UnitConvertClientboundPacket.syncConvertedUnits(getOwnerName(), List.of(getId()), List.of(newEntity.getId()));
                converted = true;
            }
        }
    }

    @Override
    public void resetBehaviours() {
        this.callToArmsGoal.stop();
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new MeleeAttackUnitGoal(this, true);
        this.buildRepairGoal = new BuildRepairGoal(this);
        this.gatherResourcesGoal = new GatherResourcesGoal(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
        this.callToArmsGoal = new CallToArmsGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, buildRepairGoal);
        this.goalSelector.addGoal(2, gatherResourcesGoal);
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    public void setupEquipmentAndUpgradesClient() {
        if (ResearchClient.hasResearch(ResearchResourceCapacity.itemName))
            this.maxResources = 200;
    }

    @Override
    public void setupEquipmentAndUpgradesServer() {
        if (ResearchServerEvents.playerHasResearch(this.getOwnerName(), ResearchResourceCapacity.itemName))
            this.maxResources = 200;

        if (this.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof BannerItem)
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.AIR));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        return pSpawnData;
    }

    private static final EntityDataAccessor<VillagerData> VILLAGER_DATA;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
        this.entityData.define(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public VillagerData getVillagerData() {
        return this.entityData.get(VILLAGER_DATA);
    }

    @Override
    public void setVillagerData(@NotNull VillagerData data) {
        VillagerData villagerdata = this.getVillagerData();
        this.entityData.set(VILLAGER_DATA, data);
    }

    public void setProfession(VillagerProfession profession) {
        this.setVillagerData(this.getVillagerData().setProfession(profession));
    }
    public VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }
    public boolean hasUnitProfession() {
        return this.getUnitProfession() != VillagerUnitProfession.NONE;
    }

    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        DataResult<Tag> var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData());
        var10000.resultOrPartial((err) -> ReignOfNether.LOGGER.error("Failed to save villager data"))
            .ifPresent((tag) -> pCompound.put("VillagerData", tag));
        pCompound.putInt("farmerExp", this.farmerExp);
        pCompound.putInt("lumberjackExp", this.lumberjackExp);
        pCompound.putInt("minerExp", this.minerExp);
        pCompound.putInt("masonExp", this.masonExp);
        pCompound.putInt("hunterExp", this.hunterExp);
        pCompound.putBoolean("isVeteran", this.isVeteran);
    }

    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("VillagerData", 10)) {
            DataResult<VillagerData> dataresult = VillagerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, pCompound.get("VillagerData")));
            dataresult.resultOrPartial((err) -> ReignOfNether.LOGGER.error("Failed to load villager data"))
                    .ifPresent(this::setVillagerData);
        }
        this.farmerExp = pCompound.getInt("farmerExp");
        this.lumberjackExp = pCompound.getInt("lumberjackExp");
        this.minerExp = pCompound.getInt("minerExp");
        this.masonExp = pCompound.getInt("masonExp");
        this.hunterExp = pCompound.getInt("hunterExp");
        if (!level.isClientSide() && pCompound.getBoolean("isVeteran"))
            makeVeteran();
    }

    static {
        VILLAGER_DATA = SynchedEntityData.defineId(VillagerUnit.class, EntityDataSerializers.VILLAGER_DATA);
    }
}
