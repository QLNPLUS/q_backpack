package com.qbackpack.mixin;

import com.qbackpack.compat.TaczAmmoCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.tacz.guns.item.ModernKineticGunScriptAPI", remap = false)
public abstract class TaczGunScriptApiMixin {
    @Shadow
    private LivingEntity shooter;

    @ModifyVariable(
            method = {"lambda$consumeAmmoFromPlayer$4", "lambda$hasAmmoToConsume$5"},
            at = @At("HEAD"),
            argsOnly = true,
            remap = false)
    private IItemHandler qBackpack$includeBackpackInventory(IItemHandler inventory) {
        return TaczAmmoCompat.includeBackpack(shooter, inventory);
    }
}
