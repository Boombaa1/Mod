package com.example.fastxp.optimization;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fastxp", value = Dist.CLIENT)
public class FastXPOptimization {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (mc.player == null || mc.level == null) return;

        // FIX:
        // gc теперь редко вызывается
        if (mc.player.tickCount % 1200 == 0) {
            System.gc();
        }

        // FIX:
        // allChanged вызывается реже
        if (mc.options.keyUse.isDown()
                && mc.player.tickCount % 20 == 0) {

            mc.levelRenderer.allChanged();
        }
    }
}
