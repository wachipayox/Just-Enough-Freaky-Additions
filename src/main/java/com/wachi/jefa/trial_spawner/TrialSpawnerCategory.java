package com.wachi.jefa.trial_spawner;

import com.wachi.jefa.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TrialSpawnerCategory extends AbstractJefaCategory<TrialSpawnerLoot> {

    public static final IRecipeType<TrialSpawnerLoot> recipeType = IRecipeType.create(JEFA.MODID, "trial_spawner_loot", TrialSpawnerLoot.class);

    public TrialSpawnerCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.TRIAL_SPAWNER.getDefaultInstance(), 6, 200, 84, 4);
    }

    @Override
    public IRecipeType<TrialSpawnerLoot> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.trial_spawner_loot");
    }

    @Override
    public int getGridX() {
        return 73;
    }

    @Override
    public int getGridY() {
        return 5;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TrialSpawnerLoot recipe, IFocusGroup focuses) {
        if(recipe.ominous())
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 13)
                    .addIngredients(
                            VanillaTypes.ITEM_STACK,
                            new ArrayList<>() {{
                                for (int f = 0; f < 5; f++) {
                                    var potion = Items.OMINOUS_BOTTLE.getDefaultInstance();
                                    potion.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(f));
                                    add(potion);}}});

        for (ItemStack itemStack : LootEntryPreviewBuilder.buildPreviewsForLootTable(
                recipe.ominous()
                        ? JefaLootTables.TRIAL_SPAWNER_OMINOUS.location()
                        : JefaLootTables.TRIAL_SPAWNER.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList()) {
            builder.addOutputSlot().add(itemStack);
        }
    }

    @Override
    public void draw(TrialSpawnerLoot recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        BlockState bS = Blocks.TRIAL_SPAWNER.defaultBlockState();
        bS = bS.setValue(TrialSpawnerBlock.OMINOUS, recipe.ominous());

        RenderUtil.renderBlockInGui(guiGraphics, bS, 12, 70, 35);
    }
}
