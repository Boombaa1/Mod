package com.example.fastxp;

import com.example.fastxp.hud.ArmorAndHandsHud;
import com.example.fastxp.hud.InventorySlotsHud;
import com.example.fastxp.hud.PvPStatsAndEffectsHud;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fastxp", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastXPHudHandler {

    private static int cachedTotems = 0;
    private static int cachedGapples = 0;

    // ПОЛНОЕ ОТКЛЮЧЕНИЕ ТУМАНА (No Fog)
    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        event.setNearPlaneDistance(10000.0F);
        event.setFarPlaneDistance(20000.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFrustumCulling(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getEntity() == mc.player) return;
        Vec3 look = mc.player.getViewVector(1.0F);
        Vec3 target = event.getEntity().position().subtract(mc.player.position()).normalize();
        if (look.dot(target) < -0.1) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;

        if (mc.options.keyUse.isDown()) {
            ItemStack mainStack = mc.player.getMainHandItem();
            ItemStack offStack = mc.player.getOffhandItem();
            
            boolean hasXp = mainStack.getItem() instanceof ExperienceBottleItem || offStack.getItem() instanceof ExperienceBottleItem;
            boolean hasPotion = mainStack.getItem() instanceof ThrowablePotionItem || offStack.getItem() instanceof ThrowablePotionItem;
            
            if (hasXp || hasPotion) {
                mc.player.getCooldowns().removeCooldown(mainStack.getItem());
                mc.player.getCooldowns().removeCooldown(offStack.getItem());
                
                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack itemInHand = mc.player.getItemInHand(hand);
                    if (itemInHand.getItem() instanceof ExperienceBottleItem || itemInHand.getItem() instanceof ThrowablePotionItem) {
                        mc.gameMode.useItem(mc.player, hand);
                        mc.player.swing(hand);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player user = event.player;
        if (user == null || event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (user == mc.player) {
            if (user.hasEffect(MobEffects.BLINDNESS)) user.removeEffect(MobEffects.BLINDNESS);
            if (user.hasEffect(MobEffects.DARKNESS)) user.removeEffect(MobEffects.DARKNESS);
            if (user.hasEffect(MobEffects.CONFUSION)) user.removeEffect(MobEffects.CONFUSION);

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

            if (user.tickCount % 4 == 0) {
                int currentTotems = 0;
                int currentGapples = 0;
                for (int i = 0; i < user.getInventory().getContainerSize(); i++) {
                    ItemStack item = user.getInventory().getItem(i);
                    if (item.is(Items.TOTEM_OF_UNDYING)) currentTotems += item.getCount();
                    if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) currentGapples += item.getCount();
                }
                cachedTotems = currentTotems;
                cachedGapples = currentGapples;
            }
        }
    }

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

    @SubscribeEvent
    public static void onRenderHud(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        ArmorAndHandsHud.render(event.getGuiGraphics(), mc, width, height);
        InventorySlotsHud.render(event.getGuiGraphics(), mc, width, height);
        PvPStatsAndEffectsHud.render(event.getGuiGraphics(), mc, width, height, cachedTotems, cachedGapples);
    }

    @SubscribeEvent
    public static void onRenderEffectsPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.POTION_ICONS.id())) {
            event.setCanceled(true);
        }
    }
}
