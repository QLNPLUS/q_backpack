package com.qbackpack.mixin;

import com.qbackpack.compat.TaczAmmoCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.animation.statemachine.GunAnimationStateContext", remap = false)
public abstract class TaczGunAnimationStateContextMixin {
    @ModifyVariable(
            method = "lambda$hasAmmoToConsume$7",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false)
    private IItemHandler qBackpack$includeBackpackInventory(IItemHandler inventory) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return inventory;
        }
        return TaczAmmoCompat.includeBackpack(player, inventory);
    }
}
