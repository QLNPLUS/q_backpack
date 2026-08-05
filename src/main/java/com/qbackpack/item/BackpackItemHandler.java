package com.qbackpack.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class BackpackItemHandler extends ItemStackHandler {
    public BackpackItemHandler(int size) {
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof BackpackItem);
    }
}
