package com.qbackpack.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.qbackpack.QBackpack;
import com.qbackpack.client.render.BackpackRenderer;
import com.qbackpack.client.screen.BackpackScreen;
import com.qbackpack.curios.CurioBackpacks;
import com.qbackpack.init.ModItems;
import com.qbackpack.init.ModMenus;
import com.qbackpack.network.ModNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class ClientEvents {
    public static final KeyMapping OPEN_BACKPACK = new KeyMapping(
            "key.q_backpack.open",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.q_backpack");

    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = QBackpack.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {}

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                registerBackpackScreen();
                CuriosRendererRegistry.register(ModItems.SMALL_BACKPACK.get(), () -> new BackpackRenderer(
                        BackpackRenderer.SMALL_MODEL));
                CuriosRendererRegistry.register(ModItems.MEDIUM_BACKPACK.get(), () -> new BackpackRenderer(
                        BackpackRenderer.MEDIUM_MODEL));
                CuriosRendererRegistry.register(ModItems.LARGE_BACKPACK.get(), () -> new BackpackRenderer(
                        BackpackRenderer.LARGE_MODEL));
                CuriosRendererRegistry.register(ModItems.HUGE_BACKPACK.get(), () -> new BackpackRenderer(
                        BackpackRenderer.HUGE_MODEL));
                CuriosRendererRegistry.register(ModItems.NETHERITE_BACKPACK.get(), () -> new BackpackRenderer(
                        BackpackRenderer.NETHERITE_MODEL));
            });
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void registerBackpackScreen() {
            MenuScreens.ScreenConstructor constructor = (menu, inventory, title) ->
                    new BackpackScreen((com.qbackpack.menu.BackpackMenu) menu, inventory, title);
            MenuScreens.register((MenuType) ModMenus.BACKPACK.get(), constructor);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_BACKPACK);
        }

        @SubscribeEvent
        public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
            event.register(BackpackRenderer.SMALL_MODEL);
            event.register(BackpackRenderer.MEDIUM_MODEL);
            event.register(BackpackRenderer.LARGE_MODEL);
            event.register(BackpackRenderer.HUGE_MODEL);
            event.register(BackpackRenderer.NETHERITE_MODEL);
        }
    }

    @Mod.EventBusSubscriber(modid = QBackpack.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {
        private ForgeBus() {}

        @SubscribeEvent
        public static void screenOpening(ScreenEvent.Opening event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null
                    && event.getNewScreen() != null
                    && event.getNewScreen().getClass() == InventoryScreen.class
                    && CurioBackpacks.findEquipped(minecraft.player).isPresent()) {
                ModNetworking.openEquippedBackpack();
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.phase == TickEvent.Phase.END && minecraft.player != null && minecraft.screen == null) {
                while (OPEN_BACKPACK.consumeClick()) {
                    ModNetworking.openEquippedBackpack();
                }
            }
        }
    }
}
