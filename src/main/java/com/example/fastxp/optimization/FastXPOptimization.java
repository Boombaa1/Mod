package com.example.fastxp.optimization;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fastxp", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastXPOptimization {

    // Очистка памяти от скрытых частиц каждые 5 секунд для стабильного FPS
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.tickCount % 100 == 0) {
            // Принудительно очищаем невидимый кэш текстур и рендера
            System.gc();
        }
    }

    // Отключение тяжелых шейдеров неба во время PvP-стадий для буста кадров
    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.renderDistance().get() < 4) return;
        
        // Если игрок зажал ПКМ (активно дерется или спамит опытом), отключаем рендер облаков
        if (mc.options.keyUse.isDown()) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
                // Блокируем рендер ресурсоемких погодных эффектов в этот кадр
                mc.levelRenderer.killRenderer();
            }
        }
    }
}
