package com.wachi.jefa;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractJefaCategory<T> implements IRecipeCategory<T> {

    protected final IDrawable icon;
    final int width, height, rows, columns;

    public AbstractJefaCategory(IGuiHelper guiHelper, ItemStack iconItem, int columns, int width, int height, int visibleRows){
        icon = guiHelper.createDrawableItemStack(iconItem);
        this.width = width; this.height = height; this.rows = visibleRows; this.columns = columns;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public abstract int getGridX();

    public abstract int getGridY();

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        IRecipeCategory.super.createRecipeExtras(builder, recipe, focuses);

        IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
        List<IRecipeSlotDrawable> outputSlots = recipeSlots.getSlots(RecipeIngredientRole.OUTPUT);

        IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(outputSlots, columns, rows);
        scrollGridWidget.setPosition(getGridX(), getGridY());
    }
}
