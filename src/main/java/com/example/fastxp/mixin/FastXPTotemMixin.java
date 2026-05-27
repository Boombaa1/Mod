package com.example.fastxp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public class FastXPTotemMixin {
    private static final Map<String, Integer> totemPopMap = new HashMap<>();

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void onEntityEvent(byte id, CallbackInfo ci) {
        if (id == 35) {
            Entity entity = (Entity) (Object) this;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.gameMode == null) return;

            if (entity instanceof Player player) {
                String targetName = player.getGameProfile().getName();
                int totalPops = totemPopMap.getOrDefault(targetName, 0) + 1;
                totemPopMap.put(targetName, totalPops);

                // Изменен префикс на: ⚔ [RagePvP] ⚔
                if (player == mc.player) {
                    String attackerName = "Враг";
                    double closestDist = 100.0;
                    for (Player p : mc.level.players()) {
                        if (p != mc.player && p.distanceToSqr(mc.player) < closestDist) {
                            closestDist = p.distanceToSqr(mc.player);
                            attackerName = p.getGameProfile().getName();
                        }
                    }
                    mc.player.sendSystemMessage(Component.literal(
                        "§8[ §c⚔ §4§lRagePvP §c⚔ §8] §fигрок §c" + attackerName + " §fснес вам тотем. Всего: §e" + totalPops
                    ));

                    for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = mc.player.getInventory().getItem(i);
                        if (!invStack.isEmpty() && invStack.is(Items.TOTEM_OF_UNDYING)) {
                            int slotId = i;
                            if (i < 9) slotId = i + 36;
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slotId, 0, ClickType.PICKUP, mc.player);
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 45, 0, ClickType.PICKUP, mc.player);
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slotId, 0, ClickType.PICKUP, mc.player);
                            break;
                        }
                    }

                } else {
                    mc.player.sendSystemMessage(Component.literal(
                        "§8[ §c⚔ §4§lRagePvP §c⚔ §8] §fвы снесли тотем игроку §6" + targetName + "§f. Всего: §e" + totalPops
                    ));
                }
            }
        }
    }
}
