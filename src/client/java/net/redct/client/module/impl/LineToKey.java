package net.redct.client.module.impl;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.redct.client.RedUtilsClient;
import net.redct.client.config.ColorSetting;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.RenderUtils;

public class LineToKey extends Module {
    public final ColorSetting color = new ColorSetting("color", "Color", 0xFF00FF00);

    private Entity target = null;

    public LineToKey(){
        super("lineToKey","Line to key", Category.DUNGEONS);
        registerSetting(color);
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (target != null && (target.isRemoved() || !target.isAlive())) {
            target = null;
        }

        if (target != null) return;

        AABB box = mc.player.getBoundingBox().inflate(50.0);
        for (Entity e : mc.level.getEntities(mc.player, box)) {
            if (isKeyEntity(e)) {
                target = e;
                break;
            }
        }
    }

    @Override
    public void onWorldRender(LevelRenderContext context) {
        // DEBUG 1: If you don't see this in the console, your module system
        // is NOT hooked up to Fabric's LevelRenderEvents!
        System.out.println("[DEBUG] onWorldRender is firing!");

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        // DEBUG 2: Let's check how far away the target is.
        // If it's 10,000 blocks away, it might be too thin to see.
        Vec3 targetPos = new Vec3(8, -60, 8);
        System.out.println("[DEBUG] Camera: " + cam + " | Distance to target: " + cam.distanceTo(targetPos));

        RenderUtils.drawTracerLine(context, cam, targetPos, color.getColor());
        RenderUtils.flushTracerLines(context);
    }

    private boolean isKeyEntity(Entity e) {
        // TODO: fill in after debug module identifies the entity
        return e.getCustomName() != null && e.getCustomName().getString().contains("Key");
    }

}
