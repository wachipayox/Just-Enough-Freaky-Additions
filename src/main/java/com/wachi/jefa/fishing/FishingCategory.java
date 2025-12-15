package com.wachi.jefa.fishing;

import com.wachi.jefa.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FishingCategory extends AbstractJefaCategory<FishingLoot> {

    public static final RecipeType<FishingLoot> recipeType = RecipeType.create(JEFA.MODID, "fishing_loot", FishingLoot.class);

    public FishingCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.FISHING_ROD.getDefaultInstance(), 10, 200, 62, 3);
    }

    @Override
    public RecipeType<FishingLoot> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.fishing_loot");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishingLoot recipe, IFocusGroup focuses) {
        scrollGridFactory.setPosition(3, 4);

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                BuiltInLootTables.FISHING.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList();
        for (ItemStack output : outputs) {
            builder.addSlotToWidget(RecipeIngredientRole.OUTPUT, scrollGridFactory)
                    .addIngredient(VanillaTypes.ITEM_STACK, output);
        }
    }
}
