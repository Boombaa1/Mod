package com.example.fastxp.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventorySlotsHud {
    public static void render(GuiGraphics guiGraphics, Minecraft mc, int width, int height) {
        int freeSlots = 0;
        
        // Считаем только основные 36 слотов инвентаря (без брони и левой руки)
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
                freeSlots++;
            }
        }

        // Позиционируем слева от хотбара, симметрично броне
        int slotsX = width / 2 - 195; 
        int slotsY = height - 22; 

        // Рисуем маленькую закругленную темную плашку под сундук и текст
        guiGraphics.fill(slotsX - 4, slotsY - 3, slotsX + 56, slotsY + 13, 0xAA000000);
        guiGraphics.renderOutline(slotsX - 4, slotsY - 3, 60, 16, 0xAA555555);

        // Рендерим иконку обычного ванильного сундука (уменьшенную и аккуратную)
        guiGraphics.renderItem(new ItemStack(Items.CHEST), slotsX, slotsY - 2);

        // Выводим красивый счетчик свободных слотов, например "24/36"
        String slotsText = freeSlots + "/36";
        int textColor = freeSlots <= 5 ? 0xFFFF5555 : 0xFFFFAA00; // Красный, если инвентарь забит мусором
        
        guiGraphics.drawString(mc.font, slotsText, slotsX + 18, slotsY + 2, textColor, true);
    }
}
