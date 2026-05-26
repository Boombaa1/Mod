package com.example.fastxp.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
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
    private void removePotionCooldownAndBoost(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> info) {
        ItemStack stack = (ItemStack)(Object)this;
        
        if (stack.getItem() instanceof ExperienceBottleItem || stack.getItem() instanceof PotionItem) {
            // 1. Убираем задержку использования (пулеметный спам)
            // Метод просто продолжается, игнорируя ванильный кулдаун.

            // 2. Если игрок зажал Shift (приседание), ускоряем летящие снаряды
            if (!world.isClientSide && user.isShiftKeyDown()) {
                // Ищем только что созданные сущности зелий/опыта рядом с игроком
                // и увеличиваем их вектор движения (Motion) в 3 раза.
                world.getEntitiesOfClass(ThrownExperienceBottle.class, user.getBoundingBox().inflate(2.0)).forEach(bottle -> {
                    if (bottle.getOwner() == user) {
                        bottle.setDeltaMovement(bottle.getDeltaMovement().scale(3.0));
                        bottle.hasImpulse = true;
                    }
                });

                world.getEntitiesOfClass(ThrownPotion.class, user.getBoundingBox().inflate(2.0)).forEach(potion -> {
                    if (potion.getOwner() == user) {
                        potion.setDeltaMovement(potion.getDeltaMovement().scale(3.0));
                        potion.hasImpulse = true;
                    }
                });
            }
        }
    }
}
