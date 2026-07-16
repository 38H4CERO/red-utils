package net.redct.client.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.redct.client.RedUtilsClient;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

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
            return (partialTick) -> point.add(0.5, 0, 0.5);
        }

        /**
         * The player's camera position (camera origin + forward offset,
         */
        static Anchor player() {
            return (partialTick) -> {
                Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
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

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    public void renderAndDrawLines(LevelRenderContext context) {
        if (lines.isEmpty()) return;

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        // 1. Shift the matrix to true world origin
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (this.buffer == null) {
            this.buffer = new BufferBuilder(ALLOCATOR, TRACER.getVertexFormatMode(), TRACER.getVertexFormat());
        }

        Matrix4fc positionMatrix = matrices.last().pose();


        // 2. Loop through all lines
        for (Line line : lines) {
            this.renderLine(positionMatrix, this.buffer, line.source(), line.target(), line.width(), line.argb());
        }

        matrices.popPose();

        drawTracer(Minecraft.getInstance(), TRACER);

    }


    private void renderLine(Matrix4fc positionMatrix, BufferBuilder buffer, Vec3 source, Vec3 target, float width, int color) {
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

    private void drawTracer(Minecraft client, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        // Build the buffer
        MeshData builtBuffer = this.buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = this.upload(drawParameters, format, builtBuffer);

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        // Rotate the vertex buffer so we are less likely to use buffers that the GPU is using
        this.vertexBuffer.rotate();
        this.buffer = null;
    }

    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (this.vertexBuffer == null || this.vertexBuffer.size() < vertexBufferSize) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.close();
            }

            this.vertexBuffer = new MappableRingBuffer(() -> RedUtilsClient.MOD_ID + " example render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            // Upload the index buffer
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> RedUtilsClient.MOD_ID + " example render pipeline rendering", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    private static Vec3 CamDelta(Camera cam) {
        Vector3fc forward = cam.forwardVector();
        return new Vec3(forward.x(), forward.y(), forward.z());
    }

    public void close() {
        ALLOCATOR.close();

        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
            this.vertexBuffer = null;
        }
    }

}
