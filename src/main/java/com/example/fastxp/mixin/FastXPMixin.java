package com.example.fastxp.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class FastXPMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void removeCooldownsAndBoostAll(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> info) {
        ItemStack stack = (ItemStack)(Object)this;
        
        // Проверяем все предметы: Опыт, Зелья, Жемчуг Края или Лук
        if (stack.getItem() instanceof ExperienceBottleItem || 
            stack.getItem() instanceof PotionItem || 
            stack.getItem() instanceof EnderpearlItem || 
            stack.getItem() instanceof BowItem) {
            
            // 1. Убираем задержку использования клика (пулеметный спам для всего)

            // 2. Если зажат Shift, то ускоряем полет снарядов в 3 раза
            if (!world.isClientSide && user.isShiftKeyDown()) {
                
                // Ускорение опыта
                world.getEntitiesOfClass(ThrownExperienceBottle.class, user.getBoundingBox().inflate(2.0)).forEach(bottle -> {
                    if (bottle.getOwner() == user) {
                        bottle.setDeltaMovement(bottle.getDeltaMovement().scale(3.0));
                        bottle.hasImpulse = true;
                    }
                });

                // Ускорение взрывных зелий
                world.getEntitiesOfClass(ThrownPotion.class, user.getBoundingBox().inflate(2.0)).forEach(potion -> {
                    if (potion.getOwner() == user) {
                        potion.setDeltaMovement(potion.getDeltaMovement().scale(3.0));
                        potion.hasImpulse = true;
                    }
                });

                // Ускорение эндер-жемчуга
                world.getEntitiesOfClass(ThrownEnderpearl.class, user.getBoundingBox().inflate(2.0)).forEach(pearl -> {
                    if (pearl.getOwner() == user) {
                        pearl.setDeltaMovement(pearl.getDeltaMovement().scale(3.0));
                        pearl.hasImpulse = true;
                    }
                });
            }
        }
    }
}
