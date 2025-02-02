package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.villagers.OakStockpile;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingClientEvents.getBuildingsWithinRange;

// Move towards a building to build/repair it
// will continually try to move towards the building if too far away as long as this goal is being enacted

// if isBuilding(), stop the random lookaround goal (look at the next block to place instead)
// and run Player place block animations with arms shown

public class BuildRepairGoal extends MoveToTargetBlockGoal {

    public boolean ignoreNextCheckpoint = false;
    public final List<Building> queuedBuildings = new ArrayList<>();
    private Building buildingTarget;

    private Boolean isBuildingServerside = false;

    private final int AUTOCAST_RANGE = 20;
    public boolean autocastRepair = false;

    public BuildRepairGoal(Mob mob) {
        super(mob, true, 0);
    }

    public void setIsBuildingServerside(boolean isBuilding) {
        this.isBuildingServerside = isBuilding;
    }

    public boolean startNextQueuedBuilding() {
        queuedBuildings.removeIf(b -> !BuildingUtils.isBuildingBuildable(this.mob.level.isClientSide(), b));
        if (queuedBuildings.size() > 0) {
            setBuildingTarget(queuedBuildings.get(0));
            return true;
        }
        return false;
    }

    public void tick() {
        if (buildingTarget == null) {
            if (!this.mob.level.isClientSide() && WorkerUnit.isIdle((WorkerUnit) this.mob) && autocastRepair) {
                Building building = BuildingUtils.findClosestBuilding(
                        this.mob.level.isClientSide(),
                        this.mob.getEyePosition(),
                        b -> b.getBlocksPlaced() < b.getBlocksTotal() &&
                        BuildingServerEvents.getUnitToBuildingRelationship((Unit) this.mob, b) != Relationship.HOSTILE
                );
                if (building != null)
                    setBuildingTarget(building);
            }
            return;
        }
        if (!BuildingUtils.isBuildingBuildable(this.mob.level.isClientSide(), buildingTarget)) {
            if (!startNextQueuedBuilding()) {
                if (buildingTarget.name.contains(" Farm") && mob instanceof WorkerUnit workerUnit) {
                    ((WorkerUnit) mob).getGatherResourceGoal().setTargetResourceName(ResourceName.FOOD);
                    ((WorkerUnit) mob).getGatherResourceGoal().setTargetFarm(buildingTarget);
                }
                // look for the nearest resource to gather after completing a stockpile
                else if (buildingTarget instanceof OakStockpile stockpile && !buildingTarget.isBuilt && mob instanceof WorkerUnit workerUnit) {
                    ((Unit) mob).getReturnResourcesGoal().depositItems();
                    workerUnit.getGatherResourceGoal().setTargetResourceName(stockpile.mostAbundantNearbyResource);
                }
                stopBuilding();
            }
            return;
        }
        calcMoveTarget();
        if (isBuilding() && buildingTarget != null) {
            BlockPos bp = buildingTarget.centrePos;
            this.mob.getLookControl().setLookAt(bp.getX(), bp.getY(), bp.getZ());
            mob.getLookControl().lookAtCooldown = 20;
        }
    }

    private void calcMoveTarget() {
        if (this.buildingTarget != null)
            this.moveTarget = this.buildingTarget.getClosestGroundPos(mob.getOnPos(), 1);
    }

    // only count as building if in range of the target - building is actioned in Building.tick()
    public boolean isBuilding() {
        if (this.mob.level.isClientSide())
            return isBuildingServerside;

        if (buildingTarget != null && this.moveTarget != null)
            if (BuildingServerEvents.getUnitToBuildingRelationship((Unit) this.mob, buildingTarget) == Relationship.OWNED ||
                buildingTarget instanceof AbstractBridge)
                return MiscUtil.isMobInRangeOfPos(moveTarget, mob, 2); // buildingTarget.isPosInsideBuilding(mob.getOnPos())
        return false;
    }

    public void setBuildingTarget(@Nullable Building target) {
        if (target != null) {
            if (ignoreNextCheckpoint)
                ignoreNextCheckpoint = false;
            else {
                MiscUtil.addUnitCheckpoint((Unit) mob, new BlockPos(
                        target.centrePos.getX(),
                        target.originPos.getY() + 1,
                        target.centrePos.getZ()),
                        true
                );
            }
        }
        this.buildingTarget = target;
        calcMoveTarget();
        this.start();
    }

    public Building getBuildingTarget() { return buildingTarget; }

    // if we override stop() it for some reason is called after start() and we can never begin this goal...
    public void stopBuilding() {
        queuedBuildings.clear();
        buildingTarget = null;
        super.stopMoving();
    }
}
