// СТРОКА 1: НАЧАЛО ФАЙЛА (Импорты и пакет)
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

    // СТРОКА 18: СЕРЕДИНА ФАЙЛА (Логика пулемёта опыта)
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void removeServerUseDelay(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (mainHand.getItem() instanceof ExperienceBottleItem || mainHand.getItem() instanceof ThrowablePotionItem || mainHand.getItem() instanceof EnderpearlItem || mainHand.getItem() instanceof BowItem ||
            offHand.getItem() instanceof ExperienceBottleItem || offHand.getItem() instanceof ThrowablePotionItem || offHand.getItem() instanceof EnderpearlItem || offHand.getItem() instanceof BowItem) {
            
            cir.setReturnValue(false);
        }
    }
}
// СТРОКА 33: КОНЕЦ ФАЙЛА
