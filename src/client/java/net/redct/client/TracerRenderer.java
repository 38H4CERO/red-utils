package net.redct.client;

import org.joml.Matrix4fc;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

/**
 * Draws a line from the camera to a target world coordinate.
 * <p>
 * The line starts exactly at the camera position. Since the camera is the
 * projection origin, that point always renders at the center of the screen -
 * which is what gives the "tracer from the crosshair" look, without needing
 * any separate 2D/HUD drawing.
 * <p>
 * Unlike a from-scratch custom RenderPipeline, this version uses Minecraft's
 * own vanilla line RenderType via {@code context.bufferSource()}. That means
 * no manual GpuBuffer, no CommandEncoder/RenderPass, no MappableRingBuffer,
 * and no GameRenderer#close mixin - LevelRenderer owns the buffer's whole
 * lifecycle and flushes it for us.
 * <p>
 * RenderTypes.LINES_TRANSLUCENT disables depth writing (see RenderPipelines.LINES_TRANSLUCENT),
 * so the line stays visible through walls/terrain. Swap to RenderTypes.lines()
 * if you'd rather it be occluded normally.
 */
public class TracerRenderer {

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
    // crosses from the extraction phase into the (possibly parallel) drawing
    // phase.
    // ------------------------------------------------------------------

    private record TracerRenderState(Vec3 targetPos) {
    }

    private static volatile TracerRenderState renderState;

    public void register() {
        LevelRenderEvents.END_EXTRACTION.register(this::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::render);
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

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        Vec3 targetPos = state.targetPos();

        System.out.println("[tracer] camera=" + camera + " target=" + targetPos);

        Matrix4fc positionMatrix = matrices.last().pose();   // no pushPose/translate anymore

        VertexConsumer consumer = context.bufferSource().getBuffer(RenderTypes.linesTranslucent());

        Vec3 relativeCamera = Vec3.ZERO;
        Vec3 relativeTarget = targetPos.subtract(camera);

        System.out.println("[tracer] relativeTarget=" + relativeTarget);

        Vec3 direction = relativeTarget.normalize();
        Vector3f normal = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);

        float r = 1f, g = 0.15f, b = 0.15f, a = 1f;
        float lineWidth = 200f;

        consumer.addVertex(positionMatrix, (float) relativeCamera.x, (float) relativeCamera.y, (float) relativeCamera.z)
                .setColor(r, g, b, a)
                .setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(lineWidth);

        consumer.addVertex(positionMatrix, (float) relativeTarget.x, (float) relativeTarget.y, (float) relativeTarget.z)
                .setColor(r, g, b, a)
                .setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(lineWidth);
    }
}