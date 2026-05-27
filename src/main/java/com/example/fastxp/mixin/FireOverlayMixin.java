package com.example.fastxp.mixin;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ScreenEffectRenderer.class)
public class FireOverlayMixin {

    @ModifyArg(
            method = "renderFire",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"
            ),
            index = 1
    )
    private static float lowerFire(float y) {

        // FIX:
        // уменьшение огня
        return -0.35F;
    }
}
