package com.example.fastxp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ElytraRollMixin {

    private static float currentRoll = 0.0f;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void injectElytraRoll(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Проверяем, летит ли игрок на элитрах
        if (mc.player.isFallFlying()) {
            // Считываем скорость поворота головы игрока (разница между текущим и прошлым кадром)
            float turnSpeed = mc.player.getViewYRot() - mc.player.yRotO;

            // Сглаживаем наклон камеры (чтобы не было резких рывков)
            float targetRoll = turnSpeed * -2.0f; // Умножаем, чтобы наклон был заметнее
            currentRoll = currentRoll + (targetRoll - currentRoll) * 0.1f * partialTicks;

            // Ограничиваем максимальный наклон вбок (например, до 45 градусов), если не делаем сальто
            if (currentRoll > 45.0f) currentRoll = 45.0f;
            if (currentRoll < -45.0f) currentRoll = -45.0f;

            // Применяем наклон экрана по оси Z
            poseStack.mulPose(Axis.ZP.rotationDegrees(currentRoll));
        } else {
            // Если игрок на земле, плавно возвращаем камеру в ровное положение
            if (currentRoll != 0.0f) {
                currentRoll = currentRoll + (0.0f - currentRoll) * 0.2f * partialTicks;
                poseStack.mulPose(Axis.ZP.rotationDegrees(currentRoll));
            }
        }
    }
}
