package com.example.fastxp.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorAndHandsHud {
    public static void render(GuiGraphics guiGraphics, Minecraft mc, int width, int height) {
        int armorY = height - 55; 
        int armorX = width / 2 - 125; // Справа от хотбара

        // Список слотов: Броня + Главная рука + Левая рука
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, armorX, armorY);

                if (stack.isDamageableItem()) {
                    int maxDamage = stack.getMaxDamage();
                    int currentDamage = maxDamage - stack.getDamageValue();
                    float ratio = (float) currentDamage / maxDamage;

                    int color = 0xFF00FF00; 
                    if (ratio < 0.25f) color = 0xFFFF0000; 
                    else if (ratio < 0.5f) color = 0xFFFFAA00;

                    guiGraphics.fill(armorX + 18, armorY + 6, armorX + 58, armorY + 11, 0x80000000);
                    guiGraphics.fill(armorX + 19, armorY + 7, armorX + 19 + (int)(38 * ratio), armorY + 10, color);

                    String text = currentDamage + "/" + maxDamage;
                    guiGraphics.drawString(mc.font, text, armorX + 62, armorY + 4, 0xFFFFFFFF, true);
                }
                armorY -= 18; 
            }
        }
    }
}
