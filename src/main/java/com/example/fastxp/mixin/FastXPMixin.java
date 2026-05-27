package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ExperienceBottleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class FastXPMixin {

    @Shadow
    private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void fastXP(CallbackInfo ci) {

        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;

        if (player == null) return;

        // FastXP только для XP бутылок
        if (player.getMainHandItem().getItem() instanceof ExperienceBottleItem) {

            this.rightClickDelay = 0;
        }
    }
}
