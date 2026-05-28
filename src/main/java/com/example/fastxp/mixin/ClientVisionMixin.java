package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "fastxp",
        value = Dist.CLIENT
)
public class ClientVisionMixin {

    private static int timer = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        timer++;

        if (timer >= 100) {

            timer = 0;

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
