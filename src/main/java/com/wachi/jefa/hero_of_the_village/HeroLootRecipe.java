package com.wachi.jefa.hero_of_the_village;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.ArrayList;
import java.util.List;

public record HeroLootRecipe(ResourceKey<VillagerProfession> profession, boolean baby, PoiType workSite, ResourceKey<LootTable> giftsTable) {

    public static List<HeroLootRecipe> recipes = new ArrayList<>(){{
        RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
        var l = registryAccess.lookupOrThrow(Registries.VILLAGER_PROFESSION)
                        .listElements().map(IHolderExtension::getDelegate).toList();
        for (Holder<VillagerProfession> h : l) {
            try{
                var data = h.getData(NeoForgeDataMaps.RAID_HERO_GIFTS);
                if(data != null)
                    registryAccess.lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE).listElements()
                            .filter(h.value().heldJobSite()).findAny().ifPresent(
                                    jobSite -> add(fabric(h.getKey(), false, jobSite.value(), data.lootTable())
                                    ));
            } catch (Exception ignored){}
        }

        add(fabric(VillagerProfession.NONE, false, null, BuiltInLootTables.UNEMPLOYED_GIFT));
        add(fabric(VillagerProfession.NONE, true, null, BuiltInLootTables.BABY_VILLAGER_GIFT));
    }};

    private static HeroLootRecipe fabric(ResourceKey<VillagerProfession> profession, boolean baby, PoiType workSiteId, ResourceKey<LootTable> gift){
        return new HeroLootRecipe(profession, baby, workSiteId, gift);
    }
}
