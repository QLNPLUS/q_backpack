package com.qbackpack.network;

import com.qbackpack.QBackpack;
import com.qbackpack.compat.ClientAmmoSummary;
import com.qbackpack.item.BackpackItem;
import com.qbackpack.menu.BackpackMenu;
import com.qbackpack.curios.CurioBackpacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

public final class ModNetworking {
    private static final String PROTOCOL = "3";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(QBackpack.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private ModNetworking() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenBackpackMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> {})
                .decoder(buffer -> new OpenBackpackMessage())
                .consumerMainThread((message, contextSupplier) -> {
                    var context = contextSupplier.get();
                    ServerPlayer player = context.getSender();
                    if (player != null && player.containerMenu != null) {
                        ItemStack carried = player.containerMenu.getCarried().copy();
                        player.containerMenu.setCarried(ItemStack.EMPTY);
                        try {
                            CurioBackpacks.findEquipped(player)
                                    .ifPresent(stack -> BackpackItem.open(player, stack));
                        } finally {
                            player.containerMenu.setCarried(carried);
                        }
                    }
                    context.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(CloseBackpackMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> {})
                .decoder(buffer -> new CloseBackpackMessage())
                .consumerMainThread((message, contextSupplier) -> {
                    var context = contextSupplier.get();
                    ServerPlayer player = context.getSender();
                    if (player != null && player.containerMenu instanceof BackpackMenu) {
                        player.closeContainer();
                    }
                    context.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(SortBackpackMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> {})
                .decoder(buffer -> new SortBackpackMessage())
                .consumerMainThread((message, contextSupplier) -> {
                    var context = contextSupplier.get();
                    ServerPlayer player = context.getSender();
                    if (player != null && player.containerMenu instanceof BackpackMenu menu) {
                        menu.sort();
                    }
                    context.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(TaczAmmoSummaryMessage.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ModNetworking::encodeTaczAmmoSummary)
                .decoder(ModNetworking::decodeTaczAmmoSummary)
                .consumerMainThread((message, contextSupplier) -> {
                    ClientAmmoSummary.update(message.stacks());
                    contextSupplier.get().setPacketHandled(true);
                })
                .add();
    }

    public static void openEquippedBackpack() {
        CHANNEL.sendToServer(new OpenBackpackMessage());
    }

    public static void sortBackpack() {
        CHANNEL.sendToServer(new SortBackpackMessage());
    }

    public static void closeBackpack() {
        CHANNEL.sendToServer(new CloseBackpackMessage());
    }

    public static void syncTaczAmmo(ServerPlayer player, List<ItemStack> stacks) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TaczAmmoSummaryMessage(stacks));
    }

    private static void encodeTaczAmmoSummary(TaczAmmoSummaryMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.stacks().size());
        message.stacks().forEach(buffer::writeItem);
    }

    private static TaczAmmoSummaryMessage decodeTaczAmmoSummary(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            stacks.add(buffer.readItem());
        }
        return new TaczAmmoSummaryMessage(stacks);
    }

    private record OpenBackpackMessage() {}
    private record CloseBackpackMessage() {}
    private record SortBackpackMessage() {}
    private record TaczAmmoSummaryMessage(List<ItemStack> stacks) {}
}
