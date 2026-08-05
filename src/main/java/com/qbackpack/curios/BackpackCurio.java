package com.qbackpack.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class BackpackCurio implements ICurio {
    private final ItemStack stack;

    public BackpackCurio(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean canEquip(SlotContext slotContext) {
        return CurioBackpacks.findEquipped(slotContext.entity()).isEmpty();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext) {
        return stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .map(handler -> {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        if (!handler.getStackInSlot(slot).isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                })
                .orElse(true);
    }
}
