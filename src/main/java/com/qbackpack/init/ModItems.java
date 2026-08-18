package com.qbackpack.init;

import com.qbackpack.QBackpack;
import com.qbackpack.item.BackpackItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, QBackpack.MOD_ID);

    public static final RegistryObject<BackpackItem> SMALL_BACKPACK = register("small_backpack", 9);
    public static final RegistryObject<BackpackItem> MEDIUM_BACKPACK = register("medium_backpack", 18);
    public static final RegistryObject<BackpackItem> LARGE_BACKPACK = register("large_backpack", 27);
    public static final RegistryObject<BackpackItem> HUGE_BACKPACK = register("huge_backpack", 36);
    public static final RegistryObject<BackpackItem> NETHERITE_BACKPACK = register("netherite_backpack", 52);

    public static final List<RegistryObject<BackpackItem>> BACKPACKS = List.of(
            SMALL_BACKPACK, MEDIUM_BACKPACK, LARGE_BACKPACK, HUGE_BACKPACK, NETHERITE_BACKPACK);

    private ModItems() {}

    private static RegistryObject<BackpackItem> register(String name, int capacity) {
        return ITEMS.register(name, () -> new BackpackItem(new Item.Properties().stacksTo(1), capacity));
    }
}
