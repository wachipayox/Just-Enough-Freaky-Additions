package com.wachi.jefa.vault;

import com.wachi.jefa.AbstractJefaCategory;
import com.wachi.jefa.JEFA;
import com.wachi.jefa.LootEntryPreviewBuilder;
import com.wachi.jefa.RenderUtil;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VaultOpeningCategory extends AbstractJefaCategory<VaultLoot> {

    public static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "vault_loot");

    public static final RecipeType<VaultLoot> recipeType = new RecipeType<>(id, VaultLoot.class);

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

        int x = 73, y = 5, i = 0;
        scrollGridFactory.setPosition(x, y);

        List<ItemStack> outputs = LootEntryPreviewBuilder.buildPreviewsForLootTable(
                recipe.ominous()
                        ? BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS.location()
                        : BuiltInLootTables.TRIAL_CHAMBERS_REWARD.location()
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
    public void draw(VaultLoot recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        BlockState bS = Blocks.VAULT.defaultBlockState();
        bS = bS.setValue(VaultBlock.OMINOUS, recipe.ominous());

        RenderUtil.renderBlockInGui(guiGraphics, bS, 12, 70, 35);
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull VaultLoot recipe) {
        return recipe.ominous()
                ? super.getRegistryName(recipe).withSuffix("_ominous")
                : super.getRegistryName(recipe);
    }
}
