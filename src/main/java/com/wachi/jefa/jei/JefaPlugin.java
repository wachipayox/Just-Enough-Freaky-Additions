package com.wachi.jefa.jei;

import com.wachi.jefa.JEFA;
import com.wachi.jefa.cat_gift.CatGift;
import com.wachi.jefa.cat_gift.CatGiftCategory;
import com.wachi.jefa.fishing.FishingCategory;
import com.wachi.jefa.fishing.FishingLoot;
import com.wachi.jefa.hero_of_the_village.HeroLootCategory;
import com.wachi.jefa.hero_of_the_village.HeroLootRecipe;
import com.wachi.jefa.mob_interaction.MobInteractionCategory;
import com.wachi.jefa.mob_interaction.MobInteractionRecipe;
import com.wachi.jefa.piglin.PiglinTrade;
import com.wachi.jefa.piglin.PiglinTradeCategory;
import com.wachi.jefa.trial_spawner.TrialSpawnerCategory;
import com.wachi.jefa.trial_spawner.TrialSpawnerLoot;
import com.wachi.jefa.vault.VaultLoot;
import com.wachi.jefa.vault.VaultOpeningCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JefaPlugin implements IModPlugin {

    public JefaPlugin(){}

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(JEFA.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new PiglinTradeCategory(guiHelper),
                new VaultOpeningCategory(guiHelper),
                new TrialSpawnerCategory(guiHelper),
                new FishingCategory(guiHelper),
                new CatGiftCategory(guiHelper),
                new MobInteractionCategory(),
                new HeroLootCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PiglinTradeCategory.recipeType, List.of(new PiglinTrade()));
        registration.addRecipes(VaultOpeningCategory.recipeType, List.of(new VaultLoot(false), new VaultLoot(true)));
        registration.addRecipes(TrialSpawnerCategory.recipeType, List.of(new TrialSpawnerLoot(false), new TrialSpawnerLoot(true)));
        registration.addRecipes(FishingCategory.recipeType, List.of(new FishingLoot()));
        registration.addRecipes(CatGiftCategory.recipeType, List.of(new CatGift()));
        registration.addRecipes(MobInteractionCategory.recipeType, MobInteractionRecipe.recipes);
        registration.addRecipes(HeroLootCategory.recipeType, HeroLootRecipe.recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(PiglinTradeCategory.recipeType, Items.PIGLIN_SPAWN_EGG.getDefaultInstance());
        registration.addCraftingStation(VaultOpeningCategory.recipeType, Items.VAULT.getDefaultInstance());
        registration.addCraftingStation(TrialSpawnerCategory.recipeType, Items.TRIAL_SPAWNER.getDefaultInstance());
        registration.addCraftingStation(FishingCategory.recipeType, Items.FISHING_ROD.getDefaultInstance());
        registration.addCraftingStation(CatGiftCategory.recipeType, Items.CAT_SPAWN_EGG.getDefaultInstance());
        registration.addCraftingStation(HeroLootCategory.recipeType, Items.VILLAGER_SPAWN_EGG.getDefaultInstance());
    }


}
