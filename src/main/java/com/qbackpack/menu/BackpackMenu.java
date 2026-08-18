package com.qbackpack.menu;

import com.qbackpack.curios.CurioBackpacks;
import com.qbackpack.init.ModMenus;
import com.qbackpack.item.BackpackItem;
import com.qbackpack.sort.BackpackSorter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public final class BackpackMenu extends InventoryMenu {
    private static final int CRAFTING_AND_ARMOR_END = 9;
    private static final int PLAYER_INVENTORY_START = 9;
    private static final int PLAYER_INVENTORY_END = 36;
    private static final int HOTBAR_START = 36;
    private static final int HOTBAR_END = 45;
    private static final int OFFHAND_SLOT = 45;
    private static final int BACKPACK_START = 46;
    private static final int STANDARD_COLUMNS = 9;
    private static final int NETHERITE_COLUMNS = 13;
    private static final int NETHERITE_CAPACITY = 52;

    private final Player player;
    private final ItemStack openStack;
    private final IItemHandler backpackInventory;
    private final int capacity;

    public BackpackMenu(int containerId, Inventory playerInventory, ItemStack openStack, int capacity) {
        super(playerInventory, !playerInventory.player.level().isClientSide, playerInventory.player);
        this.containerId = containerId;
        this.player = playerInventory.player;
        this.openStack = openStack;
        this.capacity = checkedCapacity(capacity);
        this.backpackInventory = openStack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseGet(() -> new ItemStackHandler(this.capacity));

        int extraHeight = extraHeight();
        for (Slot slot : slots) {
            if (slot.container == playerInventory
                    && slot.getSlotIndex() < playerInventory.getContainerSize() - 5) {
                slot.y += extraHeight;
            }
        }

        Slot anchor = slots.get(PLAYER_INVENTORY_START);
        int backpackTop = anchor.y - extraHeight;
        int backpackLeft = anchor.x - (columns() - STANDARD_COLUMNS) / 2 * 18;
        for (int slot = 0; slot < capacity; slot++) {
            addSlot(new BackpackSlot(backpackInventory, slot,
                    backpackLeft + slot % columns() * 18, backpackTop + slot / columns() * 18));
        }
    }

    public static BackpackMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf data) {
        return new BackpackMenu(containerId, inventory, ItemStack.EMPTY, data.readVarInt());
    }

    private static int checkedCapacity(int capacity) {
        if (capacity != 9 && capacity != 18 && capacity != 27 && capacity != 36 && capacity != NETHERITE_CAPACITY) {
            throw new IllegalArgumentException("Invalid backpack capacity: " + capacity);
        }
        return capacity;
    }

    public int rows() {
        return capacity / columns();
    }

    public int columns() {
        return capacity == NETHERITE_CAPACITY ? NETHERITE_COLUMNS : STANDARD_COLUMNS;
    }

    public int extraHeight() {
        return rows() * 18 + 4;
    }

    public void sort() {
        BackpackSorter.sort(new InvWrapper(player.getInventory()), PLAYER_INVENTORY_START, PLAYER_INVENTORY_END);
        BackpackSorter.sort(backpackInventory);
        broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.level().isClientSide || CurioBackpacks.findEquipped(player)
                .map(stack -> stack == openStack && stack.getItem() instanceof BackpackItem)
                .orElse(false);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        int backpackEnd = BACKPACK_START + capacity;

        if (index < PLAYER_INVENTORY_START || index == OFFHAND_SLOT) {
            if (!moveItemStackTo(current, PLAYER_INVENTORY_START, HOTBAR_END, false)
                    && !moveItemStackTo(current, BACKPACK_START, backpackEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (index == 0) {
                slot.onQuickCraft(current, original);
            }
        } else if (index < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(current, HOTBAR_START, HOTBAR_END, false)
                    && !moveItemStackTo(current, BACKPACK_START, backpackEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_END) {
            if (!moveItemStackTo(current, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)
                    && !moveItemStackTo(current, BACKPACK_START, backpackEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= BACKPACK_START) {
            if (!moveItemStackTo(current, HOTBAR_START, HOTBAR_END, false)
                    && !moveItemStackTo(current, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (current.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, current);
        if (index == 0) {
            player.drop(current, false);
        }
        return original;
    }

    @Override
    public @NotNull MenuType<?> getType() {
        return ModMenus.BACKPACK.get();
    }
}
