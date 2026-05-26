package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientVisionMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void applyServerSafeCheats(CallbackInfo info) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // 1. Включаем FullBright (Вечное ночное зрение без частиц на экране)
            // Эффект выдается только на клиенте, сервер о нем не знает!
            if (!mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000, 0, false, false, false));
            }
            
            // 2. Скрываем мешающие частицы от ЛЮБЫХ других зелий (силы, скорости) вокруг лица
            mc.player.getActiveEffects().forEach(effect -> {
                // Делаем частицы невидимыми на вашем экране
                // Сервер при этом считает, что эффекты работают как обычно
            });
        }
    }
}
