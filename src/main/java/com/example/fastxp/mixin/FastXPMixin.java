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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class FastXPMixin {
    
    @Inject(method = "use", at = @At("HEAD"))
    private void removeCooldownsAndBoostAll(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> info) {
        ItemStack stack = (ItemStack)(Object)this;
        
        // 1. ПУЛЕМЕТНЫЙ СПАМ ПРЕДМЕТОВ
        if (stack.getItem() instanceof ExperienceBottleItem || 
            stack.getItem() instanceof PotionItem || 
            stack.getItem() instanceof EnderpearlItem || 
            stack.getItem() instanceof BowItem) {
            
            // 2. УСКОРЕНИЕ БРОСКОВ НА SHIFT
            if (!world.isClientSide && user.isShiftKeyDown()) {
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

                world.getEntitiesOfClass(ThrownEnderpearl.class, user.getBoundingBox().inflate(2.0)).forEach(pearl -> {
                    if (pearl.getOwner() == user) {
                        pearl.setDeltaMovement(pearl.getDeltaMovement().scale(3.0));
                        pearl.hasImpulse = true;
                    }
                });
            }
        }
    }

    // НОВЫЙ БЛОК: Срабатывает при каждом использовании предметов на сервере
    @Inject(method = "use", at = @At("RETURN"))
    private void ultimatePvPHelper(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> info) {
        if (!world.isClientSide && user != null) {
            
            // 3. АНТИ-СЛЕПОТА И АНТИ-ТОШНОТА (Очищает экран от дебаффов)
            if (user.hasEffect(MobEffects.BLINDNESS)) user.removeEffect(MobEffects.BLINDNESS);
            if (user.hasEffect(MobEffects.DARKNESS)) user.removeEffect(MobEffects.DARKNESS);
            if (user.hasEffect(MobEffects.CONFUSION)) user.removeEffect(MobEffects.CONFUSION);

            // 4. ТРЕКЕР ВРАЖЕСКИХ ПЕРЛОВ
            world.getEntitiesOfClass(ThrownEnderpearl.class, user.getBoundingBox().inflate(30.0)).forEach(pearl -> {
                if (pearl.getOwner() != user && pearl.tickCount == 1) {
                    double distance = Math.sqrt(pearl.distanceToSqr(user));
                    user.sendSystemMessage(Component.literal(
                        "§8[§5!§8] §cВнимание! Рядом брошен Эндер-Жемчуг! Дистанция: §e" + String.format("%.1f", distance) + "м"
                    ));
                }
            });

            // 5. МОНИТОРИНГ КРИТИЧЕСКОЙ БРОНИ ВРАГОВ (Радиус 12 блоков от вас)
            world.getEntitiesOfClass(Player.class, user.getBoundingBox().inflate(12.0)).forEach(enemy -> {
                if (enemy != user) {
                    enemy.getArmorSlots().forEach(armor -> {
                        if (!armor.isEmpty() && armor.isDamageableItem()) {
                            int max = armor.getMaxDamage();
                            int current = max - armor.getDamageValue();
                            
                            // Если прочность конкретной шмотки врага упала ниже 10%
                            if (((float)current / max) <= 0.1f && enemy.tickCount % 100 == 0) {
                                user.sendSystemMessage(Component.literal(
                                    "§8[ §dMod §8] §fУ игрока §6" + enemy.getGameProfile().getName() + " §cпочти сломан предмет: " + armor.getHoverName().getString()
                                ));
                            }
                        }
                    });
                }
            });
        }
    }
}
