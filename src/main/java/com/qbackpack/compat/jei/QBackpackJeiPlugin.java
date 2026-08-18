package com.qbackpack.compat.jei;

import com.qbackpack.QBackpack;
import com.qbackpack.client.screen.BackpackScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public final class QBackpackJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(QBackpack.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(BackpackScreen.class, new IGuiContainerHandler<BackpackScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(BackpackScreen screen) {
                if (!screen.hasExpandedBackpackLayout()) {
                    return List.of();
                }
                return List.of(
                        new Rect2i(screen.expandedLayoutLeft(), screen.expandedLayoutTop(),
                                36, screen.expandedLayoutHeight()),
                        new Rect2i(screen.expandedLayoutRight(), screen.expandedLayoutTop(),
                                36, screen.expandedLayoutHeight()));
            }
        });
    }
}
