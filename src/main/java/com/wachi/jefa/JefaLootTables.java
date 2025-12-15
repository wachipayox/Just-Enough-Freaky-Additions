package com.wachi.jefa;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public class JefaLootTables {

    //THIS LOOT TABLES ARE SIMULATIONS OF VANILLA TRIAL SPAWNERS HARDCODED BEHAVIOUR
    public static ResourceKey<LootTable> TRIAL_SPAWNER = register("trial_chamber/trial_spawner");
    public static ResourceKey<LootTable> TRIAL_SPAWNER_OMINOUS = register("trial_chamber/trial_spawner_ominous");

    private static ResourceKey<LootTable> register(String path){
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(JEFA.MODID, path)
        );
    };
}
