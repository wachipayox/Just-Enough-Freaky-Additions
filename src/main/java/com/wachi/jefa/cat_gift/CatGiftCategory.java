package com.wachi.jefa.cat_gift;

import com.wachi.jefa.AbstractJefaCategory;
import com.wachi.jefa.JEFA;
import com.wachi.jefa.LootEntryPreviewBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.gui.elements.DrawableBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CatGiftCategory extends AbstractJefaCategory<CatGift> {

    public static final RecipeType<CatGift> recipeType = RecipeType.create(JEFA.MODID, "cat_gift", CatGift.class);

    final IDrawable icon2;

    public CatGiftCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.CAT_SPAWN_EGG.getDefaultInstance(), 10, 200, 46, 2);
        icon2 = new DrawableBuilder(ResourceLocation.fromNamespaceAndPath(
                JEFA.MODID, "textures/gui/cat_gift.png"
        ), 0, 0, 16, 16).setTextureSize(16, 16).build();
    }

    @Override
    public RecipeType<CatGift> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon2;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.cat_gift");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CatGift recipe, IFocusGroup focuses) {
        scrollGridFactory.setPosition(3, 4);

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                BuiltInLootTables.CAT_MORNING_GIFT.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList();
        for (ItemStack output : outputs) {
            builder.addSlotToWidget(RecipeIngredientRole.OUTPUT, scrollGridFactory)
                    .addIngredient(VanillaTypes.ITEM_STACK, output);
        }
    }
}
