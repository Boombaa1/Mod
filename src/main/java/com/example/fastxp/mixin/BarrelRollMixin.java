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
public class BarrelRollMixin {

    // Переменные для отсчета кадров анимации
    private static int rollTicks = 0;
    private static final int TOTAL_ROLL_TICKS = 20; // Длительность переворота (20 тиков = 1 секунда)

    // Эту функцию (метод) вы можете вызывать из любой части вашего мода, чтобы запустить прокрутку
    public static void startBarrelRoll() {
        if (rollTicks <= 0) {
            rollTicks = TOTAL_ROLL_TICKS;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void injectBarrelRoll(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Логика уменьшения тиков
        if (mc.player.tickCount % 1 == 0 && rollTicks > 0 && !mc.isPaused()) {
            // Для плавности будем высчитывать прогресс
            float progress = (TOTAL_ROLL_TICKS - rollTicks + partialTicks) / (float) TOTAL_ROLL_TICKS;
            if (progress > 1.0f) progress = 1.0f;

            // Вычисляем угол поворота (360 градусов * прогресс)
            float angle = progress * 360.0f;

            // Поворачиваем камеру по оси Z (продольная ось игрока)
            poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            
            // Уменьшаем счетчик тиков в основном обновлении игрока
            if (partialTicks >= 1.0f || mc.player.tickCount % 2 == 0) {
                // Forge обновляет тики во внутреннем цикле, уменьшаем плавно
                rollTicks--;
            }
        }
    }
}
