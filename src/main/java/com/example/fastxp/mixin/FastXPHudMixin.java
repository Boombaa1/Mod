package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(net.minecraft.client.gui.Gui.class)
public class FastXPHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderAdvancedPvPHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // =========================================================================
        // 1. ОТРЕСОВКА БРОНИ И ЕЕ ПРОЧНОСТИ (Слева снизу)
        // =========================================================================
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int armorY = height - 55; 
        int armorX = width / 2 - 125;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, armorX, armorY);

                if (stack.isDamageableItem()) {
                    int maxDamage = stack.getMaxDamage();
                    int currentDamage = maxDamage - stack.getDamageValue();
                    float ratio = (float) currentDamage / maxDamage;

                    int color = 0xFF00FF00; // Зеленый (Много прочности)
                    if (ratio < 0.25f) color = 0xFFFF0000; // Красный (Сломается!)
                    else if (ratio < 0.5f) color = 0xFFFFAA00; // Желтый

                    // Полоска прочности
                    guiGraphics.fill(armorX + 18, armorY + 6, armorX + 58, armorY + 11, 0x80000000);
                    guiGraphics.fill(armorX + 19, armorY + 7, armorX + 19 + (int)(38 * ratio), armorY + 10, color);

                    // Числовое значение (Например: 120/400)
                    String text = currentDamage + "/" + maxDamage;
                    guiGraphics.drawString(mc.font, text, armorX + 62, armorY + 4, 0xFFFFFFFF, true);
                }
                armorY -= 18; 
            }
        }

        // =========================================================================
        // 2. СЧЕТЧИК ТОТЕМОВ И ЗОЛОТЫХ ЯБЛОК ДЛЯ PvP (Справа снизу)
        // =========================================================================
        int totemCount = 0;
        int gappleCount = 0;

        // Сканируем весь инвентарь игрока на наличие предметов PvP
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack item = mc.player.getInventory().getItem(i);
            if (item.is(Items.TOTEM_OF_UNDYING)) totemCount += item.getCount();
            if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) gappleCount += item.getCount();
        }

        int pvpItemsX = width / 2 + 105;
        int pvpItemsY = height - 55;

        // Рисуем Тотемы
        if (totemCount > 0) {
            guiGraphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), pvpItemsX, pvpItemsY);
            guiGraphics.drawString(mc.font, "x" + totemCount, pvpItemsX + 18, pvpItemsY + 4, 0xFFFFFF00, true);
            pvpItemsY -= 18;
        }
        // Рисуем Яблоки
        if (gappleCount > 0) {
            guiGraphics.renderItem(new ItemStack(Items.GOLDEN_APPLE), pvpItemsX, pvpItemsY);
            guiGraphics.drawString(mc.font, "x" + gappleCount, pvpItemsX + 18, pvpItemsY + 4, 0xFFFFAA00, true);
        }

        // =========================================================================
        // 3. ЭФФЕКТЫ ЗЕЛИЙ (Справа сверху с закругленными рамками)
        // =========================================================================
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        int effectY = 10;
        int effectX = width - 115;

        for (MobEffectInstance effect : effects) {
            // Рисуем рамку эффекта
            guiGraphics.fill(effectX, effectY, width - 10, effectY + 22, 0xAA000000);
            guiGraphics.renderOutline(effectX, effectY, 105, 22, 0xAA555555);

            String name = effect.getEffect().getDisplayName().getString();
            int durationTicks = effect.getDuration();
            String time = durationTicks == -1 ? "**:**" : String.format("%d:%02d", (durationTicks / 20) / 60, (durationTicks / 20) % 60);

            // Ограничиваем длину имени, чтобы текст не вылезал за плашку
            if (name.length() > 12) name = name.substring(0, 10) + "..";

            guiGraphics.drawString(mc.font, name, effectX + 6, effectY + 2, 0xFFFFFF, true);
            guiGraphics.drawString(mc.font, time, effectX + 6, effectY + 11, 0xAAAAAA, true);

            effectY += 26; 
        }
    }
}
