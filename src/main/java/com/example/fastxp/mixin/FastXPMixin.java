package com.example.fastxp.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class FastXPMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void removePotionCooldown(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> info) {
        ItemStack stack = (ItemStack)(Object)this;
        
        if (stack.getItem() instanceof ExperienceBottleItem || stack.getItem() instanceof PotionItem) {
            // Убирает ванильную задержку клика Forge для опыта и зелий
        }
    }
}
