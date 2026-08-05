package com.qbackpack.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qbackpack.QBackpack;
import com.qbackpack.config.ClientConfig;
import com.qbackpack.curios.CurioBackpacks;
import com.qbackpack.menu.BackpackMenu;
import com.qbackpack.network.ModNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

import java.lang.reflect.Field;

public final class BackpackScreen extends InventoryScreen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            QBackpack.MOD_ID, "textures/gui/backpack_gui.png");
    private static final ResourceLocation ICONS = new ResourceLocation(
            QBackpack.MOD_ID, "textures/gui/general_icons.png");
    private static final int TOP_SECTION_HEIGHT = 82;
    private static final int SOURCE_BACKPACK_ROWS = 3;
    private static final int SOURCE_LOWER_Y = 136;
    private static final int LOWER_SECTION_HEIGHT = 88;
    private static final String CNPC_TAB_CLASS = "noppes.npcs.client.gui.player.tabs.AbstractTab";
    private static final String CNPC_VANILLA_TAB_CLASS =
            "noppes.npcs.client.gui.player.tabs.InventoryTabVanilla";

    private static InventoryMenu previousInventoryMenu;

    private final BackpackMenu backpackMenu;
    private final Player player;

    public BackpackScreen(BackpackMenu menu, Inventory inventory, Component title) {
        super(useBackpackMenu(inventory.player, menu));
        this.backpackMenu = menu;
        this.player = inventory.player;
        restoreInventoryMenu(player);
    }

    private static Player useBackpackMenu(Player player, BackpackMenu menu) {
        previousInventoryMenu = player.inventoryMenu;
        player.inventoryMenu = menu;
        return player;
    }

    private static void restoreInventoryMenu(Player player) {
        player.inventoryMenu = previousInventoryMenu;
        previousInventoryMenu = null;
    }

    @Override
    protected void init() {
        imageHeight = 170 + backpackMenu.rows() * 18;
        super.init();
        addRenderableWidget(new SortButton(leftPos + imageWidth - 18, topPos + 69));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (CurioBackpacks.findEquipped(player).isEmpty()) {
            ModNetworking.closeBackpack();
            minecraft.setScreen(new InventoryScreen(player));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, TOP_SECTION_HEIGHT);

        int rowsFromSource = Math.min(backpackMenu.rows(), SOURCE_BACKPACK_ROWS);
        int rowDestinationY = topPos + TOP_SECTION_HEIGHT;
        graphics.blit(BACKGROUND, leftPos, rowDestinationY, 0, TOP_SECTION_HEIGHT,
                imageWidth, rowsFromSource * 18);
        for (int row = SOURCE_BACKPACK_ROWS; row < backpackMenu.rows(); row++) {
            graphics.blit(BACKGROUND, leftPos, rowDestinationY + row * 18,
                    0, TOP_SECTION_HEIGHT + (SOURCE_BACKPACK_ROWS - 1) * 18,
                    imageWidth, 18);
        }

        int lowerDestinationY = rowDestinationY + backpackMenu.rows() * 18;
        graphics.blit(BACKGROUND, leftPos, lowerDestinationY, 0, SOURCE_LOWER_Y,
                imageWidth, LOWER_SECTION_HEIGHT);
        renderEntityInInventoryFollowsMouse(graphics, leftPos + 51, topPos + 75, 30,
                leftPos + 51 - mouseX, topPos + 25 - mouseY, minecraft.player);
        alignCuriosButton();
        alignCustomNpcTabs();
    }

    private void alignCuriosButton() {
        int vanillaY = height / 2 - 22;
        int offset = backpackMenu.extraHeight() / 2;
        for (Renderable renderable : renderables) {
            if (renderable instanceof ImageButton button && button.getY() == vanillaY) {
                button.setPosition(button.getX(), vanillaY - offset);
            }
        }
    }

    private void alignCustomNpcTabs() {
        int vanillaY = height / 2 - 110;
        int backpackY = vanillaY - backpackMenu.extraHeight() / 2;
        for (Renderable renderable : renderables) {
            if (isCustomNpcTab(renderable)) {
                if (renderable instanceof AbstractWidget widget && widget.getY() == vanillaY) {
                    widget.setPosition(widget.getX(), backpackY);
                }
                markBackpackAsVanillaInventoryTab(renderable);
            }
        }
    }

    private static boolean isCustomNpcTab(Renderable renderable) {
        for (Class<?> type = renderable.getClass(); type != null; type = type.getSuperclass()) {
            if (CNPC_TAB_CLASS.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void markBackpackAsVanillaInventoryTab(Renderable renderable) {
        if (!CNPC_VANILLA_TAB_CLASS.equals(renderable.getClass().getName())) {
            return;
        }
        try {
            Class<?> abstractTab = renderable.getClass().getSuperclass();
            Field screenClass = abstractTab.getDeclaredField("screenClass");
            screenClass.setAccessible(true);
            if (screenClass.get(renderable) != BackpackScreen.class) {
                screenClass.set(renderable, BackpackScreen.class);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // CNPC is optional and may change its internal tab implementation.
        }
    }

    private final class SortButton extends Button {
        private SortButton(int x, int y) {
            super(Button.builder(Component.translatable("gui.q_backpack.sort"), button -> {
                ModNetworking.sortBackpack();
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, 1.0F);
            }).pos(x, y).size(10, 10));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            graphics.blit(ICONS, getX(), getY(), 0, 25 + (isHovered ? 10 : 0), 10, 10);
            if (isHovered && ClientConfig.SHOW_SORT_BUTTON_TOOLTIP.get()) {
                graphics.renderTooltip(font, Component.translatable("gui.q_backpack.sort"), mouseX, mouseY);
            }
        }
    }
}
