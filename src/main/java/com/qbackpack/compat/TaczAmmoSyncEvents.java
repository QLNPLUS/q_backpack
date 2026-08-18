package com.qbackpack.compat;

import com.qbackpack.QBackpack;
import com.qbackpack.curios.CurioBackpacks;
import com.qbackpack.network.ModNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = QBackpack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TaczAmmoSyncEvents {
    private static final Map<UUID, List<ItemStack>> LAST_SENT = new HashMap<>();

    private TaczAmmoSyncEvents() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || event.player.tickCount % 5 != 0 || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        List<ItemStack> summary = collect(player);
        List<ItemStack> previous = LAST_SENT.get(player.getUUID());
        if (sameStacks(previous, summary)) {
            return;
        }
        LAST_SENT.put(player.getUUID(), summary.stream().map(ItemStack::copy).toList());
        ModNetworking.syncTaczAmmo(player, summary);
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide) {
            LAST_SENT.remove(event.getEntity().getUUID());
        }
    }

    private static List<ItemStack> collect(ServerPlayer player) {
        List<ItemStack> result = new ArrayList<>();
        CurioBackpacks.findEquipped(player)
                .flatMap(stack -> stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve())
                .ifPresent(handler -> {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        ItemStack stack = handler.getStackInSlot(slot);
                        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                        if (!stack.isEmpty() && id != null && id.getNamespace().equals("tacz")
                                && (id.getPath().equals("ammo") || id.getPath().equals("ammo_box"))) {
                            result.add(stack.copy());
                        }
                    }
                });
        return result;
    }

    private static boolean sameStacks(List<ItemStack> first, List<ItemStack> second) {
        if (first == null || first.size() != second.size()) {
            return false;
        }
        for (int index = 0; index < first.size(); index++) {
            if (!ItemStack.matches(first.get(index), second.get(index))) {
                return false;
            }
        }
        return true;
    }
}
