package com.qbackpack;

import com.qbackpack.config.ClientConfig;
import com.qbackpack.init.ModItems;
import com.qbackpack.init.ModMenus;
import com.qbackpack.network.ModNetworking;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QBackpack.MOD_ID)
public final class QBackpack {
    public static final String MOD_ID = "q_backpack";

    public QBackpack() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);
        modBus.addListener(this::addCreativeTabContents);
        ModNetworking.register();
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            ModItems.BACKPACKS.forEach(entry -> event.accept(entry.get()));
        }
    }
}
