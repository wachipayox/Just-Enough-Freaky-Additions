package com.wachi.jefa.vault;

import com.wachi.jefa.AbstractJefaCategory;
import com.wachi.jefa.JEFA;
import com.wachi.jefa.LootEntryPreviewBuilder;
import com.wachi.jefa.RenderUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VaultOpeningCategory extends AbstractJefaCategory<VaultLoot> {

    public static final RecipeType<VaultLoot> recipeType = RecipeType.create(JEFA.MODID, "vault_loot", VaultLoot.class);

    public VaultOpeningCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.VAULT.getDefaultInstance(), 6, 200, 84, 4);
    }

    @Override
    public RecipeType<VaultLoot> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.vault_loot");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VaultLoot recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 13).addIngredient(
                VanillaTypes.ITEM_STACK,
                recipe.ominous()
                        ? Items.OMINOUS_TRIAL_KEY.getDefaultInstance()
                        : Items.TRIAL_KEY.getDefaultInstance()
        );
        scrollGridFactory.setPosition(73, 5);

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                recipe.ominous()
                        ? BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS.location()
                        : BuiltInLootTables.TRIAL_CHAMBERS_REWARD.location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList();
        for (ItemStack output : outputs) {
            builder.addSlotToWidget(RecipeIngredientRole.OUTPUT, scrollGridFactory)
                    .addIngredient(VanillaTypes.ITEM_STACK, output);
        }
    }

    @Override
    public void draw(VaultLoot recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        BlockState bS = Blocks.VAULT.defaultBlockState();
        bS = bS.setValue(VaultBlock.OMINOUS, recipe.ominous());

        RenderUtil.renderBlockInGui(guiGraphics, bS, 12, 70, 35);
    }
}
