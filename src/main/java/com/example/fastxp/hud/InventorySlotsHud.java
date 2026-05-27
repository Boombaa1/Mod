package com.example.fastxp.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventorySlotsHud {
    public static void render(GuiGraphics guiGraphics, Minecraft mc, int width, int height) {
        int freeSlots = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) freeSlots++;
        }

        int slotsX = width / 2 - 195; 
        int slotsY = height - 22; 

        guiGraphics.fill(slotsX - 4, slotsY - 3, slotsX + 56, slotsY + 13, 0xAA000000);
        guiGraphics.renderOutline(slotsX - 4, slotsY - 3, 60, 16, 0xAA555555);
        guiGraphics.renderItem(new ItemStack(Items.CHEST), slotsX, slotsY - 2);

        String slotsText = freeSlots + "/36";
        int textColor = freeSlots <= 5 ? 0xFFFF5555 : 0xFFFFAA00;
        guiGraphics.drawString(mc.font, slotsText, slotsX + 18, slotsY + 2, textColor, true);
    }
}
