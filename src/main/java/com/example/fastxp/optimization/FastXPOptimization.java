package com.example.fastxp.optimization;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fastxp", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastXPOptimization {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.tickCount % 100 == 0) {
            System.gc();
        }
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.renderDistance().get() < 4) return;
        
        if (mc.options.keyUse.isDown()) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
                // ИСПРАВЛЕНО: Используем правильный метод перезагрузки рендера для Forge 1.20.4
                mc.levelRenderer.allChanged();
            }
        }
    }
}
