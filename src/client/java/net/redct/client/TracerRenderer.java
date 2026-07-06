package net.redct.client;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;


/**
 * Draws a single line from the camera to a target world coordinate.
 * <p>
 * The line starts exactly at the camera position. Since the camera is the
 * projection origin, that point always renders at the center of the screen -
 * which is what gives the "tracer from the crosshair" look, without needing
 * any separate 2D/HUD drawing.
 * <p>
 * Two phases, per https://docs.fabricmc.net/develop/rendering/world :
 * - "extraction": snapshot what we want to draw into an immutable state
 * - "drawing": upload that state to the GPU and draw it
 */
public class TracerRenderer {

    // ------------------------------------------------------------------
    // Render pipeline
    // ------------------------------------------------------------------
    //
    // RenderPipelines.DEBUG_LINE_SNIPPET is the line-drawing equivalent of
    // the RenderPipelines.DEBUG_FILLED_SNIPPET used for the "waypoint
    // through walls" example in the Fabric docs. If this exact constant
    // name has moved in your copy of 26.1.x, run `./gradlew genSources`,
    // open RenderPipelines in your IDE and autocomplete "DEBUG_LINE" - drop
    // whichever *_SNIPPET constant you get in its place. Nothing else in
    // this file depends on the exact name.
    private static final RenderPipeline TRACER_LINE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(RedUtilsClient.MOD_ID, "pipeline/tracer_line"))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
                    // Empty depth/stencil state = no depth test = the line draws
                    // through walls/terrain, like a classic "tracer". Delete this
                    // line (and the .withDepthStencilState import usage) if you'd
                    // rather the line get hidden behind blocks normally.
                    .withDepthStencilState(Optional.empty())
                    .build()
    );

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

    // ------------------------------------------------------------------
    // GPU-side resources
    // ------------------------------------------------------------------

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    private static TracerRenderer instance;

    public static TracerRenderer getInstance() {
        return instance;
    }

    public void register() {
        instance = this;
        LevelRenderEvents.END_EXTRACTION.register(this::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::renderAndDraw);
    }

    // ------------------------------------------------------------------
    // Extraction phase
    // ------------------------------------------------------------------

    private void extract(LevelExtractionContext context) {
        Vec3 currentTarget = target;
        renderState = currentTarget == null ? null : new TracerRenderState(currentTarget);
    }

    // ------------------------------------------------------------------
    // Drawing phase
    // ------------------------------------------------------------------

    private void renderAndDraw(LevelRenderContext context) {
        TracerRenderState state = renderState;

        if (state == null) {
            return;
        }

        this.buildLine(context, state);
        this.draw(Minecraft.getInstance());
    }

    private void buildLine(LevelRenderContext context, TracerRenderState state) {
        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (this.buffer == null) {
            this.buffer = new BufferBuilder(ALLOCATOR, TRACER_LINE_PIPELINE.getVertexFormatMode(), TRACER_LINE_PIPELINE.getVertexFormat());
        }

        Matrix4fc positionMatrix = matrices.last().pose();
        float r = 1f, g = 0.15f, b = 0.15f, a = 1f;

        // Vertex 1: the camera position itself. After the translate above,
        // this lands exactly at (0,0,0) in view space - the screen center.
        this.buffer.addVertex(positionMatrix, (float) camera.x, (float) camera.y, (float) camera.z)
                .setColor(r, g, b, a);

        // Vertex 2: the target world coordinate.
        this.buffer.addVertex(positionMatrix, (float) state.targetPos().x, (float) state.targetPos().y, (float) state.targetPos().z)
                .setColor(r, g, b, a);


        VertexConsumer consumer = context.bufferSource().getBuffer(RenderTypes.linesTranslucent()); // or .lines() for depth-tested

        Vector3f normal = target.subtract(camera).toVector3f().normalize();

        consumer.addVertex(positionMatrix, (float) camera.x, (float) camera.y, (float) camera.z)
                .setColor(r, g, b, a)
                .setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(1.0f);
        matrices.popPose();
    }

    private void draw(Minecraft client) {
        MeshData builtBuffer = this.buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = this.upload(drawParameters, format, builtBuffer);
        drawInternal(client, builtBuffer, drawParameters, vertices, format);

        // Rotate so we're less likely to write into a buffer the GPU is still reading.
        this.vertexBuffer.rotate();
        this.buffer = null;
    }

    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        if (this.vertexBuffer == null || this.vertexBuffer.size() < vertexBufferSize) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.close();
            }

            this.vertexBuffer = new MappableRingBuffer(() -> RedUtilsClient.MOD_ID + " tracer line", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }

    private static void drawInternal(Minecraft client, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        // A line strip is never QUADS mode, so we always go through the
        // general-purpose sequential index buffer here.
        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(TRACER_LINE_PIPELINE.getVertexFormatMode());
        GpuBuffer indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
        VertexFormat.IndexType indexType = shapeIndexBuffer.type();

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> RedUtilsClient.MOD_ID + " tracer line rendering", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(TRACER_LINE_PIPELINE);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    /**
     * Called from {@link com.example.tracermod.mixin.client.GameRendererMixin}
     * when GameRenderer#close runs, to release GPU resources.
     */
    public void close() {
        ALLOCATOR.close();

        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
            this.vertexBuffer = null;
        }
    }
}