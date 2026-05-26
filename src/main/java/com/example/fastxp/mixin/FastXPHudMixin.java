package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(net.minecraft.client.gui.Gui.class)
public class FastXPHudMixin {

    // ИСПРАВЛЕНО: для Forge 1.20.4 изменены аргументы метода 'render' (GuiGraphics и float)
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void renderUltimatePvPHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // =========================================================================
        // ОТРЕСОВКА ФПС И ПИНГА
        // =========================================================================
        int fps = mc.getFps();
        int ping = 0;
        
        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            PlayerInfo playerInfo = connection.getPlayerInfo(mc.player.getUUID());
            if (playerInfo != null) {
                ping = playerInfo.getLatency();
            }
        }

        String hudText = "§a" + fps + " §fFPS  §7|  §b" + ping + " §fPing";
        int hudX = (width - mc.font.width(hudText)) / 2;
        int hudY = 6;

        guiGraphics.fill(hudX - 6, hudY - 3, hudX + mc.font.width(hudText) + 6, hudY + 11, 0x99000000);
        guiGraphics.renderOutline(hudX - 6, hudY - 3, mc.font.width(hudText) + 12, 14, 0x55555555);
        guiGraphics.drawString(mc.font, hudText, hudX, hudY, 0xFFFFFFFF, false);

        // =========================================================================
        // ИНДИКАТОР SAFE TOTEM (Текст над инвентарём при критическом HP)
        // =========================================================================
        float health = mc.player.getHealth();
        if (health <= 6.0f && !mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            String warningText = "[!] ВОЗЬМИ ТОТЕМ [!]";
            int textX = (width - mc.font.width(warningText)) / 2;
            int textY = height - 68; 
            int alpha = (int) (Mth.sin((float)mc.player.tickCount * 0.4f) * 85 + 170);
            int blinkColor = (alpha << 24) | 0xFF2222;
            guiGraphics.drawString(mc.font, warningText, textX, textY, blinkColor, true);
        }

        // =========================================================================
        // ОТРЕСОВКА БРОНИ И ЕЕ ПРОЧНОСТИ (Слева снизу)
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

        // =========================================================================
        // СЧЕТЧИК ТОТЕМОВ И ЯБЛОК В ЗАКРУГЛЕННЫХ ПЛАШКАХ (Справа снизу)
        // =========================================================================
        int totemCount = 0;
        int gappleCount = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack item = mc.player.getInventory().getItem(i);
            if (item.is(Items.TOTEM_OF_UNDYING)) totemCount += item.getCount();
            if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) gappleCount += item.getCount();
        }

        int pvpItemsX = width / 2 + 105;
        int pvpItemsY = height - 55;

        if (totemCount > 0) {
            guiGraphics.fill(pvpItemsX - 2, pvpItemsY - 2, pvpItemsX + 45, pvpItemsY + 18, 0xAA000000);
            guiGraphics.renderOutline(pvpItemsX - 2, pvpItemsY - 2, 47, 20, 0xAA555555);
            guiGraphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), pvpItemsX, pvpItemsY);
            guiGraphics.drawString(mc.font, "x" + totemCount, pvpItemsX + 18, pvpItemsY + 4, 0xFFFFFF00, true);
            pvpItemsY -= 22;
        }
        if (gappleCount > 0) {
            guiGraphics.fill(pvpItemsX - 2, pvpItemsY - 2, pvpItemsX + 45, pvpItemsY + 18, 0xAA000000);
            guiGraphics.renderOutline(pvpItemsX - 2, pvpItemsY - 2, 47, 20, 0xAA555555);
            guiGraphics.renderItem(new ItemStack(Items.GOLDEN_APPLE), pvpItemsX, pvpItemsY);
            guiGraphics.drawString(mc.font, "x" + gappleCount, pvpItemsX + 18, pvpItemsY + 4, 0xFFFFAA00, true);
        }

        // =========================================================================
        // ЭФФЕКТЫ ЗЕЛИЙ (Справа сверху)
        // =========================================================================
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        int effectY = 10;
        int effectX = width - 115;

        for (MobEffectInstance effect : effects) {
            guiGraphics.fill(effectX, effectY, width - 10, effectY + 22, 0xAA000000);
            guiGraphics.renderOutline(effectX, effectY, 105, 22, 0xAA555555);

            String name = effect.getEffect().getDisplayName().getString();
            int durationTicks = effect.getDuration();
            String time = durationTicks == -1 ? "**:**" : String.format("%d:%02d", (durationTicks / 20) / 60, (durationTicks / 20) % 60);

            if (name.length() > 12) name = name.substring(0, 10) + "..";

            guiGraphics.drawString(mc.font, name, effectX + 6, effectY + 2, 0xFFFFFF, true);
            guiGraphics.drawString(mc.font, time, effectX + 6, effectY + 11, 0xAAAAAA, true);

            effectY += 26; 
        }

        // =========================================================================
        // ИСПРАВЛЕНО: ЗАКРЫТ БЛОК ТАЙМЕРОВ КУЛДАУНОВ
        // =========================================================================
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                float cooldownPercent = mc.player.getCooldowns().getCooldownPercent(item, partialTick);
                
                if (cooldownPercent > 0.0f) {
                    float secondsLeft = (cooldownPercent * 15); 
                    if (item == Items.CHORUS_FRUIT) secondsLeft = (cooldownPercent * 1);
                    if (item == Items.SHIELD) secondsLeft = (cooldownPercent * 5);
                }
            }
        }
    }
}
