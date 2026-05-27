package com.example.fastxp.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import java.util.Collection;

public class PvPStatsAndEffectsHud {
    public static void render(GuiGraphics guiGraphics, Minecraft mc, int width, int height, int cachedTotems, int cachedGapples) {
        // 1. FPS и Пинг по центру
        int fps = mc.getFps();
        int ping = 0;
        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            PlayerInfo playerInfo = connection.getPlayerInfo(mc.player.getUUID());
            if (playerInfo != null) ping = playerInfo.getLatency();
        }

        String hudText = "§a" + fps + " §fFPS  §7|  §b" + ping + " §fPing";
        int hudX = (width - mc.font.width(hudText)) / 2;
        guiGraphics.fill(hudX - 6, 3, hudX + mc.font.width(hudText) + 6, 17, 0x99000000);
        guiGraphics.renderOutline(hudX - 6, 3, mc.font.width(hudText) + 12, 14, 0x55555555);
        guiGraphics.drawString(mc.font, hudText, hudX, 6, 0xFFFFFFFF, false);

        // 2. Pre-Warning Safe Totem
        if (mc.player.getHealth() <= 6.0f && !mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            String warningText = "[!] ВОЗЬМИ ТОТЕМ [!]";
            guiGraphics.drawString(mc.font, warningText, (width - mc.font.width(warningText)) / 2, height - 68, 0xFFFF2222, true);
        }

        // 3. Тотемы и яблоки из кэша
        int pvpItemsX = width / 2 + 235; 
        int pvpItemsY = height - 55;

        guiGraphics.fill(pvpItemsX - 2, pvpItemsY - 2, pvpItemsX + 45, pvpItemsY + 18, 0xAA000000);
        guiGraphics.renderOutline(pvpItemsX - 2, pvpItemsY - 2, 47, 20, 0xAA555555);
        guiGraphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), pvpItemsX, pvpItemsY);
        guiGraphics.drawString(mc.font, "x" + cachedTotems, pvpItemsX + 18, pvpItemsY + 4, cachedTotems > 0 ? 0xFFFFFF00 : 0x55FFFFFF, true);
        
        pvpItemsY -= 22;

        guiGraphics.fill(pvpItemsX - 2, pvpItemsY - 2, pvpItemsX + 45, pvpItemsY + 18, 0xAA000000);
        guiGraphics.renderOutline(pvpItemsX - 2, pvpItemsY - 2, 47, 20, 0xAA555555);
        guiGraphics.renderItem(new ItemStack(Items.GOLDEN_APPLE), pvpItemsX, pvpItemsY);
        guiGraphics.drawString(mc.font, "x" + cachedGapples, pvpItemsX + 18, pvpItemsY + 4, cachedGapples > 0 ? 0xFFFFAA00 : 0x55FFFFFF, true);

        // 4. Эффекты зелий с ИКОНКАМИ
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        int effectY = 10;
        int effectX = width - 125; 

        for (MobEffectInstance effect : effects) {
            guiGraphics.fill(effectX, effectY, width - 10, effectY + 22, 0xAA000000);
            guiGraphics.renderOutline(effectX, effectY, 115, 22, 0xAA555555);

            TextureAtlasSprite sprite = mc.getMobEffectTextures().get(effect.getEffect());
            if (sprite != null) {
                guiGraphics.blit(effectX + 4, effectY + 4, 0, 14, 14, sprite);
            }

            String name = effect.getEffect().getDisplayName().getString();
            if (name.length() > 11) name = name.substring(0, 9) + "..";
            int ticks = effect.getDuration();
            String time = ticks == -1 ? "**:**" : String.format("%d:%02d", (ticks / 20) / 60, (ticks / 20) % 60);

            guiGraphics.drawString(mc.font, name, effectX + 24, effectY + 2, 0xFFFFFFFF, true);
            guiGraphics.drawString(mc.font, time, effectX + 24, effectY + 11, 0xFFFFFF00, true);
            effectY += 26; 
        }

        // 5. Таймеры кулдаунов на иконках
        for (int slot = 0; slot < 9; slot++) {
            ItemStack s = mc.player.getInventory().getItem(slot);
            if (!s.isEmpty()) {
                Item item = s.getItem();
                float cd = mc.player.getCooldowns().getCooldownPercent(item, Minecraft.getInstance().getFrameTimeNS());
                if (cd > 0.0f) {
                    float sec = cd * 15;
                    if (item == Items.CHORUS_FRUIT) sec = cd * 1;
                    if (item == Items.SHIELD) sec = cd * 5;
                    if (sec > 0.1f) {
                        guiGraphics.drawString(mc.font, String.format("%.1f", sec), width / 2 - 88 + (slot * 20), height - 19, 0xFFFF5555, true);
                    }
                }
            }
        }
    }
}
