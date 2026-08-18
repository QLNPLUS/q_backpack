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
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.List;

public final class BackpackScreen extends InventoryScreen {
    private static final int EXPANDED_LAYOUT_OFFSET = 36;
    private static final int EXPANDED_TEXTURE_OVERHANG = 36;
    private static final int APOTHIC_PANEL_BACKPACK_GAP = 0;
    private static final ResourceLocation SMALL_BACKGROUND = background("small_backpack");
    private static final ResourceLocation MEDIUM_BACKGROUND = background("medium_backpack");
    private static final ResourceLocation LARGE_BACKGROUND = background("large_backpack");
    private static final ResourceLocation HUGE_BACKGROUND = background("huge_backpack");
    private static final ResourceLocation NETHERITE_BACKGROUND = background("netherite_backpack");
    private static final ResourceLocation ICONS = new ResourceLocation(
            QBackpack.MOD_ID, "textures/gui/general_icons.png");
    private static final String CNPC_TAB_CLASS = "noppes.npcs.client.gui.player.tabs.AbstractTab";
    private static final String CNPC_VANILLA_TAB_CLASS =
            "noppes.npcs.client.gui.player.tabs.InventoryTabVanilla";
    private static final String APOTHIC_ATTRIBUTES_GUI =
            "dev.shadowsoffire.attributeslib.client.AttributesGui";
    private static final String APOTHIC_ATTRIBUTES_WIDGET =
            APOTHIC_ATTRIBUTES_GUI + "$";
    private static final int APOTHIC_PANEL_WIDTH = 131;
    private static final int APOTHIC_PANEL_HEIGHT = 166;

    private static InventoryMenu previousInventoryMenu;

    private final BackpackMenu backpackMenu;
    private final Player player;
    private final ResourceLocation background;
    private GuiEventListener apothicMouseTarget;

    public BackpackScreen(BackpackMenu menu, Inventory inventory, Component title) {
        super(useBackpackMenu(inventory.player, menu));
        this.backpackMenu = menu;
        this.player = inventory.player;
        this.background = backgroundForMenu(menu);
        restoreInventoryMenu(player);
    }

    private static ResourceLocation background(String name) {
        return new ResourceLocation(QBackpack.MOD_ID, "textures/gui/" + name + ".png");
    }

