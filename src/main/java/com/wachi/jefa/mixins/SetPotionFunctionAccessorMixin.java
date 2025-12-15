package com.wachi.jefa.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetPotionFunction.class)
public interface SetPotionFunctionAccessorMixin {

    @Accessor("potion")
    Holder<Potion> getPotion();
}
