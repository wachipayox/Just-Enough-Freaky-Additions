package com.wachi.jefa;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IScrollGridWidgetFactory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractJefaCategory<T> implements IRecipeCategory<T> {

    protected final IDrawable background;
    protected final IDrawable icon;
    protected final IScrollGridWidgetFactory<?> scrollGridFactory;

    public AbstractJefaCategory(IGuiHelper guiHelper, ItemStack iconItem, int columns, int width, int height, int visibleRows){
        background = guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "textures/gui/bg.png"), 0, 60, width, height);
        icon = guiHelper.createDrawableItemStack(iconItem);
        this.scrollGridFactory = guiHelper.createScrollGridFactory(columns, visibleRows);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

}
