package com.wachi.jefa.fishing;

import com.wachi.jefa.*;
import com.wachi.jefa.piglin.PiglinTrade;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlotBuilder;
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
import net.minecraft.resources.ResourceLocation;
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
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.fishing_loot");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishingLoot recipe, IFocusGroup focuses) {
        int x = 3, y = 4, i = 0;
        scrollGridFactory.setPosition(x, y);

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                BuiltInLootTables.FISHING.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList();
        for (ItemStack output : outputs) {
            if(JEFA.emi_loaded && builder instanceof JemiRecipeLayoutBuilder subBuilder) {
                JemiRecipeSlotBuilder sB = (JemiRecipeSlotBuilder) subBuilder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
                sB.addIngredient(VanillaTypes.ITEM_STACK, output);
                i++; x+=16;
                if(!scrollGridFactory.getArea().containsPoint(x + 16, y)){
                    x-=i*16; i = 0; y+=16;
                }
            }
            else
                builder.addSlotToWidget(RecipeIngredientRole.OUTPUT, scrollGridFactory)
                        .addIngredient(VanillaTypes.ITEM_STACK, output);
        }
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}
