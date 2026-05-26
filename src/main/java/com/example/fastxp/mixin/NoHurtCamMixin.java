package com.example.fastxp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NoHurtCamMixin {
    
    // Исправлено для 1.20.4: Внедряемся в метод bobHurt по его прямому SRG-имени с remap=false
    @Inject(method = "m_109117_", at = @At("HEAD"), cancellable = true, remap = false)
    private void onBobHurt(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }
}
