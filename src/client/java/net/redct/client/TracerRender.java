package net.redct.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

/**
 * Draws a line from the camera to a target world coordinate.
 * <p>
 * Registered on {@link LevelRenderEvents#END_MAIN} rather than
 * AFTER_TRANSLUCENT_TERRAIN — END_MAIN is the event confirmed (via a real,
 * currently-working mod using this same API) to work reliably with
 * {@code context.bufferSource()}, and it requires an explicit
 * {@code endBatch()} call, which we do below.
 */
public class TracerRender {

    // ------------------------------------------------------------------
    // Public target API - call TracerRenderer.setTarget(...) from anywhere
    // (a command, a key bind, a config screen, etc.) to move the line.
    // Passing null hides the line.
    // ------------------------------------------------------------------

    private static volatile Vec3 target;

    public static void setTarget(Vec3 pos) {
        target = pos;
    }

    public static Vec3 getTarget() {
        return target;
    }

    // ------------------------------------------------------------------
    // Extracted render state - must be immutable + cheap to build, since it
    // crosses from the extraction phase into the drawing phase.
    // ------------------------------------------------------------------

    private record TracerRenderState(Vec3 targetPos) {
    }

    private static volatile TracerRenderState renderState;

    public void register() {
        LevelRenderEvents.END_EXTRACTION.register(this::extract);
        LevelRenderEvents.END_MAIN.register(this::render);
    }

    // ------------------------------------------------------------------
    // Extraction phase - just freeze the data, nothing GPU-related happens
    // here.
    // ------------------------------------------------------------------

    private void extract(LevelExtractionContext context) {
        Vec3 currentTarget = target;
        renderState = currentTarget == null ? null : new TracerRenderState(currentTarget);
    }

    // ------------------------------------------------------------------
    // Drawing phase
    // ------------------------------------------------------------------

    private void render(LevelRenderContext context) {
        TracerRenderState state = renderState;
        if (state == null) {
            return;
        }

        Vec3 camera = context.gameRenderer().getMainCamera().position();
        Vec3 targetPos = state.targetPos();

        Vec3 toTarget = targetPos.subtract(camera);
        double len = toTarget.length();
        if (len < 1e-5) {
            return;
        }
        Vec3 dir = toTarget.scale(1.0 / len);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        MultiBufferSource.BufferSource bufferSource = context.bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderTypes.LINES);

        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();

        float r = 1f, g = 0.15f, b = 0.15f, a = 1f;
        float lineWidth = 3f;

        lines.addVertex(matrix, (float) camera.x, (float) camera.y, (float) camera.z)
                .setColor(r, g, b, a)
                .setNormal(pose, (float) dir.x, (float) dir.y, (float) dir.z)
                .setLineWidth(lineWidth);

        lines.addVertex(matrix, (float) targetPos.x, (float) targetPos.y, (float) targetPos.z)
                .setColor(r, g, b, a)
                .setNormal(pose, (float) dir.x, (float) dir.y, (float) dir.z)
                .setLineWidth(lineWidth);

        bufferSource.endBatch(RenderTypes.LINES);
        System.out.println("[tracer] drew line, camera=" + camera + " target=" + targetPos);
        poseStack.popPose();
    }
}