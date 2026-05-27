package com.example.fastxp.optimization;

import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fastxp", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastXPOptimization {

    // Удалены:
    // - System.gc()
    // - allChanged()
    //
    // Потому что они вызывали:
    // - фризы
    // - статтеры
    // - пересборку чанков
    // - просадки FPS
}
