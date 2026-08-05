package com.qbackpack.menu;

import com.qbackpack.item.BackpackItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

final class BackpackSlot extends SlotItemHandler {
    BackpackSlot(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !(stack.getItem() instanceof BackpackItem) && super.mayPlace(stack);
    }
}
