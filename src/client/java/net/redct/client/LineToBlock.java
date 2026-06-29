package net.redct.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class LineToBlock {

    private static final double TARGET_X = 8.5;
    private static final double TARGET_Y = -60.5;
    private static final double TARGET_Z = 8.5;

    // No setOutputTarget — defaults to main framebuffer instead of ITEM_ENTITY_TARGET
    private static final RenderType TRACER_LINE = RenderType.create(
            "tracer_line",
            RenderSetup.builder(RenderPipelines.LINES)
                    .createRenderSetup()
    );

    public static void render(LevelRenderContext context) {
        Vec3 cameraPos = context.levelState().cameraRenderState.pos;

        float dx = (float)(TARGET_X - cameraPos.x);
        float dy = (float)(TARGET_Y - cameraPos.y);
        float dz = (float)(TARGET_Z - cameraPos.z);

        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0f) return;
        float nx = dx / len;
        float ny = dy / len;
        float nz = dz / len;

        PoseStack poseStack = context.poseStack();
        MultiBufferSource.BufferSource bufferSource = context.bufferSource();

        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();

        VertexConsumer lines = bufferSource.getBuffer(TRACER_LINE);

        lines.addVertex(mat, 0f, 0f, 0f)
                .setColor(255, 0, 0, 255)
                .setNormal(nx, ny, nz)
                .setLineWidth(2f);

        lines.addVertex(mat, dx, dy, dz)
                .setColor(255, 0, 0, 255)
                .setNormal(nx, ny, nz)
                .setLineWidth(2f);

        bufferSource.endBatch(TRACER_LINE);

        poseStack.popPose();
    }
}