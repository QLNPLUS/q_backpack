package com.qbackpack.mixin;

import com.qbackpack.compat.CuriosCarriedAccessor;
import com.qbackpack.menu.BackpackMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.server.SPacketGrabbedItem;

import java.util.function.Supplier;

@Mixin(CPacketOpenVanilla.class)
public final class CPacketOpenVanillaMixin implements CuriosCarriedAccessor {
    @Shadow(remap = false)
    @Final
    private ItemStack carried;

    @Override
    public ItemStack qBackpack$getCarried() {
        return carried;
    }

    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private static void qBackpack$keepBackpackOpen(CPacketOpenVanilla message,
                                                    Supplier<NetworkEvent.Context> contextSupplier,
                                                    CallbackInfo callback) {
        callback.cancel();
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack carried = player.isCreative()
                    ? ((CuriosCarriedAccessor) message).qBackpack$getCarried()
                    : player.containerMenu.getCarried();
            player.containerMenu.setCarried(ItemStack.EMPTY);

            if (!(player.containerMenu instanceof BackpackMenu)) {
                player.doCloseContainer();
            }

            if (!carried.isEmpty()) {
                player.containerMenu.setCarried(carried);
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SPacketGrabbedItem(carried));
            }
        });
        context.setPacketHandled(true);
    }
}
