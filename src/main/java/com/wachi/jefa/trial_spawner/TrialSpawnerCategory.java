package com.wachi.jefa.trial_spawner;

import com.wachi.jefa.*;
import com.wachi.jefa.piglin.PiglinTrade;
import com.wachi.jefa.vault.VaultLoot;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TrialSpawnerCategory extends AbstractJefaCategory<TrialSpawnerLoot> {

    public static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "trial_spawner_loot");

    public static final RecipeType<TrialSpawnerLoot> recipeType = new RecipeType<>(id,TrialSpawnerLoot.class);

    public TrialSpawnerCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.TRIAL_SPAWNER.getDefaultInstance(), 6, 200, 84, 4);
    }

    @Override
    public RecipeType<TrialSpawnerLoot> getRecipeType() {
        return recipeType;
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
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.trial_spawner_loot");
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
                                    potion.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, f);
                                    add(potion);}}});

        int x = getGridX(), y = getGridY(), i = 0;

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                recipe.ominous()
                        ? JefaLootTables.TRIAL_SPAWNER_OMINOUS.location()
                        : JefaLootTables.TRIAL_SPAWNER.location()
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
    public void draw(TrialSpawnerLoot recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        BlockState bS = Blocks.TRIAL_SPAWNER.defaultBlockState();
        bS = bS.setValue(TrialSpawnerBlock.OMINOUS, recipe.ominous());

        RenderUtil.renderBlockInGui(guiGraphics, bS, 12, 70, 35);
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull TrialSpawnerLoot recipe) {
        return recipe.ominous()
                ? super.getRegistryName(recipe).withSuffix("_ominous")
                : super.getRegistryName(recipe);
    }
}
