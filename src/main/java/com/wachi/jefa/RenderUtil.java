package com.wachi.jefa;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wachi.jefa.mixins.GuiGraphicsAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

public class RenderUtil {
    public static void renderBlockInGui(GuiGraphics gg, BlockState state, int x, int y, int size) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher brd = mc.getBlockRenderer();

        PoseStack pose = gg.pose();
        pose.pushPose();
        pose.translate(x, y, 200.0F);

        pose.scale(size, size, size);

        pose.mulPose(new Quaternionf().rotateX((float) Math.toRadians(180)));

        pose.mulPose(new Quaternionf().rotateY((float) Math.toRadians(45)));
        pose.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-15)));
        pose.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-15)));

        brd.renderSingleBlock(state, pose, ((GuiGraphicsAccessorMixin)gg).getBufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, (RenderType)null);
        gg.flush();
        pose.popPose();
    }
}
