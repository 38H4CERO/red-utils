package net.redct.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RenderUtils {

    public static final RenderType TRACER_LINE = RenderType.create(
            "tracer_line",
            RenderSetup.builder(RenderPipelines.LINES_TRANSLUCENT)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    // Note: If you want the line to render THROUGH walls (X-Ray),
                    // you will need to change LINES_TRANSLUCENT to a pipeline that has depth testing disabled.
                    .createRenderSetup()
    );

    public static void drawLine(LevelRenderContext context, Vec3 camPos, Vec3 from, Vec3 to, int argb) {
        float fx = (float)(from.x - camPos.x);
        float fy = (float)(from.y - camPos.y);
        float fz = (float)(from.z - camPos.z);
        float tx = (float)(to.x - camPos.x);
        float ty = (float)(to.y - camPos.y);
        float tz = (float)(to.z - camPos.z);

        float dx = tx - fx, dy = ty - fy, dz = tz - fz;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;

        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8)  & 0xFF;
        int b = argb         & 0xFF;

        PoseStack.Pose pose = context.poseStack().last();
        Matrix4f matrix = pose.pose();

        VertexConsumer lines = context.bufferSource().getBuffer(TRACER_LINE);
        lines.addVertex(matrix, fx, fy, fz).setColor(r, g, b, a).setNormal(pose, dx / len, dy / len, dz / len).setLineWidth(1.0f);
        lines.addVertex(matrix, tx, ty, tz).setColor(r, g, b, a).setNormal(pose, dx / len, dy / len, dz / len).setLineWidth(1.0f);
    }

    public static void drawTracerLine(LevelRenderContext context, Vec3 camPos, Vec3 target, int argb) {
        drawLine(context, camPos, camPos, target, argb);
    }

    // --- NEW METHOD ---
    // Call this at the very end of your rendering module to push the lines to the screen
    public static void flushTracerLines(LevelRenderContext context) {
        if (context.bufferSource() instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(TRACER_LINE);
        }
    }
}