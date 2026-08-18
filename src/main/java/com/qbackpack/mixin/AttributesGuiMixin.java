package com.qbackpack.mixin;

import com.qbackpack.client.screen.BackpackScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Keeps Apothic Attributes from moving the host InventoryScreen when its panel
 * is opened. The panel itself is moved left to fit beside the expanded bag.
 */
@Pseudo
@Mixin(targets = "dev.shadowsoffire.attributeslib.client.AttributesGui", remap = false)
public abstract class AttributesGuiMixin {
    private static final String HIDE_UNCHANGED_BUTTON = "hideUnchangedBtn";
    private static final Field LEFT_POS_FIELD = findLeftPosField();

    @Shadow(remap = false)
    @Final
    protected InventoryScreen parent;

    @Shadow(remap = false)
    @Final
    protected ImageButton toggleBtn;

    @Shadow(remap = false)
    @Final
    protected ImageButton recipeBookButton;

    @Shadow(remap = false)
    protected int leftPos;

    @Shadow(remap = false)
    protected int topPos;

    private int qBackpack$parentLeft;
    private int qBackpack$panelLeft;
    private int qBackpack$panelTop;
    private int qBackpack$recipeBookX;
    private int qBackpack$recipeBookY;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void qBackpack$rememberOriginalLayout(InventoryScreen parent, CallbackInfo callback) {
        if (parent instanceof BackpackScreen) {
            // ScreenEvent.Init.Post runs after BackpackScreen has applied its final layout offset.
            qBackpack$parentLeft = parent.getGuiLeft();
            qBackpack$panelLeft = ((BackpackScreen) parent).apothicPanelAnchorLeft();
            qBackpack$panelTop = topPos;
            if (recipeBookButton != null) {
                qBackpack$recipeBookX = recipeBookButton.getX();
                qBackpack$recipeBookY = recipeBookButton.getY();
            }
            qBackpack$positionPanel();
        }
    }

    @Inject(method = "toggleVisibility", at = @At("TAIL"), remap = false)
    private void qBackpack$restoreHostLayout(CallbackInfo callback) {
        if (parent instanceof BackpackScreen) {
            setParentLeft(parent, qBackpack$parentLeft);
            qBackpack$positionPanel();
            if (recipeBookButton != null) {
                recipeBookButton.setPosition(qBackpack$recipeBookX, qBackpack$recipeBookY);
            }
        }
    }

    @Inject(method = "m_88315_", at = @At("TAIL"), remap = false)
    private void qBackpack$positionWidgets(GuiGraphics graphics, int mouseX, int mouseY,
                                            float partialTick, CallbackInfo callback) {
        if (parent instanceof BackpackScreen) {
            setParentLeft(parent, qBackpack$parentLeft);
            qBackpack$positionPanel();
        }
    }

    private void qBackpack$positionPanel() {
        leftPos = qBackpack$panelLeft - 131;
        topPos = qBackpack$panelTop;
        toggleBtn.setPosition(qBackpack$panelLeft + 63, qBackpack$panelTop + 10);

        AbstractWidget hideButton = qBackpack$getHideButton();
        if (hideButton != null) {
            hideButton.setPosition(leftPos + 7, topPos + 151);
        }
    }

    private AbstractWidget qBackpack$getHideButton() {
        try {
            Field field = getClass().getDeclaredField(HIDE_UNCHANGED_BUTTON);
            field.setAccessible(true);
            Object value = field.get(this);
            return value instanceof AbstractWidget widget ? widget : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static void setParentLeft(InventoryScreen parent, int left) {
        if (LEFT_POS_FIELD == null) {
            return;
        }
        try {
            LEFT_POS_FIELD.setInt(parent, left);
        } catch (IllegalAccessException | RuntimeException ignored) {
            // The compatibility layer must not prevent the game from starting.
        }
    }

    private static Field findLeftPosField() {
        for (String name : new String[]{"leftPos", "f_97735_"}) {
            try {
                Field field = AbstractContainerScreen.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Try the name used by the other Minecraft runtime mapping.
            }
        }
        return null;
    }
}
