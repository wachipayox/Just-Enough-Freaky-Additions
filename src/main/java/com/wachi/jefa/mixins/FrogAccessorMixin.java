package com.wachi.jefa.mixins;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frog.class)
public interface FrogAccessorMixin {

    @Accessor("DATA_VARIANT_ID")
    EntityDataAccessor<Holder<FrogVariant>> getFrogVariantData();

}
