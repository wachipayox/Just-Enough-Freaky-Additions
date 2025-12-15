package com.wachi.jefa.mob_interaction;

import com.ibm.icu.impl.Pair;
import com.wachi.jefa.mixins.SheepAccessorMixin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record MobInteractionRecipe(
        Function<Level, Pair<Entity, ItemStack>> mobMid,
        Function<Level, Pair<Entity, ItemStack>> mobIn,
        ItemStack itemIn,
        ItemStack itemOut
){
    public static List<MobInteractionRecipe> recipes = new ArrayList<>(){{
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Cow(EntityType.COW, level), Items.COW_SPAWN_EGG.getDefaultInstance()),
                null, Items.BUCKET.getDefaultInstance(), Items.MILK_BUCKET.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new MushroomCow(EntityType.MOOSHROOM, level), Items.MOOSHROOM_SPAWN_EGG.getDefaultInstance()),
                null, Items.BUCKET.getDefaultInstance(), Items.MILK_BUCKET.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new MushroomCow(EntityType.MOOSHROOM, level), Items.MOOSHROOM_SPAWN_EGG.getDefaultInstance()),
                null, Items.BOWL.getDefaultInstance(), Items.MUSHROOM_STEW.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Chicken(EntityType.CHICKEN, level), Items.CHICKEN_SPAWN_EGG.getDefaultInstance()),
                null, null, Items.EGG.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Armadillo(EntityType.ARMADILLO, level), Items.ARMADILLO_SPAWN_EGG.getDefaultInstance()),
                null, Items.BRUSH.getDefaultInstance(), Items.ARMADILLO_SCUTE.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(new Turtle(EntityType.TURTLE, level), Items.TURTLE_SPAWN_EGG.getDefaultInstance()),
                (level) -> {
                    var turtle = new Turtle(EntityType.TURTLE, level);
                    turtle.setBaby(true);
                    return Pair.of(turtle, Items.TURTLE_SPAWN_EGG.getDefaultInstance());
                },
                null, Items.TURTLE_SCUTE.getDefaultInstance()
        ));

        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariant.TEMPERATE, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new Slime(EntityType.SLIME, level), Items.SLIME_SPAWN_EGG.getDefaultInstance()),
                null, Items.SLIME_BALL.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariant.TEMPERATE, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new MagmaCube(EntityType.MAGMA_CUBE, level), Items.MAGMA_CUBE_SPAWN_EGG.getDefaultInstance()),
                null, Items.OCHRE_FROGLIGHT.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariant.WARM, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new MagmaCube(EntityType.MAGMA_CUBE, level), Items.MAGMA_CUBE_SPAWN_EGG.getDefaultInstance()),
                null, Items.PEARLESCENT_FROGLIGHT.getDefaultInstance()
        ));
        add(new MobInteractionRecipe(
                (level) -> Pair.of(getFrogVariant(FrogVariant.COLD, level), Items.FROG_SPAWN_EGG.getDefaultInstance()),
                (level) -> Pair.of(new MagmaCube(EntityType.MAGMA_CUBE, level), Items.MAGMA_CUBE_SPAWN_EGG.getDefaultInstance()),
                null, Items.VERDANT_FROGLIGHT.getDefaultInstance()
        ));

        //sheep for each dye color
        for(var entry : SheepAccessorMixin.getItemByDyeMap().entrySet()){
            add(new MobInteractionRecipe(
                    (level) -> {
                        var s = new Sheep(EntityType.SHEEP, level);
                        s.setColor(entry.getKey());
                        return Pair.of(s, Items.SHEEP_SPAWN_EGG.getDefaultInstance());
                    },
                    null, Items.SHEARS.getDefaultInstance(), entry.getValue().asItem().getDefaultInstance()
            ));
        }
    }};

    private static Frog getFrogVariant(ResourceKey<FrogVariant> variant, Level level){
        var frog = new Frog(EntityType.FROG, level);
        frog.setVariant(BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(variant));
        return frog;
    }
}
