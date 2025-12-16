package com.wachi.jefa.fishing;

import com.wachi.jefa.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;

public class FishingCategory extends AbstractJefaCategory<FishingLoot> {

    public static final IRecipeType<FishingLoot> recipeType = IRecipeType.create(JEFA.MODID, "fishing_loot", FishingLoot.class);

    public FishingCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.FISHING_ROD.getDefaultInstance(), 10, 200, 62, 3);
    }

    @Override
    public IRecipeType<FishingLoot> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.fishing_loot");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishingLoot recipe, IFocusGroup focuses) {
        for (ItemStack itemStack : LootEntryPreviewBuilder.buildPreviewsForLootTable(
                BuiltInLootTables.FISHING.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList()) {
            builder.addOutputSlot().add(itemStack);
        }
    }

    @Override
    public int getGridX() {
        return 3;
    }

    @Override
    public int getGridY() {
        return 4;
    }
}
