package com.qbackpack.curios;

import com.qbackpack.QBackpack;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

@Mod.EventBusSubscriber(modid = QBackpack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BackpackPickupEvents {
    private BackpackPickupEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void insertInventoryOverflow(EntityItemPickupEvent event) {
        if (event.getResult() != Event.Result.DEFAULT) {
            return;
        }

        Player player = event.getEntity();
        ItemEntity itemEntity = event.getItem();
        if (itemEntity.target != null && !itemEntity.target.equals(player.getUUID())) {
            return;
        }

        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return;
        }

        ItemStack overflow = ItemHandlerHelper.insertItemStacked(
                new PlayerMainInvWrapper(player.getInventory()), groundStack.copy(), true);
        if (overflow.isEmpty()) {
            return;
        }

        int overflowCount = overflow.getCount();
        ItemStack backpackRemainder = CurioBackpacks.findEquipped(player)
                .flatMap(backpack -> backpack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve())
                .map(handler -> ItemHandlerHelper.insertItemStacked(handler, overflow.copy(), false))
                .orElse(overflow);
        int inserted = overflowCount - backpackRemainder.getCount();
        if (inserted <= 0) {
            return;
        }

        ItemStack pickedUp = overflow.copy();
        pickedUp.setCount(inserted);
        groundStack.shrink(inserted);

        ForgeEventFactory.firePlayerItemPickupEvent(player, itemEntity, pickedUp);
        player.take(itemEntity, inserted);
        player.awardStat(Stats.ITEM_PICKED_UP.get(pickedUp.getItem()), inserted);

        if (groundStack.isEmpty()) {
            itemEntity.discard();
            groundStack.setCount(inserted);
            event.setCanceled(true);
        }
        player.onItemPickup(itemEntity);
    }
}
