package com.qbackpack.curios;

import com.qbackpack.QBackpack;
import com.qbackpack.item.BackpackItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;

@Mod.EventBusSubscriber(modid = QBackpack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CuriosEvents {
    private static final ResourceLocation CURIO_CAPABILITY = new ResourceLocation(QBackpack.MOD_ID, "curio");

    private CuriosEvents() {}

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof BackpackItem) {
            event.addCapability(CURIO_CAPABILITY, CuriosApi.createCurioProvider(new BackpackCurio(stack)));
        }
    }
}
