package net.redct.client.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.utils.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityDebugModule extends Module {
    private final Set<UUID> logged = new HashSet<>();

    public EntityDebugModule() {
        super("entityDebug", "Entity Debug", Category.DEBUG);
    }

    @Override
    public void onDisable() {
        logged.clear();
    }

    // TODO: ClientEntityEvents.ENTITY_LOAD
    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        AABB box = mc.player.getBoundingBox().inflate(20.0);

        for (Entity e : mc.level.getEntities(mc.player, box)) {
            if (logged.add(e.getUUID())) {
                /*
                e.getId()
                Armor stand tiene +1 de su mob correspondiente, creo
                 */
                float health = -1;
                float maxHealth = -1;
                if (e instanceof LivingEntity living) {
                    health = living.getHealth();
                    maxHealth = living.getMaxHealth();

                }
                //if (!EntityUtils.isMob(e.getName().getString())) return;

                //GlowRegistry.setGlowing(e, true);
                Logger.log("ENTITY", "type=%s | name=%s | customName=%s | team=%s | id=%s | pos=[%.1f,%.1f,%.1f]",
                        e.getType().toShortString(),
                        e.getName().getString(),
                        e.getCustomName() != null ? e.getCustomName().getString() : "null",
                        e.getTeam()!= null ? e.getTeam().getNameTagVisibility() : "null",
                        e.getId(),
                        e.getX(), e.getY(), e.getZ()
                );


            }
        }
    }
}