package net.redct.client.utils.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.render.Tracer;
import net.redct.client.utils.render.Tracer.Anchor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {
    private static final Set<UUID> processed = ConcurrentHashMap.newKeySet();

    public static void onNameResolved(Entity entity, Component customName) {
        if (customName == null) return;

        UUID uuid = entity.getUUID();
        if (processed.contains(uuid)) return;

        String name = customName.getString();

        if (EntityUtils.isMob(name)){
            if (shouldGlow(name)){
                GlowRegistry.setGlowing(uuid, true);
            }
            if (shouldTrace(name)){
                Tracer.setLine(uuid.toString(), Anchor.player(), Anchor.entity(entity), 3f, ARGB.white(255));
            }

        } else {
            System.out.println("[ERROR] check "+ name);
        }

        processed.add(uuid);
    }

    private static boolean shouldGlow(String name){
        return false;
    }

    private static boolean shouldTrace(String name){
        return false;
    }

}
