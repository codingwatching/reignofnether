package com.solegendary.reignofnether.unit.modelling.renderers;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.unit.modelling.models.RoyalGuardModel;
import com.solegendary.reignofnether.unit.units.villagers.RoyalGuardUnit;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RoyalGuardRenderer extends MobRenderer<RoyalGuardUnit, RoyalGuardModel<RoyalGuardUnit>> {

    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("reignofnether", "textures/entities/royal_guard_unit.png");

    public RoyalGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalGuardModel<>(context.bakeLayer(RoyalGuardModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull RoyalGuardUnit royalGuardUnit) {
        return TEXTURE_LOCATION;
    }

    public RoyalGuardRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation mll) {
        super(pContext, new RoyalGuardModel<>(pContext.bakeLayer(mll)), 0.5F);
    }
}
