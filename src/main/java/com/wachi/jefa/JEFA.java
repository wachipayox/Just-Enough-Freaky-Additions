package com.wachi.jefa;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.wachi.jefa.mixins.LootPoolAccessorMixin;
import com.wachi.jefa.mixins.LootTableAccessorMixin;
import com.wachi.jefa.mixins.NestedLootTableAccessorMixin;
import com.wachi.jefa.network.SendLootTable;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(JEFA.MODID)
public class JEFA {
    public static final String MODID = "jefa";
    public static final Logger LOGGER = LogUtils.getLogger();

    private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
    }

    private static boolean networkingRegistered = false;
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    public JEFA(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerNetworking);
        NeoForge.EVENT_BUS.register(this);
    }

    public static List<ResourceKey<LootTable>> toSync = new ArrayList<>(){{
        add(BuiltInLootTables.PIGLIN_BARTERING);
        add(BuiltInLootTables.TRIAL_CHAMBERS_REWARD);
        add(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS);
        add(BuiltInLootTables.FISHING);
        add(BuiltInLootTables.CAT_MORNING_GIFT);
        add(JefaLootTables.TRIAL_SPAWNER);
        add(JefaLootTables.TRIAL_SPAWNER_OMINOUS);
    }};

    @SubscribeEvent
    public void onServerStartedInitSubTables(ServerStartedEvent event){
        //ADD TO SYNC AUTOMATICALLY ALL VILLAGER PROFESSION GIFTS
        RegistryAccess registryAccess = event.getServer().registryAccess();
        var l = registryAccess.lookupOrThrow(Registries.VILLAGER_PROFESSION)
                .listElements().map(IHolderExtension::getDelegate).toList();
        for (Holder<VillagerProfession> h : l) {
            try{
                var data = h.getData(NeoForgeDataMaps.RAID_HERO_GIFTS);
                if (data != null) toSync.add(data.lootTable());
            } catch (Exception ignored){}
        }

        //ADD TO SYNC ALL SUB TABLES THAT ARE CALLED IN ALREADY SYNCED TABLES
        var toAdd = new ArrayList<ResourceKey<LootTable>>();
        for (ResourceKey<LootTable> key : toSync) {
            toAdd.addAll(syncSubLootTables(key, event.getServer()));
        }
        toAdd.forEach(tA -> {if (!toSync.contains(tA)) toSync.add(tA);});
    }

    public List<ResourceKey<LootTable>> syncSubLootTables(ResourceKey<LootTable> table, MinecraftServer server){
        var toAdd = new ArrayList<ResourceKey<LootTable>>();
        for (LootPool pool : ((LootTableAccessorMixin) server.reloadableRegistries().getLootTable(table)).getPools()) {
            for(LootPoolEntryContainer e : ((LootPoolAccessorMixin)pool).getEntries().stream().filter(
                    e -> e instanceof NestedLootTable
            ).toList()) {
                var nTable = ((NestedLootTableAccessorMixin) e).getContents();
                ResourceLocation tableKey;
                if(nTable.left().isPresent())
                        tableKey = nTable.left().get().location();
                else if(nTable.right().isPresent())
                        tableKey = nTable.right().get().getLootTableId();
                else continue;

                var key = ResourceKey.create(Registries.LOOT_TABLE, tableKey);
                toAdd.add(key);
                JEFA.LOGGER.debug("Added sub loot table: " + key.location());
                toAdd.addAll(syncSubLootTables(key, server));
            }
        }
        return toAdd;
    }
    List<ServerPlayer> syncedPlayers = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void sendJoinPacketToPlayer(OnDatapackSyncEvent event){
        if(!(event.getPlayer() instanceof ServerPlayer sPlayer) || sPlayer.getServer() == null || syncedPlayers.contains(sPlayer)) return;

        RegistryAccess registryAccess = sPlayer.getServer().registryAccess();
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

        for(ResourceKey<LootTable> key : toSync) {
            LootTable table = sPlayer.getServer().reloadableRegistries().getLootTable(key);
            JsonElement json = LootTable.DIRECT_CODEC
                    .encodeStart(ops, table)
                    .getOrThrow();

            if (json == null || !json.isJsonObject()) continue;
            JsonObject obj = json.getAsJsonObject();

            PacketDistributor.sendToPlayer(sPlayer,
                    new SendLootTable(key, obj.toString())
            );
        }
        syncedPlayers.add(sPlayer);
    }

    @SubscribeEvent
    public void onExitServer(ClientPlayerNetworkEvent.LoggingOut event){ClientLootTableReader.onExitServer();}

    @SubscribeEvent
    public void onPlayerLeaves(PlayerEvent.PlayerLoggedOutEvent event){
        if(event.getEntity() instanceof ServerPlayer pl)
            syncedPlayers.remove(pl);
    }

    @SubscribeEvent
    public void onServerStop(ServerStoppedEvent event){
        syncedPlayers.clear();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).optional();
        MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
        networkingRegistered = true;
    }

    public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        if (networkingRegistered)
            throw new IllegalStateException("Cannot register new network messages after networking has been registered");
        MESSAGES.put(id, new NetworkMessage<>(reader, handler));
    }

}
