package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ExperienceBottleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class FastXPMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void fastXP(CallbackInfo ci) {

        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;

        if (player == null) return;

        // FIX:
        // теперь работает только для XP
        if (player.getMainHandItem().getItem() instanceof ExperienceBottleItem) {
            mc.rightClickDelay = 0;
        }
    }
}
