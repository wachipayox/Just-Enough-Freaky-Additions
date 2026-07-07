package com.wachi.jefa;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractJefaCategory<T> implements IRecipeCategory<T> {

    protected final int width, height, rows, columns;

    protected final IDrawable background;
    protected final IDrawable icon;

    public AbstractJefaCategory(IGuiHelper guiHelper, ItemStack iconItem, int columns, int width, int height, int visibleRows){
        background = guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "textures/gui/bg.png"), 0, 60, width, height);
        icon = guiHelper.createDrawableItemStack(iconItem);
        this.width = width; this.height = height; this.rows = visibleRows; this.columns = columns;
    }

    public abstract ResourceLocation getID();

    public abstract int getGridX();

    public abstract int getGridY();

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull T recipe) {
        return getID().withSuffix("_main");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        IRecipeCategory.super.createRecipeExtras(builder, recipe, focuses);

        IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
        if (recipeSlots == null) return;

        List<IRecipeSlotDrawable> outputSlots = recipeSlots.getSlots(RecipeIngredientRole.OUTPUT);
        IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(outputSlots, columns, rows);
        scrollGridWidget.setPosition(getGridX(), getGridY());
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }

}
