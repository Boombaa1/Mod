package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientVisionMixin {

    @Unique
    private int fastxp$timer = 0;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void applyServerSafeCheats(boolean pause, CallbackInfo ci) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        fastxp$timer++;

        // обновляем эффект раз в 5 секунд
        if (fastxp$timer >= 100) {

            fastxp$timer = 0;

            mc.player.addEffect(
                    new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            220,
                            0,
                            false,
                            false,
                            false
                    )
            );
        }
    }
}
