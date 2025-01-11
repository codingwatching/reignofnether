// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

package com.solegendary.reignofnether.unit.modelling.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PiglinMerchantModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "ron__piglinschapionbbmodel"), "main");
	private final ModelPart Main;
	private final ModelPart Head;
	private final ModelPart EarLeft;
	private final ModelPart EarRight;
	private final ModelPart Pack;
	private final ModelPart Bone;
	private final ModelPart ChainRight;
	private final ModelPart ChainLeft;
	private final ModelPart Wart;
	private final ModelPart Wart2;
	private final ModelPart Wart3;
	private final ModelPart Body;
	private final ModelPart ArmRight;
	private final ModelPart ArmLeft;
	private final ModelPart LegLeft;
	private final ModelPart LegRight;

	public PiglinMerchantModel(ModelPart root) {
		this.Main = root.getChild("Main");
		this.Head = this.Main.getChild("Head");
		this.EarLeft = this.Head.getChild("EarLeft");
		this.EarRight = this.Head.getChild("EarRight");
		this.Pack = this.Main.getChild("Pack");
		this.Bone = this.Pack.getChild("Bone");
		this.ChainRight = this.Bone.getChild("ChainRight");
		this.ChainLeft = this.Bone.getChild("ChainLeft");
		this.Wart = this.Bone.getChild("Wart");
		this.Wart2 = this.Bone.getChild("Wart2");
		this.Wart3 = this.Bone.getChild("Wart3");
		this.Body = this.Main.getChild("Body");
		this.ArmRight = this.Main.getChild("ArmRight");
		this.ArmLeft = this.Main.getChild("ArmLeft");
		this.LegLeft = this.Main.getChild("LegLeft");
		this.LegRight = this.Main.getChild("LegRight");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Main = partdefinition.addOrReplaceChild("Main", CubeListBuilder.create(), PartPose.offset(0.0F, 5.0F, -2.0F));

		PartDefinition Head = Main.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(32, 0).addBox(-5.0F, -6.0F, -9.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(61, 2).addBox(-2.0F, -2.0F, -10.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(72, 0).addBox(-6.0F, -7.0F, -10.0F, 12.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(32, 5).addBox(-3.0F, 0.0F, -10.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 2).addBox(2.0F, 0.0F, -10.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition EarLeft = Head.addOrReplaceChild("EarLeft", CubeListBuilder.create().texOffs(72, 0).mirror().addBox(-1.9239F, 0.3827F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.2F, -4.0F, -6.0F, 0.0F, 0.0F, 0.3927F));

		PartDefinition EarRight = Head.addOrReplaceChild("EarRight", CubeListBuilder.create().texOffs(72, 0).addBox(0.2F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(60, 18).addBox(-0.8F, 3.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9F, -4.0F, -6.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition Pack = Main.addOrReplaceChild("Pack", CubeListBuilder.create().texOffs(76, 82).mirror().addBox(-10.3333F, -7.8333F, -4.8333F, 21.0F, 15.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(73, 110).addBox(-8.3333F, 7.1667F, -4.8333F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(81, 65).addBox(-11.8333F, -14.8333F, -4.8333F, 24.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3333F, -4.1667F, 10.8333F, 0.0873F, 0.0F, 0.0F));

		PartDefinition Bone = Pack.addOrReplaceChild("Bone", CubeListBuilder.create().texOffs(149, 47).mirror().addBox(14.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(82, 53).addBox(-16.0F, -1.0F, -1.0F, 30.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(149, 47).addBox(-18.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6667F, -10.8333F, -5.8333F, -0.0873F, 0.0F, 0.0F));

		PartDefinition ChainRight = Bone.addOrReplaceChild("ChainRight", CubeListBuilder.create(), PartPose.offset(5.5F, 1.0F, -1.0F));

		PartDefinition Chain_r1 = ChainRight.addOrReplaceChild("Chain_r1", CubeListBuilder.create().texOffs(136, 6).addBox(0.0F, -1.0F, -1.0F, 3.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 1.0F, -0.48F, 0.0F, 0.0F));

		PartDefinition ChainLeft = Bone.addOrReplaceChild("ChainLeft", CubeListBuilder.create(), PartPose.offset(-7.5F, 1.0F, -1.0F));

		PartDefinition Chain_r2 = ChainLeft.addOrReplaceChild("Chain_r2", CubeListBuilder.create().texOffs(136, 6).addBox(0.0F, -1.0F, -1.0F, 3.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 1.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition Wart = Bone.addOrReplaceChild("Wart", CubeListBuilder.create().texOffs(104, 0).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 1.0F, 0.0F));

		PartDefinition Wart_r1 = Wart.addOrReplaceChild("Wart_r1", CubeListBuilder.create().texOffs(104, 0).addBox(-5.0F, -6.0F, -1.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 6.0F, 1.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Wart2 = Bone.addOrReplaceChild("Wart2", CubeListBuilder.create().texOffs(122, 0).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, 1.0F, 0.0F));

		PartDefinition Wart_r2 = Wart2.addOrReplaceChild("Wart_r2", CubeListBuilder.create().texOffs(122, 0).addBox(-5.0F, -6.0F, -1.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 6.0F, 1.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Wart3 = Bone.addOrReplaceChild("Wart3", CubeListBuilder.create().texOffs(116, 0).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(11.0F, 1.0F, 0.0F));

		PartDefinition Wart_r3 = Wart3.addOrReplaceChild("Wart_r3", CubeListBuilder.create().texOffs(116, 0).addBox(-5.0F, -6.0F, -1.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 6.0F, 1.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Body = Main.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(32, 24).addBox(-8.0F, -16.0F, -4.0F, 16.0F, 19.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(12, 76).addBox(-8.0F, -3.0F, -4.0F, 16.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 3.0F));

		PartDefinition ArmRight = Main.addOrReplaceChild("ArmRight", CubeListBuilder.create().texOffs(89, 27).addBox(-1.0F, 0.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, -5.0F, 1.5F));

		PartDefinition ArmLeft = Main.addOrReplaceChild("ArmLeft", CubeListBuilder.create().texOffs(89, 27).mirror().addBox(-4.0F, -1.0F, -3.0F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-9.0F, -4.0F, 2.0F));

		PartDefinition LegLeft = Main.addOrReplaceChild("LegLeft", CubeListBuilder.create().texOffs(111, 30).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 7.0F, 3.0F));

		PartDefinition LegRight = Main.addOrReplaceChild("LegRight", CubeListBuilder.create().texOffs(111, 30).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 10.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 160, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}