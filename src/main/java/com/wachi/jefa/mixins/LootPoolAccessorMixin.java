package com.wachi.jefa.mixins;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootPool.class)
public interface LootPoolAccessorMixin {

    @Accessor("entries")
    List<LootPoolEntryContainer> getEntries();

    @Accessor("conditions")
    List<LootItemCondition> getConditions();
}
