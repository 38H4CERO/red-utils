package net.redct.client.utils.entity;

import net.minecraft.world.entity.Entity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlowRegistry {
    private static final Set<Integer> glowing = ConcurrentHashMap.newKeySet();

    public static void setGlowing(Entity entity, boolean glow) {
        setGlowing(entity.getId(), glow);
    }


    public static void setGlowing(int id, boolean glow) {
        if (glow) {
            glowing.add(id);
        } else {
            glowing.remove(id);
        }
    }



    public static void clearGlowRegistry(){
        glowing.clear();
    }

    public static boolean shouldGlow(Entity entity) {
        return shouldGlow(entity.getId());
    }

    public static boolean shouldGlow(int id) {
        return glowing.contains(id);
    }

}