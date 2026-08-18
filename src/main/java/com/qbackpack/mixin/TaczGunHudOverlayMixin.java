package com.qbackpack.mixin;

import com.qbackpack.compat.ClientAmmoSummary;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.gui.overlay.GunHudOverlay", remap = false)
public abstract class TaczGunHudOverlayMixin {
    @ModifyVariable(method = "handleInventoryAmmo", at = @At("HEAD"), argsOnly = true, remap = false)
    private static Inventory qBackpack$includeBackpackAmmo(Inventory inventory) {
        return ClientAmmoSummary.appendTo(inventory);
    }
}
