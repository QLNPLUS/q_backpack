package com.qbackpack.curios;

import com.qbackpack.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public final class CurioBackpacks {
    private CurioBackpacks() {}

    public static Optional<ItemStack> findEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve().flatMap(inventory -> {
            for (var entry : ModItems.BACKPACKS) {
                var result = inventory.findFirstCurio(entry.get());
                if (result.isPresent()) {
                    return Optional.of(result.get().stack());
                }
            }
            return Optional.empty();
        });
    }
}
