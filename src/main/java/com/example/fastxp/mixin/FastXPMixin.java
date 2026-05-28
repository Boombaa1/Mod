package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "fastxp",
        value = Dist.CLIENT
)
public class FastXPMixin {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        // FastXP только для XP бутылок
        if (mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) {

            // Forge 1.20.4 safe
            mc.options.keyUse.setDown(true);
        }
    }
}
