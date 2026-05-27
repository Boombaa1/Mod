package com.example.fastxp.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorAndHandsHud {
    public static void render(GuiGraphics guiGraphics, Minecraft mc, int width, int height) {
        // Отресовка брони
        EquipmentSlot[] slots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
        int armorY = height - 55; 
        int armorX = width / 2 + 105; 

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, armorX, armorY);
                if (stack.isDamageableItem()) {
                    int maxDamage = stack.getMaxDamage();
                    int currentDamage = maxDamage - stack.getDamageValue();
                    float ratio = (float) currentDamage / maxDamage;
                    int color = ratio < 0.25f ? 0xFFFF0000 : (ratio < 0.5f ? 0xFFFFAA00 : 0xFF00FF00);

                    guiGraphics.fill(armorX + 18, armorY + 6, armorX + 58, armorY + 11, 0x80000000);
                    guiGraphics.fill(armorX + 19, armorY + 7, armorX + 19 + (int)(38 * ratio), armorY + 10, color);
                    guiGraphics.drawString(mc.font, currentDamage + "/" + maxDamage, armorX + 62, armorY + 4, 0xFFFFFFFF, true);
                }
            } else {
                guiGraphics.drawString(mc.font, "-", armorX + 5, armorY + 4, 0x55FFFFFF, true);
            }
            armorY -= 18; 
        }

        // Отресовка прочности правой и левой рук ПОД броней
        int handY = height - 55;
        int handX = width / 2 + 190;
        EquipmentSlot[] hands = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
        for (EquipmentSlot hand : hands) {
            ItemStack stack = mc.player.getItemBySlot(hand);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                guiGraphics.renderItem(stack, handX, handY);
                int max = stack.getMaxDamage();
                int current = max - stack.getDamageValue();
                float ratio = (float) current / max;
                int color = ratio < 0.25f ? 0xFFFF0000 : (ratio < 0.5f ? 0xFFFFAA00 : 0xFF00FF00);

                guiGraphics.fill(handX + 18, handY + 6, handX + 58, handY + 11, 0x80000000);
                guiGraphics.fill(handX + 19, handY + 7, handX + 19 + (int)(38 * ratio), handY + 10, color);
                guiGraphics.drawString(mc.font, current + "/" + max, handX + 62, handY + 4, 0xFFFFFFFF, true);
                handY -= 18;
            }
        }
    }
}
