package com.example.fastxp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = "fastxp", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastXPHudHandler {

    // 1. МГНОВЕННЫЙ СБРОС КУЛДАУНА И БУСТ СНАРЯДОВ ПРИ КЛИКЕ
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player user = event.getEntity();
        if (user == null || !user.level().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ExperienceBottleItem || 
            stack.getItem() instanceof ThrowablePotionItem || 
            stack.getItem() instanceof EnderpearlItem || 
            stack.getItem() instanceof BowItem) {
            
            // Стираем задержку использования
            user.getCooldowns().removeCooldown(stack.getItem());
            
            // Буст скорости в 3 раза на Shift
            if (user.isShiftKeyDown()) {
                user.level().getEntitiesOfClass(ThrownExperienceBottle.class, user.getBoundingBox().inflate(2.0)).forEach(bottle -> {
                    if (bottle.getOwner() == user) bottle.setDeltaMovement(bottle.getDeltaMovement().scale(3.0));
                });
                user.level().getEntitiesOfClass(ThrownPotion.class, user.getBoundingBox().inflate(2.0)).forEach(potion -> {
                    if (potion.getOwner() == user) potion.setDeltaMovement(potion.getDeltaMovement().scale(3.0));
                });
                user.level().getEntitiesOfClass(ThrownEnderpearl.class, user.getBoundingBox().inflate(2.0)).forEach(pearl -> {
                    if (pearl.getOwner() == user) pearl.setDeltaMovement(pearl.getDeltaMovement().scale(3.0));
                });
            }
        }
    }

    // 2. ЕЖЕТИКОВАЯ ПРОВЕРКА ДЕБАФФОВ, ЧУЖИХ ПЕРЛОВ И БРОНИ ВРАГОВ
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player user = event.player;
        if (user == null || event.phase != TickEvent.Phase.START || !user.level().isClientSide()) return;

        // Автоочистка вредных эффектов
        if (user.hasEffect(MobEffects.BLINDNESS)) user.removeEffect(MobEffects.BLINDNESS);
        if (user.hasEffect(MobEffects.DARKNESS)) user.removeEffect(MobEffects.DARKNESS);
        if (user.hasEffect(MobEffects.CONFUSION)) user.removeEffect(MobEffects.CONFUSION);

        // Алерт на летящий рядом чужой жемчуг энда
        user.level().getEntitiesOfClass(ThrownEnderpearl.class, user.getBoundingBox().inflate(30.0)).forEach(pearl -> {
            if (pearl.getOwner() != user && pearl.tickCount == 1) {
                double distance = Math.sqrt(pearl.distanceToSqr(user));
                user.sendSystemMessage(Component.literal(
                    "§8[§5!§8] §cВнимание! Рядом брошен Эндер-Жемчуг! Дистанция: §e" + String.format("%.1f", distance) + "м"
                ));
            }
        });

        // Сканирование прочности брони врагов в радиусе 12 блоков
        user.level().getEntitiesOfClass(Player.class, user.getBoundingBox().inflate(12.0)).forEach(enemy -> {
            if (enemy != user && enemy.tickCount % 100 == 0) {
                enemy.getArmorSlots().forEach(armor -> {
                    if (!armor.isEmpty() && armor.isDamageableItem()) {
                        int max = armor.getMaxDamage();
                        int current = max - armor.getDamageValue();
                        if (((float)current / max) <= 0.1f) {
                            user.sendSystemMessage(Component.literal(
                                "§8[ §dMod §8] §fУ игрока §6" + enemy.getGameProfile().getName() + " §cпочти сломан предмет: " + armor.getHoverName().getString()
                            ));
                        }
                    }
                });
            }
        });
    }
    // 3. ОТРЕСОВКА ИНТЕРФЕЙСА (HUD) СПРАВА ОТ ИНВЕНТАРЯ
    @SubscribeEvent
    public static void onRenderHud(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // FPS и Пинг
        int fps = mc.getFps();
        int ping = 0;
        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            PlayerInfo playerInfo = connection.getPlayerInfo(mc.player.getUUID());
            if (playerInfo != null) ping = playerInfo.getLatency();
        }

        String hudText = "§a" + fps + " §fFPS  §7|  §b" + ping + " §fPing";
        int hudX = (width - mc.font.width(hudText)) / 2;
        int hudY = 6;

        guiGraphics.fill(hudX - 6, hudY - 3, hudX + mc.font.width(hudText) + 6, hudY + 11, 0x99000000);
        guiGraphics.renderOutline(hudX - 6, hudY - 3, mc.font.width(hudText) + 12, 14, 0x55555555);
        guiGraphics.drawString(mc.font, hudText, hudX, hudY, 0xFFFFFFFF, false);

        // Safe Totem предупреждение
        if (mc.player.getHealth() <= 6.0f && !mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            String warningText = "[!] ВОЗЬМИ ТОТЕМ [!]";
            guiGraphics.drawString(mc.font, warningText, (width - mc.font.width(warningText)) / 2, height - 68, 0xFFFF2222, true);
        }

        // Броня справа от инвентаря
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int armorY = height - 55; 
        int armorX = width / 2 + 95; 

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
                armorY -= 18; 
            }
        }

        // Тотемы и яблоки в плашках
        int totemCount = 0;
        int gappleCount = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack item = mc.player.getInventory().getItem(i);
            if (item.is(Items.TOTEM_OF_UNDYING)) totemCount += item.getCount();
            if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) gappleCount += item.getCount();
        }

        int pvpItemsX = width / 2 + 215; 
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

        // Эффекты зелий
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        int effectY = 10;
        int effectX = width - 115;

        for (MobEffectInstance effect : effects) {
            guiGraphics.fill(effectX, effectY, width - 10, effectY + 22, 0xAA000000);
            guiGraphics.renderOutline(effectX, effectY, 105, 22, 0xAA555555);
            String name = effect.getEffect().getDisplayName().getString();
            if (name.length() > 12) name = name.substring(0, 10) + "..";
            int ticks = effect.getDuration();
            String time = ticks == -1 ? "**:**" : String.format("%d:%02d", (ticks / 20) / 60, (ticks / 20) % 60);

            guiGraphics.drawString(mc.font, name, effectX + 6, effectY + 2, 0xFFFFFFFF, true);
            guiGraphics.drawString(mc.font, time, effectX + 6, effectY + 11, 0xAAAAAA00, true);
            effectY += 26; 
        }
    }
}
