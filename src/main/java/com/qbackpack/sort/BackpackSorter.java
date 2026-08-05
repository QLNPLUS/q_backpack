package com.qbackpack.sort;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Quark-style category sorting, scoped to a backpack handler. */
public final class BackpackSorter {
    private static final Comparator<ItemStack> ORDER = Comparator
            .comparingInt(BackpackSorter::category)
            .thenComparing(BackpackSorter::categoryDetail)
            .thenComparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
            .thenComparingInt(ItemStack::getDamageValue)
            .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
            .thenComparing(stack -> stack.hasTag() ? stack.getTag().toString() : "");

    private BackpackSorter() {}

    public static void sort(IItemHandler handler) {
        sort(handler, 0, handler.getSlots());
    }

    public static void sort(IItemHandler handler, int start, int end) {
        if (!(handler instanceof IItemHandlerModifiable modifiable)) {
            return;
        }
        if (start < 0 || end > handler.getSlots() || start > end) {
            throw new IllegalArgumentException("Invalid sorting range: " + start + ".." + end);
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = start; slot < end; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                mergeInto(stacks, stack.copy());
            }
        }
        stacks.sort(ORDER);

        int slot = start;
        for (; slot - start < stacks.size() && slot < end; slot++) {
            modifiable.setStackInSlot(slot, stacks.get(slot - start));
        }
        for (; slot < end; slot++) {
            modifiable.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private static void mergeInto(List<ItemStack> stacks, ItemStack incoming) {
        for (ItemStack existing : stacks) {
            if (!ItemStack.isSameItemSameTags(existing, incoming)) {
                continue;
            }
            int moved = Math.min(incoming.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (moved > 0) {
                existing.grow(moved);
                incoming.shrink(moved);
            }
            if (incoming.isEmpty()) {
                return;
            }
        }
        stacks.add(incoming);
    }

    private static int category(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.isEdible()) return 0;
        if (item instanceof BlockItem block && block.getBlock() instanceof TorchBlock) return 1;
        if (item instanceof PickaxeItem) return 2;
        if (item instanceof ShovelItem) return 3;
        if (item instanceof AxeItem) return 4;
        if (item instanceof SwordItem) return 5;
        if (item instanceof DiggerItem) return 6;
        if (item instanceof ArmorItem) return 7;
        if (item instanceof BowItem) return 8;
        if (item instanceof CrossbowItem) return 9;
        if (item instanceof TridentItem) return 10;
        if (item instanceof TippedArrowItem) return 12;
        if (item instanceof ArrowItem) return 11;
        if (item instanceof PotionItem) return 13;
        if (item instanceof MinecartItem) return 14;
        if (item instanceof BlockItem block && block.getBlock() instanceof BaseRailBlock) return 15;
        if (item instanceof DyeItem) return 16;
        if (!(item instanceof BlockItem)) return 17;
        return 18;
    }

    private static String categoryDetail(ItemStack stack) {
        Item item = stack.getItem();
        int power = 0;
        if (item instanceof TieredItem tiered) {
            power = Math.round(tiered.getTier().getSpeed() * 100);
        } else if (item instanceof ArmorItem armor) {
            power = armor.getMaterial().getDefenseForType(armor.getType()) * 100 + armor.getType().ordinal();
        }
        int enchantments = EnchantmentHelper.getEnchantments(stack).entrySet().stream()
                .mapToInt(Map.Entry::getValue).sum();
        return String.format("%08d:%08d", Integer.MAX_VALUE - power, Integer.MAX_VALUE - enchantments);
    }
}
