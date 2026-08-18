package com.qbackpack.mixin;

import com.qbackpack.compat.TaczAmmoCompat;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.tacz.guns.api.item.gun.AbstractGunItem", remap = false)
public abstract class TaczAbstractGunItemMixin {
    @Unique
    private static final ThreadLocal<LivingEntity> Q_BACKPACK$RELOAD_SHOOTER = new ThreadLocal<>();

    @Inject(method = "canReload", at = @At("HEAD"), remap = false)
    private void qBackpack$captureReloadShooter(
            LivingEntity shooter, ItemStack gun, CallbackInfoReturnable<Boolean> callback) {
        Q_BACKPACK$RELOAD_SHOOTER.set(shooter);
    }

    @ModifyVariable(
            method = "lambda$canReload$1",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false)
    private static IItemHandler qBackpack$includeBackpackForReload(IItemHandler inventory) {
        LivingEntity shooter = Q_BACKPACK$RELOAD_SHOOTER.get();
        return shooter == null ? inventory : TaczAmmoCompat.includeBackpack(shooter, inventory);
    }

    @Inject(method = "canReload", at = @At("RETURN"), remap = false)
    private void qBackpack$clearReloadShooter(
            LivingEntity shooter, ItemStack gun, CallbackInfoReturnable<Boolean> callback) {
        Q_BACKPACK$RELOAD_SHOOTER.remove();
    }

    @Redirect(
            method = "hasInventoryAmmo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"),
            remap = false)
    private LazyOptional<?> qBackpack$includeBackpackInventory(
            LivingEntity entity, Capability<?> capability, Direction side) {
        return TaczAmmoCompat.getCapabilityIncludingBackpack(entity, capability, side);
    }
}
