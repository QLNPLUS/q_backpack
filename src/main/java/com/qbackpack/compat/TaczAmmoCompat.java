package com.qbackpack.compat;

import com.mojang.logging.LogUtils;
import com.qbackpack.curios.CurioBackpacks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;

public final class TaczAmmoCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentMap<String, Boolean> DIAGNOSTIC_PATHS = new ConcurrentHashMap<>();

    private TaczAmmoCompat() {}

    public static <T> LazyOptional<T> getCapabilityIncludingBackpack(
            LivingEntity entity, Capability<T> capability, @Nullable Direction side) {
        LazyOptional<T> original = entity.getCapability(capability, side);
        if (capability != ForgeCapabilities.ITEM_HANDLER) {
            return original;
        }

        return original.resolve()
                .filter(IItemHandler.class::isInstance)
                .map(IItemHandler.class::cast)
                .map(handler -> LazyOptional.<IItemHandler>of(() -> includeBackpack(entity, handler)).<T>cast())
                .orElse(original);
    }

    public static IItemHandler includeBackpack(LivingEntity entity, IItemHandler original) {
        if (entity.level().isClientSide) {
            return ClientAmmoSummary.appendTo(original);
        }

        Optional<ItemStack> equipped = CurioBackpacks.findEquipped(entity);
        Optional<IItemHandler> backpack = equipped
                .flatMap(stack -> stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve());
        logDiagnostic(entity, original, equipped, backpack);
        IItemHandler combined = backpack
                .<IItemHandler>map(handler -> combine(original, handler))
                .orElse(original);

        // Keep Curios ammo boxes in the extraction chain even when another
        // compatibility wrapper has changed the original handler type.
        var curios = CuriosApi.getCuriosInventory(entity).resolve();
        if (curios.isPresent() && !alreadyIncludesCurios(original)) {
            for (var entry : curios.get().getCurios().values()) {
                combined = combine(combined, entry.getStacks());
            }
        }
        return combined;
    }

    private static boolean alreadyIncludesCurios(IItemHandler handler) {
        String name = handler.getClass().getName();
        return name.contains("ItemHandlerWithCurios") || name.contains("InventoryWithCurios");
    }

    public static IItemHandler combine(IItemHandler first, IItemHandler second) {
        return new CombinedItemHandler(first, second);
    }

    private static void logDiagnostic(LivingEntity entity, IItemHandler original,
                                      Optional<ItemStack> equipped, Optional<IItemHandler> backpack) {
        String path = Thread.currentThread().getName();
        if (DIAGNOSTIC_PATHS.putIfAbsent(path, Boolean.TRUE) != null) {
            return;
        }

        StringBuilder contents = new StringBuilder();
        backpack.ifPresent(handler -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    String itemId = String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
                    if (!itemId.startsWith("tacz:")) {
                        continue;
                    }
                    if (contents.length() > 0) {
                        contents.append(", ");
                    }
                    contents.append(slot).append('=').append(itemId).append('x').append(stack.getCount())
                            .append(' ').append(stack.getTag());
                }
            }
        });
        LOGGER.info("[Q Backpack/TACZ] path={}, client={}, entity={}, equipped={}, originalSlots={}, backpackSlots={}, taczContents=[{}]",
                path, entity.level().isClientSide, entity.getName().getString(),
                equipped.map(stack -> stack.getItem().toString()).orElse("none"), original.getSlots(),
                backpack.map(IItemHandler::getSlots).orElse(0), contents);
    }

    private static final class CombinedItemHandler implements IItemHandler {
        private final IItemHandler first;
        private final IItemHandler second;
        private boolean loggedBackpackRead;
        private boolean loggedBackpackExtract;

        private CombinedItemHandler(IItemHandler first, IItemHandler second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int getSlots() {
            return first.getSlots() + second.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            IItemHandler target = handler(slot);
            ItemStack stack = target.getStackInSlot(localSlot(slot));
            if (target == second && !loggedBackpackRead && !stack.isEmpty()) {
                loggedBackpackRead = true;
                LOGGER.info("[Q Backpack/TACZ] TACZ reached backpack slots: combinedSlot={}, backpackSlot={}, item={}, count={}, tag={}",
                        slot, localSlot(slot), ForgeRegistries.ITEMS.getKey(stack.getItem()), stack.getCount(), stack.getTag());
            }
            return stack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return handler(slot).insertItem(localSlot(slot), stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler target = handler(slot);
            ItemStack extracted = target.extractItem(localSlot(slot), amount, simulate);
            if (target == second && !loggedBackpackExtract) {
                loggedBackpackExtract = true;
                LOGGER.info("[Q Backpack/TACZ] TACZ extracted from backpack: combinedSlot={}, backpackSlot={}, requested={}, simulate={}, extracted={}, count={}, tag={}",
                        slot, localSlot(slot), amount, simulate, ForgeRegistries.ITEMS.getKey(extracted.getItem()),
                        extracted.getCount(), extracted.getTag());
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return handler(slot).getSlotLimit(localSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return handler(slot).isItemValid(localSlot(slot), stack);
        }

        private IItemHandler handler(int slot) {
            checkSlot(slot);
            return slot < first.getSlots() ? first : second;
        }

        private int localSlot(int slot) {
            return slot < first.getSlots() ? slot : slot - first.getSlots();
        }

        private void checkSlot(int slot) {
            if (slot < 0 || slot >= getSlots()) {
                throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range [0," + getSlots() + ")");
            }
        }
    }
}
