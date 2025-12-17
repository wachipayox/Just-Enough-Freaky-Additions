package com.wachi.jefa.mob_interaction;

import com.ibm.icu.impl.Pair;
import com.wachi.jefa.LootEntryPreviewBuilder;
import com.wachi.jefa.mixins.EntityAccessorMixin;
import com.wachi.jefa.mixins.FrogAccessorMixin;
import net.minecraft.data.loot.packs.LootData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record MobInteractionRecipe(
        Function<Level, Pair<Entity, ItemStack>> mobMid,
        Function<Level, Pair<Entity, ItemStack>> mobIn,
        ItemStack itemIn,
        ItemStack itemOut,
        Function<Entity, List<ItemStack>> itemOutFunction
){
    public static List<MobInteractionRecipe> recipes = new ArrayList<>(){{
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Cow(EntityType.COW, level), Items.COW_SPAWN_EGG.getDefaultInstance()),
                null, Items.BUCKET.getDefaultInstance(), Items.MILK_BUCKET.getDefaultInstance(), null
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new MushroomCow(EntityType.MOOSHROOM, level), Items.MOOSHROOM_SPAWN_EGG.getDefaultInstance()),
                null, Items.BUCKET.getDefaultInstance(), Items.MILK_BUCKET.getDefaultInstance(), null
        ));

        add(new MobInteractionRecipe(
                (level) -> Pair.of(new MushroomCow(EntityType.MOOSHROOM, level), Items.MOOSHROOM_SPAWN_EGG.getDefaultInstance()),
                null, Items.BOWL.getDefaultInstance(), Items.MUSHROOM_STEW.getDefaultInstance(), null
        ));

        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Armadillo(EntityType.ARMADILLO, level), Items.ARMADILLO_SPAWN_EGG.getDefaultInstance()),
                null, Items.BRUSH.getDefaultInstance(), null,
                e -> LootEntryPreviewBuilder.buildPreviewsForLootTable(BuiltInLootTables.ARMADILLO_SHED.location())
                        .stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Turtle(EntityType.TURTLE, level), Items.TURTLE_SPAWN_EGG.getDefaultInstance()),
                (level) -> {
                    var turtle = new Turtle(EntityType.TURTLE, level);
                    turtle.setBaby(true);
                    return Pair.of(turtle, Items.TURTLE_SPAWN_EGG.getDefaultInstance());
                },
                null, Items.TURTLE_SCUTE.getDefaultInstance(), null
        ));

        add(new MobInteractionRecipe(
                (level) -> Pair.of(getChickenVariant(ChickenVariants.TEMPERATE, level), Items.CHICKEN_SPAWN_EGG.getDefaultInstance()),
                null, null, Items.EGG.getDefaultInstance(), null
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getChickenVariant(ChickenVariants.WARM, level), Items.CHICKEN_SPAWN_EGG.getDefaultInstance()),
                null, null, Items.BROWN_EGG.getDefaultInstance(), null
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getChickenVariant(ChickenVariants.COLD, level), Items.CHICKEN_SPAWN_EGG.getDefaultInstance()),
                null, null, Items.BLUE_EGG.getDefaultInstance(), null
        ));

        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariants.TEMPERATE, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new Slime(EntityType.SLIME, level), Items.SLIME_SPAWN_EGG.getDefaultInstance()),
                null, Items.SLIME_BALL.getDefaultInstance(), null
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariants.TEMPERATE, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new MagmaCube(EntityType.MAGMA_CUBE, level), Items.MAGMA_CUBE_SPAWN_EGG.getDefaultInstance()),
                null, Items.OCHRE_FROGLIGHT.getDefaultInstance(), null
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariants.WARM, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new MagmaCube(EntityType.MAGMA_CUBE, level), Items.MAGMA_CUBE_SPAWN_EGG.getDefaultInstance()),
                null, Items.PEARLESCENT_FROGLIGHT.getDefaultInstance(), null
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariants.COLD, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new MagmaCube(EntityType.MAGMA_CUBE, level), Items.MAGMA_CUBE_SPAWN_EGG.getDefaultInstance()),
                null, Items.VERDANT_FROGLIGHT.getDefaultInstance(), null
        ));

        //sheep for each dye color
        for(var entry : LootData.WOOL_ITEM_BY_DYE.entrySet()){
            add(new MobInteractionRecipe(
                    (level) -> {
                        var s = new Sheep(EntityType.SHEEP, level);
                        s.setColor(entry.getKey());
                        return Pair.of(s, Items.SHEEP_SPAWN_EGG.getDefaultInstance());
                    },
                    null, Items.SHEARS.getDefaultInstance(), null,
                    e -> {
                        if(BuiltInLootTables.SHEAR_SHEEP_BY_DYE.get(entry.getKey()) instanceof ResourceKey<LootTable> key)
                            return LootEntryPreviewBuilder.buildPreviewsForLootTable(key.location())
                                    .stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList();
                        else
                            return List.of(entry.getValue().asItem().getDefaultInstance());
                    }
            ));

        }
    }};

    private static Chicken getChickenVariant(ResourceKey<ChickenVariant> variant, Level level){
        var chicken = new Chicken(EntityType.CHICKEN, level);
        chicken.setVariant(VariantUtils.getDefaultOrAny(level.registryAccess(), variant));
        return chicken;
    }

    private static Frog getFrogVariant(ResourceKey<FrogVariant> variant, Level level){
        var frog = new Frog(EntityType.FROG, level);
        ((EntityAccessorMixin)frog).getEntityData().set(((FrogAccessorMixin)frog).getFrogVariantData(), VariantUtils.getDefaultOrAny(level.registryAccess(), variant));
        return frog;
    }
}
