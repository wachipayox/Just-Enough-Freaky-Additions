package com.wachi.jefa.mixins;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.Map;

@Mixin(Sheep.class)
public interface SheepAccessorMixin {

    @Accessor("ITEM_BY_DYE")
    static Map<DyeColor, ItemLike> getItemByDyeMap() {
        return new HashMap<>();
    };
}
