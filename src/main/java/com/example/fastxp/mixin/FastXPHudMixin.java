package com.example.fastxp.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(net.minecraft.client.gui.Gui.class)
public class FastXPHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCustomHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // 1. ОТРЕСОВКА БРОНИ СНИЗУ (Над панелью быстрого доступа)
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int armorY = height - 55; // Позиция чуть выше хотбара
        int armorX = width / 2 - 110;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                // Рисуем саму иконку брони
                guiGraphics.renderItem(stack, armorX, armorY);

                if (stack.isDamageableItem()) {
                    int maxDamage = stack.getMaxDamage();
                    int currentDamage = maxDamage - stack.getDamageValue();
                    float ratio = (float) currentDamage / maxDamage;

                    // Выбираем цвет полоски (Зеленый -> Желтый -> Красный)
                    int color = 0xFF00FF00; // Зеленый
                    if (ratio < 0.25f) color = 0xFFFF0000; // Красный
                    else if (ratio < 0.5f) color = 0xFFFFAA00; // Желтый

                    // Рисуем задний фон полоски прочности
                    guiGraphics.fill(armorX + 18, armorY + 6, armorX + 58, armorY + 11, 0x80000000);
                    // Рисуем заполненную закругленную полоску
                    guiGraphics.fill(armorX + 19, armorY + 7, armorX + 19 + (int)(38 * ratio), armorY + 10, color);

                    // Текст прочности справа формата "100/500"
                    String text = currentDamage + "/" + maxDamage;
                    guiGraphics.drawString(mc.font, text, armorX + 62, armorY + 4, 0xFFFFFFFF, true);
                }
                armorY -= 18; // Сдвигаем следующий элемент выше
            }
        }

        // 2. ОТРЕСОВКА ЭФФЕКТОВ ЗЕЛИЙ (Справа сверху)
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        int effectY = 10;
        int effectX = width - 130;

        for (MobEffectInstance effect : effects) {
            // Рисуем красивую плашку с закругленными углами в стиле Майнкрафт (темный полупрозрачный фон)
            guiGraphics.fill(effectX, effectY, width - 10, effectY + 24, 0xAA101010);
            guiGraphics.renderOutline(effectX, effectY, 120, 24, 0xAA404040); // Серая рамка

            // Название эффекта и время
            String name = effect.getEffect().getDisplayName().getString();
            int durationTicks = effect.getDuration();
            String time = durationTicks == -1 ? "**:**" : String.format("%d:%02d", (durationTicks / 20) / 60, (durationTicks / 20) % 60);

            // Отресовка текста на плашке
            guiGraphics.drawString(mc.font, name, effectX + 8, effectY + 3, 0xFFFFFF, false);
            guiGraphics.drawString(mc.font, time, effectX + 8, effectY + 13, 0xAAAAAA, false);

            effectY += 28; // Сдвигаем следующий эффект ниже
        }
    }
}
