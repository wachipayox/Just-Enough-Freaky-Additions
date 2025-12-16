package com.wachi.jefa.mob_interaction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.wachi.jefa.JEFA;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.gui.elements.DrawableBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;


public class MobInteractionCategory implements IRecipeCategory<MobInteractionRecipe> {

    public static final RecipeType<MobInteractionRecipe> recipeType = RecipeType.create(JEFA.MODID, "mob_interaction", com.wachi.jefa.mob_interaction.MobInteractionRecipe.class);

    protected final IDrawable background;
    protected final IDrawable icon;

    public MobInteractionCategory(){
        background = new DrawableBuilder(ResourceLocation.fromNamespaceAndPath(
                JEFA.MODID, "textures/gui/mob_int_bg.png"
        ), 0, 0, 100, 24).setTextureSize(100, 24).build();
        icon = new DrawableBuilder(ResourceLocation.fromNamespaceAndPath(
                JEFA.MODID, "textures/gui/mob_int.png"
        ), 0, 0, 16, 16).setTextureSize(16, 16).build();
    }

    @Override
    public RecipeType<MobInteractionRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jefa.category.mob_interaction");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MobInteractionRecipe recipe, IFocusGroup focuses) {
        if(recipe.mobIn() != null)
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.mobIn().apply(Minecraft.getInstance().level).second);
        else if(recipe.itemIn() != null)
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 4)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.itemIn());

        if(recipe.mobMid() != null)
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.mobMid().apply(Minecraft.getInstance().level).second);

        if(recipe.itemOut() != null)
            builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 4)
                    .addIngredient(VanillaTypes.ITEM_STACK, recipe.itemOut());
    }

    //Storaging entities so if there is a resource pack like fresh animations the animations can set up
    public static Map<MobInteractionRecipe, Entity> inMobs = new HashMap<>();
    public static Map<MobInteractionRecipe, Entity> midMobs = new HashMap<>();
    public static Level lastLevel = null;

    @Override
    public void draw(MobInteractionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        var level = Minecraft.getInstance().level;
        if(level == null) return;
        else if(lastLevel == null || lastLevel != level){
            inMobs.clear();
            midMobs.clear();
            lastLevel = level;
        }

        if(recipe.mobIn() != null) {
            var mobIn = inMobs.computeIfAbsent(recipe, k -> recipe.mobIn().apply(level).first);
            renderEntity(guiGraphics, mobIn, 18, 18, (float) (12 / Math.max(mobIn.getBoundingBox().getSize(), 0.55)), mouseX, mouseY);
        }if(recipe.mobMid() != null) {
            var mobMid = midMobs.computeIfAbsent(recipe, k -> recipe.mobMid().apply(level).first);
            renderEntity(guiGraphics, mobMid, 50, 18, (float) (12 / Math.max(mobMid.getBoundingBox().getSize(), 0.55)), mouseX, mouseY);
        }
    }

    public static void renderEntity(GuiGraphics guiGraphics, Entity entity, int x, int y, float scale, double mouseX, double mouseY){
        var mc = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(x, y, 50);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
        poseStack.mulPose(Axis.YN.rotationDegrees(-20.0F));

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(new Quaternionf().rotateY((float)Math.toRadians(180.0)));
        dispatcher.setRenderShadow(false);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 0, poseStack, bufferSource, 0x00F000F0);
        bufferSource.endBatch();
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }

}
