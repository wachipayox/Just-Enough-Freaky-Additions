package com.wachi.jefa.network;

import com.wachi.jefa.ClientLootTableReader;
import com.wachi.jefa.JEFA;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;

@EventBusSubscriber()
public record SendLootTable(ResourceKey<LootTable> id, String json) implements CustomPacketPayload {

    public static final Type<SendLootTable> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "loot_table_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SendLootTable> STREAM_CODEC = StreamCodec
            .of((RegistryFriendlyByteBuf buffer, SendLootTable message) -> {
                buffer.writeResourceKey(message.id);
                buffer.writeUtf(message.json);

            }, (
                    RegistryFriendlyByteBuf buffer) -> {
                return new SendLootTable(buffer.readResourceKey(Registries.LOOT_TABLE), buffer.readUtf());

            });

    @Override
    public Type<SendLootTable> type() {
        return TYPE;
    }

    public static void handleData(final SendLootTable message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            context.enqueueWork(() -> {
                JEFA.LOGGER.debug("Received Loot Table Sync on client for table {}", message.id.location());
                ClientLootTableReader.receivedLootTableFromServer(message.id.location(), message.json);

            }).exceptionally(e -> {
                context.connection().disconnect(Component.literal(e.getMessage()));
                return null;
            });
        } else {
            JEFA.LOGGER.warn("Received Loot Table Sync on other side");
        }
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        JEFA.addNetworkMessage(SendLootTable.TYPE, SendLootTable.STREAM_CODEC,
                SendLootTable::handleData);
    }

}
