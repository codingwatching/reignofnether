
package com.solegendary.reignofnether.unit.modelling.models;

import com.mojang.math.Vector3f;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

// HierarchicalModel but adds some keyframe animation methods from 1.20
@OnlyIn(Dist.CLIENT)
public abstract class KeyframeHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public KeyframeHierarchicalModel() { this(RenderType::entityCutoutNoCull); }

    public KeyframeHierarchicalModel(Function<ResourceLocation, RenderType> p_170623_) {
        super(p_170623_);
    }

    protected void animateWalk(AnimationDefinition animDef, float limbSwing, float limbSwingAmount, float limbSwingSpeed, float limbSwingAmountSpeed) {
        long i = (long)(limbSwing * 50.0F * limbSwingSpeed);
        float f = Math.min(limbSwingAmount * limbSwingAmountSpeed, 1.0f);
        KeyframeAnimations.animate(this, animDef, i, f, ANIMATION_VECTOR_CACHE);
    }

    protected void applyStatic(AnimationDefinition animDef) {
        KeyframeAnimations.animate(this, animDef, 0L, 1.0F, ANIMATION_VECTOR_CACHE);
    }
}
