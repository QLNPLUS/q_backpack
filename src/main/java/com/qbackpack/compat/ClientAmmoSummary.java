package com.qbackpack.compat;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public final class ClientAmmoSummary {
    private static volatile List<ItemStack> stacks = List.of();
    private static volatile IItemHandler handler = new ItemStackHandler(0);

    private ClientAmmoSummary() {}

    public static void update(List<ItemStack> newStacks) {
        List<ItemStack> copies = newStacks.stream().map(ItemStack::copy).toList();
        ItemStackHandler newHandler = new ItemStackHandler(copies.size());
        for (int slot = 0; slot < copies.size(); slot++) {
            newHandler.setStackInSlot(slot, copies.get(slot));
        }
        stacks = copies;
        handler = newHandler;
    }

    public static IItemHandler appendTo(IItemHandler original) {
        IItemHandler summary = handler;
        return summary.getSlots() == 0 ? original : TaczAmmoCompat.combine(original, summary);
    }

    public static Inventory appendTo(Inventory original) {
        return stacks.isEmpty() ? original : new SummaryInventory(original, stacks);
    }

    private static final class SummaryInventory extends Inventory {
        private final Inventory original;
        private final List<ItemStack> summary;

        private SummaryInventory(Inventory original, List<ItemStack> summary) {
            super(original.player);
            this.original = original;
            this.summary = summary;
        }

        @Override
        public int getContainerSize() {
            return original.getContainerSize() + summary.size();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot < original.getContainerSize()
                    ? original.getItem(slot)
                    : summary.get(slot - original.getContainerSize());
        }
    }
}
