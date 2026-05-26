package com.example.fastxp.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class FastXPMixin {

    // Срезаем задержку тиков использования на уровне игрока
    @Inject(method = "m_36135_", at = @At("HEAD"), remap = false)
    private void removeServerUseDelay(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Проверяем, зажаты ли PvP предметы в руках
        if (mainHand.getItem() instanceof ExperienceBottleItem || mainHand.getItem() instanceof ThrowablePotionItem || mainHand.getItem() instanceof EnderpearlItem || mainHand.getItem() instanceof BowItem ||
            offHand.getItem() instanceof ExperienceBottleItem || offHand.getItem() instanceof ThrowablePotionItem || offHand.getItem() instanceof EnderpearlItem || offHand.getItem() instanceof BowItem) {
            
            // Если игрок зажимает клик, принудительно обнуляем внутренний счетчик тиков снаряда
            player.m_21235_(); 
        }
    }
}
