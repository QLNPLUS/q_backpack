package com.qbackpack.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BackpackCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final BackpackItemHandler handler;
    private final LazyOptional<BackpackItemHandler> optional;

    BackpackCapabilityProvider(int capacity, @Nullable CompoundTag nbt) {
        handler = new BackpackItemHandler(capacity);
        optional = LazyOptional.of(() -> handler);
        if (nbt != null && !nbt.isEmpty()) {
            handler.deserializeNBT(nbt);
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        handler.deserializeNBT(nbt);
    }
}