    private static ResourceLocation backgroundForMenu(BackpackMenu menu) {
        if (menu.columns() == 13) {
            return NETHERITE_BACKGROUND;
        }
        return switch (menu.rows()) {
            case 1 -> SMALL_BACKGROUND;
            case 2 -> MEDIUM_BACKGROUND;
            case 3 -> LARGE_BACKGROUND;
            case 4 -> HUGE_BACKGROUND;
            default -> throw new IllegalArgumentException("Invalid backpack rows: " + menu.rows());
        };
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

    public boolean hasExpandedBackpackLayout() {
        return backpackMenu.columns() == 13;
    }

    public int expandedLayoutLeft() {
        return backgroundLeft();
    }

    public int expandedLayoutRight() {
        return backgroundLeft() + imageWidth - 36;
    }

    public int expandedLayoutTop() {
        return topPos;
    }

    public int expandedLayoutHeight() {
        return imageHeight;
    }

    public int apothicPanelAnchorLeft() {
        return getGuiLeft() - (hasExpandedBackpackLayout() ? APOTHIC_PANEL_BACKPACK_GAP : 0);
    }

    @Override
    protected void init() {
        imageWidth = backpackMenu.columns() == 13 ? 248 : 176;
        imageHeight = 170 + backpackMenu.rows() * 18;
        super.init();
        if (hasExpandedBackpackLayout()) {
            leftPos += EXPANDED_LAYOUT_OFFSET;
            for (Renderable renderable : renderables) {
                if (renderable instanceof ImageButton button
                        && !isApothicAttributesWidget(renderable)) {
                    button.setPosition(button.getX() + EXPANDED_LAYOUT_OFFSET, button.getY());
                }
            }
        }
        addRenderableWidget(new SortButton(leftPos + 176 - 18, topPos + 69));
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        List<Renderable> apothicRenderables = takeApothicRenderables();
        try {
            super.render(graphics, mouseX, mouseY, partialTick);
        } finally {
            renderables.addAll(apothicRenderables);
        }

        if (apothicRenderables.isEmpty()) {
            return;
        }

        graphics.flush();
        minecraft.renderBuffers().bufferSource().endBatch();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        for (Renderable renderable : apothicRenderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.flush();
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        apothicMouseTarget = findApothicMouseTarget(mouseX, mouseY);
        if (apothicMouseTarget != null) {
            apothicMouseTarget.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (apothicMouseTarget != null) {
            apothicMouseTarget.mouseReleased(mouseX, mouseY, button);
            apothicMouseTarget = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (apothicMouseTarget != null) {
            apothicMouseTarget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        GuiEventListener attributesGui = findApothicGui();
        if (attributesGui != null && isApothicOpen(attributesGui)
                && isInsideApothicPanel(mouseX, mouseY)) {
            attributesGui.mouseScrolled(mouseX, mouseY, scrollDelta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(background, backgroundLeft(), topPos, 0, 0, imageWidth, imageHeight);
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

    private int backgroundLeft() {
        return leftPos - (hasExpandedBackpackLayout() ? EXPANDED_TEXTURE_OVERHANG : 0);
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

    private static boolean isApothicAttributesWidget(Renderable renderable) {
        return renderable.getClass().getName().startsWith(APOTHIC_ATTRIBUTES_WIDGET);
    }

    private List<Renderable> takeApothicRenderables() {
        List<Renderable> overlay = new ArrayList<>();
        for (Renderable renderable : renderables) {
            String className = renderable.getClass().getName();
            if (APOTHIC_ATTRIBUTES_GUI.equals(className)
                    || className.startsWith(APOTHIC_ATTRIBUTES_WIDGET)) {
                overlay.add(renderable);
            }
        }
        renderables.removeAll(overlay);
        return overlay;
    }

    private GuiEventListener findApothicMouseTarget(double mouseX, double mouseY) {
        GuiEventListener attributesGui = null;
        List<? extends GuiEventListener> listeners = children();
        for (int index = listeners.size() - 1; index >= 0; index--) {
            GuiEventListener listener = listeners.get(index);
            String className = listener.getClass().getName();
            if (className.startsWith(APOTHIC_ATTRIBUTES_WIDGET)
                    && listener instanceof AbstractWidget widget
                    && widget.visible && widget.isActive()
                    && widget.isMouseOver(mouseX, mouseY)) {
                return listener;
            }
            if (APOTHIC_ATTRIBUTES_GUI.equals(className)) {
                attributesGui = listener;
            }
        }

        if (attributesGui != null && isApothicOpen(attributesGui)
                && isInsideApothicPanel(mouseX, mouseY)) {
            return attributesGui;
        }
        return null;
    }

    private GuiEventListener findApothicGui() {
        for (GuiEventListener listener : children()) {
            if (APOTHIC_ATTRIBUTES_GUI.equals(listener.getClass().getName())) {
                return listener;
            }
        }
        return null;
    }

    private boolean isInsideApothicPanel(double mouseX, double mouseY) {
        int panelLeft = getGuiLeft() - APOTHIC_PANEL_WIDTH;
        return mouseX >= panelLeft && mouseX < panelLeft + APOTHIC_PANEL_WIDTH
                && mouseY >= getGuiTop() && mouseY < getGuiTop() + APOTHIC_PANEL_HEIGHT;
    }

    private static boolean isApothicOpen(GuiEventListener attributesGui) {
        try {
            Field open = attributesGui.getClass().getDeclaredField("open");
            open.setAccessible(true);
            return open.getBoolean(attributesGui);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
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
