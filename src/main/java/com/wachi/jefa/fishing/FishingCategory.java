package com.wachi.jefa.fishing;

import com.wachi.jefa.*;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FishingCategory extends AbstractJefaCategory<FishingLoot> {

    public static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "fishing_loot");

    public static final RecipeType<FishingLoot> recipeType = new RecipeType<>(id, FishingLoot.class);

    public FishingCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.FISHING_ROD.getDefaultInstance(), 10, 200, 62, 3);
    }

    @Override
    public RecipeType<FishingLoot> getRecipeType() {
        return recipeType;
    }

    @Override
    public int getGridX() {
        return 3;
    }

    @Override
    public int getGridY() {
        return 4;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.fishing_loot");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishingLoot recipe, IFocusGroup focuses) {
        int x = getGridX(), y = getGridY(), i = 0;

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                BuiltInLootTables.FISHING.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList();
        for (ItemStack output : outputs) {
            if(JEFA.emi_loaded && builder instanceof JemiRecipeLayoutBuilder subBuilder) {
                subBuilder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .addIngredient(VanillaTypes.ITEM_STACK, output);
                i++; x+=16;
                if(i >= columns){
                    x = getGridX(); i = 0; y+=16;
                }
            }
            else
                builder.addSlot(RecipeIngredientRole.OUTPUT)
                        .addIngredient(VanillaTypes.ITEM_STACK, output);
        }
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}
