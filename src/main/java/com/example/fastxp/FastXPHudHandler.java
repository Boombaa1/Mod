package com.example.fastxp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = "fastxp", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastXPHudHandler {

    private static int cachedTotems = 0;
    private static int cachedGapples = 0;

    // ОПТИМИЗАЦИЯ: ОТКЛЮЧАЕМ РЕНДЕР ИГРОКОВ И МОБОВ ЗА СПИНОЙ (ФПС БУСТ)
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getEntity() == mc.player) return;

        LivingEntity entity = event.getEntity();
        
        // Получаем вектор взгляда вашей камеры
        Vec3 lookVec = mc.player.getViewVector(1.0F);
        
        // Получаем направление от вас до энтити
        Vec3 targetVec = entity.position().subtract(mc.player.position()).normalize();
        
        // Считаем скалярное произведение векторов
        double dotProduct = lookVec.dot(targetVec);
        
        // Если значение меньше 0 — энтити гарантированно находится сзади вас
        if (dotProduct < -0.1) {
            event.setCanceled(true); // Отменяем рендеринг этой модели
        }
    }

    // Бесконечный сброс кулдауна и автоочистка эффектов
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player user = event.player;
        if (user == null || event.phase != TickEvent.Phase.START) return;

        for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack stack = user.getItemInHand(hand);
            if (!stack.isEmpty() && (stack.getItem() instanceof ExperienceBottleItem || 
                stack.getItem() instanceof ThrowablePotionItem || 
                stack.getItem() instanceof EnderpearlItem || 
                stack.getItem() instanceof BowItem)) {
                user.getCooldowns().removeCooldown(stack.getItem());
            }
        }

        if (user.hasEffect(MobEffects.BLINDNESS)) user.removeEffect(MobEffects.BLINDNESS);
        if (user.hasEffect(MobEffects.DARKNESS)) user.removeEffect(MobEffects.DARKNESS);
        if (user.hasEffect(MobEffects.CONFUSION)) user.removeEffect(MobEffects.CONFUSION);

        if (!user.level().isClientSide()) {
            user.level().getEntitiesOfClass(ThrownEnderpearl.class, user.getBoundingBox().inflate(30.0)).forEach(pearl -> {
                if (pearl.getOwner() != user && pearl.tickCount == 1) {
                    double distance = Math.sqrt(pearl.distanceToSqr(user));
                    user.sendSystemMessage(Component.literal(
                        "§8[§5!§8] §cВнимание! Рядом брошен Эндер-Жемчуг! Дистанция: §e" + String.format("%.1f", distance) + "м"
                    ));
                }
            });

            if (user.tickCount % 100 == 0) {
                user.level().getEntitiesOfClass(Player.class, user.getBoundingBox().inflate(12.0)).forEach(enemy -> {
                    if (enemy != user) {
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
        }
    }
    // Буст скорости полёта предметов на Shift
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player user = event.getEntity();
        if (user == null || user.level().isClientSide() || !user.isShiftKeyDown()) return;

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ExperienceBottleItem || 
            stack.getItem() instanceof ThrowablePotionItem || 
            stack.getItem() instanceof EnderpearlItem) {
            
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

    // 3. ОТРИСОВКА ВСЕГО КАСТОМНОГО PvP-ИНТЕРФЕЙСА (HUD)
    @SubscribeEvent
    public static void onRenderHud(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // FPS и Пинг по центру экрана
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

        // Pre-Warning Safe Totem
        if (mc.player.getHealth() <= 6.0f && !mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            String warningText = "[!] ВОЗЬМИ ТОТЕМ [!]";
            guiGraphics.drawString(mc.font, warningText, (width - mc.font.width(warningText)) / 2, height - 68, 0xFFFF2222, true);
        }

        // Индикаторы брони справа от инвентаря
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

        // Считаем тотемы и яблоки 5 раз в секунду
        if (mc.player.tickCount % 4 == 0) {
            int currentTotems = 0;
            int currentGapples = 0;
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                ItemStack item = mc.player.getInventory().getItem(i);
                if (item.is(Items.TOTEM_OF_UNDYING)) currentTotems += item.getCount();
                if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) currentGapples += item.getCount();
            }
            cachedTotems = currentTotems;
            cachedGapples = currentGapples;
        }

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

        // Кастомное меню эффектов зелий в правой части экрана (С иконками!)
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
    }

    // ПОЛНОСТЬЮ ОТКЛЮЧАЕМ ДУБЛИРУЮЩИЙСЯ СТАНДАРТНЫЙ ИНТЕРФЕЙС ЭФФЕКТОВ
   @SubscribeEvent
public static void onRenderEffectsPre(RenderGuiOverlayEvent.Pre event) {
    if (event.getOverlay().id().equals(VanillaGuiOverlay.POTION_ICONS.id())) {
        event.setCanceled(true);
    }
 }
}
