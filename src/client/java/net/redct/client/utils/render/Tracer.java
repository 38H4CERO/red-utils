package net.redct.client.utils.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.redct.client.RedUtilsClient;
import org.joml.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Tracer {
    private static Tracer instance;

    private static final RenderPipeline TRACER = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(RedUtilsClient.MOD_ID, "pipeline/lines"))
            .withDepthStencilState(Optional.empty())
            .build()
    );

    private static final List<Line> lines = new ArrayList<>();

    private static final Map<String, AnchoredLine> anchoredLines = new ConcurrentHashMap<>();

    public interface Anchor {
        /** Current world position. */
        Vec3 resolve(float partialTick);

        /** A point that never moves. */
        static Anchor fixed(Vec3 point) {
            return (partialTick) -> point.add(0, 0, 0);
        }

        /**
         * The player's camera position (camera origin + forward offset,
         */
        static Anchor player() {
            return (partialTick) -> {
                Camera cam = Minecraft.getInstance().gameRenderer.mainCamera();
                return cam.position().add(CamDelta(cam));
            };
        }

        /**
         * A moving entity's position, recomputed every frame.
         *  Tracer does NOT check whether the entity is still alive/loaded
         */
        static Anchor entity(Entity entity) {
            //return entity::position
            return (partialTick) -> entity.getPosition(partialTick).add(0, entity.getEyeHeight(), 0);
        }
    }



    public static String setLine(Anchor source, Anchor target, float width, int argb) {
        return setLine(UUID.randomUUID().toString(), source, target, width, argb);
    }


    public static String setLine(String id ,Anchor source, Anchor target, float width, int argb) {
        anchoredLines.put(id, new AnchoredLine(source, target, width, argb));
        return id;
    }

    public static void removeLine(String id) {
        anchoredLines.remove(id);
    }

    public static void clearLines() {
        anchoredLines.clear();
    }


    public static Tracer getInstance() {
        if (instance == null) instance = new Tracer();
        return instance;
    }

    // Access data from the world or anything here in the extraction phase.
    // You can only access the (immutable and thread safe) render state in the drawing phase.
    public void extractLine(LevelExtractionContext context) {
        lines.clear();
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        for (AnchoredLine line : anchoredLines.values()) {
            lines.add(new Line(line.source().resolve(partialTick), line.target().resolve(partialTick), line.width(), line.argb()));
        }
    }

    // Render states should be immutable, thread safe, and fast to create.
    private record Line(Vec3 source, Vec3 target, float width, int argb) { }
    private record AnchoredLine(Anchor source, Anchor target, float width, int argb) { }

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private StagedVertexBuffer stagedBuffer;

    public void renderAndDrawLines(LevelRenderContext context) {
        if (lines.isEmpty()) return;

        if (this.stagedBuffer == null) {
            this.stagedBuffer = new StagedVertexBuffer(() -> RedUtilsClient.MOD_ID + " tracer buffer", RenderType.SMALL_BUFFER_SIZE);
        }

        VertexFormat formatBinding = TRACER.getVertexFormatBinding(0);
        if (formatBinding == null) return;

        PrimitiveTopology topology = TRACER.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = this.stagedBuffer.appendDraw(
                formatBinding,
                topology,
                topology == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null
        );

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        // 1. Shift the matrix to true world origin
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Matrix4fc positionMatrix = matrices.last().pose();
        VertexConsumer builder = this.stagedBuffer.getVertexBuilder(draw);

        // 2. Loop through all lines
        for (Line line : lines) {
            this.renderLine(positionMatrix, builder, line.source(), line.target(), line.width(), line.argb());
        }

        matrices.popPose();

        // 3. Upload and draw
        this.stagedBuffer.upload();

        StagedVertexBuffer.ExecuteInfo info = this.stagedBuffer.getExecuteInfo(draw);
        if (info != null) {
            drawTracer(Minecraft.getInstance(), info, TRACER);
        }

        this.stagedBuffer.endFrame();
    }


    private void renderLine(Matrix4fc positionMatrix, VertexConsumer buffer, Vec3 source, Vec3 target, float width, int color) {
        // Calculate normal for line thickness orientation
        float dx = (float) (target.x() - source.x());
        float dy = (float) (target.y() - source.y());
        float dz = (float) (target.z() - source.z());
        Vector3f normal = new Vector3f(dx, dy, dz).normalize();

        buffer.addVertex(positionMatrix, (float) source.x(), (float) source.y(), (float) source.z())
                .setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color))
                .setNormal(normal.x(), normal.y(), normal.z())
                .setLineWidth(width);

        buffer.addVertex(positionMatrix, (float) target.x(), (float) target.y(), (float) target.z())
                .setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color))
                .setNormal(normal.x(), normal.y(), normal.z())
                .setLineWidth(width);

    }

    private static void drawTracer(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();
        if (colorTexture == null) return;

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> RedUtilsClient.MOD_ID + " tracer render pipeline rendering", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    private static Vec3 CamDelta(Camera cam) {
        Vector3fc forward = cam.forwardVector();
        return new Vec3(forward.x(), forward.y(), forward.z());
    }

    public void close() {
        if (this.stagedBuffer != null) {
            this.stagedBuffer.close();
            this.stagedBuffer = null;
        }
    }

}