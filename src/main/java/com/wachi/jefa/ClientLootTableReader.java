package com.wachi.jefa;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.wachi.jefa.mixins.LootTableAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.Reader;
import java.util.*;

public final class ClientLootTableReader {

    private static final Map<ResourceLocation, List<LootPool>> synced_loot_tables = new HashMap<>();

    public static void onExitServer(){
        synced_loot_tables.clear();
        LootEntryPreviewBuilder.LootProbEngine.CACHE.clear();
        LootEntryPreviewBuilder.LootProbEngine.VISITING = ThreadLocal.withInitial(HashSet::new);
    }

    public static List<LootPool> getLootPools(ResourceLocation id){
        return synced_loot_tables.getOrDefault(id, getFromClientData(id));
    }

    private static List<LootPool> getFromClientData(ResourceLocation id){
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation res = ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "client_data/" + lootTableToJsonLocation(id));

        List<LootPool> r = new ArrayList<>();
        mc.getResourceManager().getResource(res).ifPresent(resource -> {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                r.addAll(receivedLootTableFromServer(id, json.toString()));
            } catch (Exception e) {
                JEFA.LOGGER.error("Error parsing client data file {}. Error: {}", res, e.getMessage());
            }
        });
        return r;
    }

    private static String lootTableToJsonLocation(ResourceLocation id) {
        return id.getNamespace() + "/loot_table/" + id.getPath() + ".json";
    }

    public static List<LootPool> receivedLootTableFromServer(ResourceLocation id, String jsonString) {
        List<LootPool> r = new ArrayList<>();
        try {
            JsonElement json = JsonParser.parseString(jsonString);

            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

            DataResult<LootTable> parsed =
                    LootTable.DIRECT_CODEC.parse(ops, json);

            parsed.resultOrPartial(
                    msg -> JEFA.LOGGER.error("Failed to parse loot table {}: {}", id, msg)
                    ).ifPresent(table -> r.addAll(extractPools(id, table)));
        } catch (Exception e) {
            JEFA.LOGGER.error("Error reading loot table {} from packet, reason: {}", id, e);
        }
        return r;
    }

    private static List<LootPool> extractPools(ResourceLocation id, LootTable table) {
        List<LootPool> pools = ((LootTableAccessorMixin) table).getPools();

        if(!pools.isEmpty()) synced_loot_tables.put(id, pools);
        return pools;
    }
}
