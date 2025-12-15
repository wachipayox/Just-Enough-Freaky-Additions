package com.wachi.jefa.hero_of_the_village;

import com.wachi.jefa.AbstractJefaCategory;
import com.wachi.jefa.JEFA;
import com.wachi.jefa.LootEntryPreviewBuilder;
import com.wachi.jefa.mob_interaction.MobInteractionCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.gui.elements.DrawableBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeroLootCategory extends AbstractJefaCategory<HeroLootRecipe> {

    public static final RecipeType<HeroLootRecipe> recipeType = RecipeType.create(JEFA.MODID, "hero_loot", HeroLootRecipe.class);
    protected final IDrawable icon2;

    public HeroLootCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.EMERALD.getDefaultInstance(), 8, 200, 24, 1);

        icon2 = new DrawableBuilder(
                ResourceLocation.parse(
                        "textures/mob_effect/hero_of_the_village.png"),
                0,  0, 18, 18).setTextureSize(18, 18).build();
    }

    @Override
    public int getGridX() {
        return 36;
    }

    @Override
    public int getGridY() {
        return 4;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon2;
    }

    @Override
    public RecipeType<HeroLootRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.hero_loot");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HeroLootRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 9).addIngredients(
                VanillaTypes.ITEM_STACK,
                recipe.workSite().matchingStates().stream().map(bs -> bs.getBlock().asItem().getDefaultInstance()).toList()
        );

        for (ItemStack itemStack : LootEntryPreviewBuilder.buildPreviewsForLootTable(
                recipe.giftsTable().location()
        ).stream().map(LootEntryPreviewBuilder.PreviewResult::stack).toList()) {
            builder.addOutputSlot().addItemStack(itemStack);
        }

    }

    @Override
    public void draw(HeroLootRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var villager = new Villager(EntityType.VILLAGER, mc.level);
        villager.setVillagerData(new VillagerData(VillagerType.PLAINS, recipe.profession(), 0));
        MobInteractionCategory.renderEntity(guiGraphics, villager, 10, 24, 12, mouseX, mouseY);
    }

}
