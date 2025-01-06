package com.solegendary.reignofnether.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

    @Shadow private int nextSongDelay = 100;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void getProjectionMatrix(CallbackInfo ci) {
        Minecraft MC = Minecraft.getInstance();
        if (MC.screen instanceof TitleScreen)
            nextSongDelay = 0;
    }
}