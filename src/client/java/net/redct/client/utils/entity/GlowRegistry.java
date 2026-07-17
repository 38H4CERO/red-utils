package net.redct.client.utils.entity;

import net.minecraft.world.entity.Entity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GlowRegistry {
    private static final Set<UUID> glowing = ConcurrentHashMap.newKeySet();

    public static void setGlowing(Entity entity, boolean glow) {
        setGlowing(entity.getUUID(), glow);
    }

    public static void setGlowing(String uuid, boolean glow) {
        setGlowing(UUID.fromString(uuid), glow);
    }

    public static void setGlowing(UUID uuid, boolean glow) {
        if (glow) {
            glowing.add(uuid);
        } else {
            glowing.remove(uuid);
        }
    }


    public static void clearGlowRegistry(){
        glowing.clear();
    }

    public static boolean shouldGlow(Entity entity) {
        return shouldGlow(entity.getUUID());
    }
    public static boolean shouldGlow(String uuid) {
        return shouldGlow((UUID.fromString(uuid)));
    }
    public static boolean shouldGlow(UUID uuid) {
        return glowing.contains(uuid);
    }

}
