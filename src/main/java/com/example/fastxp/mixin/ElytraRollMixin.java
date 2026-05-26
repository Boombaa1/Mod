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
            // Исправленный расчет поворота в градусах для 1.20.4
            float currentYRot = mc.player.getYRot();
            float lastYRot = mc.player.yRotO;

            // Высчитываем чистую скорость поворота мыши
            float turnSpeed = currentYRot - lastYRot;

            // Обрабатываем переход угла через границу 180/-180 градусов, чтобы камеру не дергало при развороте на юг
            if (turnSpeed < -180.0f) turnSpeed += 360.0f;
            if (turnSpeed > 180.0f) turnSpeed -= 360.0f;

            // Сглаживаем наклон камеры (увеличиваем множитель для эффекта)
            float targetRoll = turnSpeed * -2.5f; 
            currentRoll = currentRoll + (targetRoll - currentRoll) * 0.15f * partialTicks;

            // Ограничиваем максимальный наклон вбок до 50 градусов
            if (currentRoll > 50.0f) currentRoll = 50.0f;
            if (currentRoll < -50.0f) currentRoll = -50.0f;

            // Применяем наклон экрана по оси Z
            poseStack.mulPose(Axis.ZP.rotationDegrees(currentRoll));
        } else {
            // Если игрок на земле, плавно возвращаем камеру в ровное положение
            if (Math.abs(currentRoll) > 0.01f) {
                currentRoll = currentRoll + (0.0f - currentRoll) * 0.2f * partialTicks;
                poseStack.mulPose(Axis.ZP.rotationDegrees(currentRoll));
            } else {
                currentRoll = 0.0f;
            }
        }
    }
}
