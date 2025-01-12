// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

package com.solegendary.reignofnether.unit.modelling.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.unit.modelling.animations.NecromancerAnimations;
import com.solegendary.reignofnether.unit.modelling.animations.PiglinMerchantAnimations;
import com.solegendary.reignofnether.unit.units.monsters.NecromancerUnit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class NecromancerModel<T extends Entity> extends KeyframeHierarchicalModel<T> {

	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ReignOfNether.MOD_ID, "necromancer_layer"), "main");

	private final ModelPart Main;
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart LegL;
	private final ModelPart LegR;
	private final ModelPart ArmR;
	private final ModelPart armL;
	private final ModelPart Pads;
	private final ModelPart Cape;
	private final ModelPart Staff;

	public NecromancerModel(ModelPart root) {
		this.Main = root.getChild("Main");
		this.Head = this.Main.getChild("Head");
		this.Body = this.Main.getChild("Body");
		this.LegL = this.Main.getChild("LegL");
		this.LegR = this.Main.getChild("LegR");
		this.ArmR = this.Main.getChild("ArmR");
		this.armL = this.Main.getChild("armL");
		this.Pads = this.Main.getChild("Pads");
		this.Cape = this.Main.getChild("Cape");
		this.Staff = this.Main.getChild("Staff");
	}

	@Override
	public @NotNull ModelPart root() {
		return this.Main;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Main = partdefinition.addOrReplaceChild("Main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Head = Main.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 47).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -24.0F, 1.0F));

		PartDefinition Body = Main.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -11.5F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(12, 32).addBox(-4.0F, 0.5F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.5F, 1.0F));

		PartDefinition LegL = Main.addOrReplaceChild("LegL", CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -12.0F, 1.0F));

		PartDefinition LegR = Main.addOrReplaceChild("LegR", CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -12.0F, 1.0F));

		PartDefinition ArmR = Main.addOrReplaceChild("ArmR", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -23.0F, 1.0F));

		PartDefinition armL = Main.addOrReplaceChild("armL", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -22.0F, 1.0F));

		PartDefinition Pads = Main.addOrReplaceChild("Pads", CubeListBuilder.create().texOffs(42, 32).addBox(-9.0F, -3.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(42, 44).addBox(4.0F, -3.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -22.0F, 1.0F));

		PartDefinition Cape = Main.addOrReplaceChild("Cape", CubeListBuilder.create().texOffs(40, 3).addBox(-8.0F, -1.0F, -1.0F, 16.0F, 23.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -23.0F, -0.4F));

		PartDefinition Staff = Main.addOrReplaceChild("Staff", CubeListBuilder.create().texOffs(66, 41).addBox(-0.5F, -1.875F, -0.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(65, 32).addBox(-1.5F, -3.875F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(73, 47).addBox(-2.5F, -2.875F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(73, 47).addBox(1.5F, -2.875F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(73, 47).addBox(-0.5F, -2.875F, -2.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(73, 47).addBox(-0.5F, -2.875F, 1.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.5F, -19.125F, -6.5F));

		PartDefinition Crep_r1 = Staff.addOrReplaceChild("Crep_r1", CubeListBuilder.create().texOffs(73, 47).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.875F, -1.5F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition Crep_r2 = Staff.addOrReplaceChild("Crep_r2", CubeListBuilder.create().texOffs(73, 47).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -0.875F, 0.5F, 0.0F, 0.0F, 1.5708F));

		return LayerDefinition.create(meshdefinition, 80, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		NecromancerUnit necromancer = ((NecromancerUnit) entity);

		AttributeInstance ms = necromancer.getAttribute(Attributes.MOVEMENT_SPEED);
		if (ms == null)
			return;
		float speed = (float) ms.getValue() * 10;

		if (necromancer.getMoveGoal().getMoveTarget() != null && !entity.isInWaterOrBubble() && limbSwingAmount > 0.01f) {
			animateWalk(NecromancerAnimations.WALK, limbSwing, limbSwingAmount, speed, speed);
			necromancer.walkAnimState.startIfStopped((int) (ageInTicks));
		} else {
			animate(necromancer.idleAnimState, NecromancerAnimations.IDLE, ageInTicks);
			necromancer.idleAnimState.startIfStopped((int) ageInTicks);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}