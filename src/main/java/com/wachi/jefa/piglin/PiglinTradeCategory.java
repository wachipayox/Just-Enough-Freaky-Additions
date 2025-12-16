package com.wachi.jefa.piglin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.wachi.jefa.AbstractJefaCategory;
import com.wachi.jefa.JEFA;
import com.wachi.jefa.LootEntryPreviewBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class PiglinTradeCategory extends AbstractJefaCategory<PiglinTrade> {

    public static final RecipeType<PiglinTrade> recipeType = RecipeType.create(JEFA.MODID, "piglin_trade", PiglinTrade.class);

    public PiglinTradeCategory(IGuiHelper guiHelper){
        super(guiHelper, Items.PIGLIN_HEAD.getDefaultInstance(), 6, 200, 100, 5);
    }

    @Override
    public RecipeType<PiglinTrade> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.piglin_trade");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PiglinTrade recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 13).addIngredient(
                VanillaTypes.ITEM_STACK,
                Items.GOLD_INGOT.getDefaultInstance()
        );

        for (ItemStack itemStack : LootEntryPreviewBuilder.buildPreviewsForLootTable(BuiltInLootTables.PIGLIN_BARTERING.location())
                .stream().map(LootEntryPreviewBuilder.PreviewResult::stack)
                .toList()) {
            builder.addOutputSlot().addItemStack(itemStack);
        }

    }

    public static Piglin globalPiglin = null;
    public static Level lastLevel = null;

    private static Piglin generatePiglin(Level level){
        Piglin piglin = new Piglin(EntityType.PIGLIN, level);
        piglin.setBaby(false);
        piglin.setAggressive(false);
        piglin.setImmuneToZombification(true);

        piglin.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GOLD_INGOT));

        float bodyYaw  = -25.0F;
        float headYaw  = bodyYaw - 60F;
        float headPitch = 25.0F;

        piglin.yBodyRot  = bodyYaw;
        piglin.yBodyRotO = bodyYaw;

        piglin.yHeadRot  = headYaw;
        piglin.yHeadRotO = headYaw;

        piglin.setXRot(headPitch);
        piglin.xRotO = headPitch;

        return piglin;
    }

    @Override
    public void draw(PiglinTrade recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        var level = Minecraft.getInstance().level;
        if(level == null) return;
        else if(lastLevel == null || lastLevel != level || globalPiglin == null){
            globalPiglin = generatePiglin(level);
            lastLevel = level;
        }

        Minecraft mc = Minecraft.getInstance();

        int x = 45;
        int y = 90;
        float scale = 35.0F;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(x, y, 50);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(new Quaternionf().rotateY((float)Math.toRadians(180.0)));
        dispatcher.setRenderShadow(false);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        dispatcher.render(globalPiglin, 0.0, 0.0, 0.0, 0.0F, 0, poseStack, bufferSource, 0x00F000F0);
        bufferSource.endBatch();
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }
}
