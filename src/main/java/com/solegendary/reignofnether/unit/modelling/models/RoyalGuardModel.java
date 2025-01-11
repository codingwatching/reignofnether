// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

package com.solegendary.reignofnether.unit.modelling.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.solegendary.reignofnether.ReignOfNether;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RoyalGuardModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ReignOfNether.MOD_ID, "royal_guard_layer"), "main");

	private final ModelPart Head;
	private final ModelPart Helm;
	private final ModelPart Body;
	private final ModelPart ArmR;
	private final ModelPart Sheld;
	private final ModelPart ArmL;
	private final ModelPart Mace;
	private final ModelPart Head2;
	private final ModelPart LegR;
	private final ModelPart LegR2;

	public RoyalGuardModel(ModelPart root) {
		this.Head = root.getChild("Head");
		this.Helm = this.Head.getChild("Helm");
		this.Body = root.getChild("Body");
		this.ArmR = root.getChild("ArmR");
		this.Sheld = this.ArmR.getChild("Sheld");
		this.ArmL = root.getChild("ArmL");
		this.Mace = this.ArmL.getChild("Mace");
		this.Head2 = this.Mace.getChild("Head2");
		this.LegR = root.getChild("LegR");
		this.LegR2 = root.getChild("LegR2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.5F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(-1.0F, -3.0F, -6.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -0.6F, -0.5F));

		PartDefinition Helm = Head.addOrReplaceChild("Helm", CubeListBuilder.create().texOffs(34, 0).addBox(-8.0F, -11.0F, -1.5F, 10.0F, 10.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(74, 9).addBox(-5.0F, -14.0F, -1.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 0).addBox(2.0F, -9.0F, 1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 3).addBox(3.0F, -11.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 3).mirror().addBox(-10.9F, -11.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(33, 0).mirror().addBox(-9.0F, -9.0F, 1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 0.0F, -3.5F));

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 20).addBox(-3.96F, -12.9152F, -3.8888F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(33, 39).addBox(-3.96F, -12.9152F, -3.8888F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.5F))
		.texOffs(66, 29).addBox(-3.96F, -12.9152F, -3.8888F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(-1.04F, 12.9152F, -0.1112F));

		PartDefinition cube_r1 = Body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(67, 51).mirror().addBox(-0.5F, -4.0F, -1.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(67, 51).addBox(-5.7F, -4.0F, -1.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.54F, -7.4152F, -4.5888F, 0.6109F, 0.0F, 0.0F));

		PartDefinition ArmR = partdefinition.addOrReplaceChild("ArmR", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(99, 37).addBox(3.5F, -0.5F, 0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(100, 28).addBox(-0.5F, -1.5F, -1.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(91, 5).addBox(2.0F, 8.0F, -2.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(91, 14).addBox(1.0F, 11.0F, -2.0F, 3.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 1.0F, -2.0F));

		PartDefinition Sheld = ArmR.addOrReplaceChild("Sheld", CubeListBuilder.create().texOffs(3, 64).addBox(-7.0F, -11.0F, -0.5F, 14.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 13.0F, 0.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition ArmL = partdefinition.addOrReplaceChild("ArmL", CubeListBuilder.create().texOffs(16, 40).mirror().addBox(-3.5F, -0.7F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(99, 37).mirror().addBox(-6.0F, -0.2F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(100, 28).mirror().addBox(-4.0F, -1.2F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(110, 5).mirror().addBox(-4.5F, 8.3F, -3.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(110, 14).mirror().addBox(-4.5F, 11.3F, -3.0F, 3.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.5F, 0.7F, -1.0F));

		PartDefinition Mace = ArmL.addOrReplaceChild("Mace", CubeListBuilder.create().texOffs(135, 36).addBox(0.0F, -6.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 10.3F, -1.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition Head2 = Mace.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(124, 24).addBox(-4.0F, -5.0F, -1.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(124, 35).addBox(-2.0F, -7.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -6.0F, 0.7F, 0.6981F, 0.0F, 0.0F));

		PartDefinition Spike_r1 = Head2.addOrReplaceChild("Spike_r1", CubeListBuilder.create().texOffs(124, 35).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -3.0F, 1.0F, 0.0F, -1.5708F, 1.5708F));

		PartDefinition Spike_r2 = Head2.addOrReplaceChild("Spike_r2", CubeListBuilder.create().texOffs(124, 35).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -3.0F, 2.0F, 0.0F, 1.5708F, -1.5708F));

		PartDefinition Spike_r3 = Head2.addOrReplaceChild("Spike_r3", CubeListBuilder.create().texOffs(124, 35).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -3.0F, 4.0F, -1.5708F, 0.0F, -3.1416F));

		PartDefinition Spike_r4 = Head2.addOrReplaceChild("Spike_r4", CubeListBuilder.create().texOffs(124, 35).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.0F, -1.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition Spike_r5 = Head2.addOrReplaceChild("Spike_r5", CubeListBuilder.create().texOffs(124, 35).addBox(0.0F, 0.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.0F, 2.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition LegR = partdefinition.addOrReplaceChild("LegR", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(93, 48).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
		.texOffs(97, 42).addBox(-1.0F, 5.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -1.0F));

		PartDefinition LegR2 = partdefinition.addOrReplaceChild("LegR2", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(-2.0F, -5.0F, -3.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(110, 48).mirror().addBox(-2.0F, -5.0F, -3.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
		.texOffs(113, 42).mirror().addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 17.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 144, 96);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		ArmR.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		ArmL.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LegR.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LegR2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}