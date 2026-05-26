package com.example.fastxp.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class FastXPMixin {
    // Этот миксин теперь чист, вся логика ушла в безопасный обработчик событий
}
