package com.qbackpack.item;

import com.qbackpack.menu.BackpackMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BackpackItem extends Item {
    private final int capacity;

    public BackpackItem(Properties properties, int capacity) {
        super(properties);
        if (capacity < 9 || capacity > 36 || capacity % 9 != 0) {
            throw new IllegalArgumentException("Backpack capacity must be 9, 18, 27, or 36");
        }
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new BackpackCapabilityProvider(capacity, nbt);
    }

    public static void open(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem backpack)) {
            return;
        }

        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return stack.getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return new BackpackMenu(containerId, inventory, stack, backpack.capacity());
            }
        };
        NetworkHooks.openScreen(player, provider, data -> data.writeVarInt(backpack.capacity()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.q_backpack.capacity", capacity).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.q_backpack.inventory").withStyle(ChatFormatting.DARK_GRAY));
    }
}
