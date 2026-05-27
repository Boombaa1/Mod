package com.example.fastxp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ShieldTransformMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD")
    )
    private void transformShield(
            LocalPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            CallbackInfo ci
    ) {

        if (stack.getItem() == Items.SHIELD) {

            poseStack.scale(0.8f, 0.8f, 0.8f);

            poseStack.translate(0.1f, -0.15f, 0.0f);
        }
    }
}
