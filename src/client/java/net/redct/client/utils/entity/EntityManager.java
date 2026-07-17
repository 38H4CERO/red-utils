package net.redct.client.utils.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.render.Tracer;
import net.redct.client.utils.render.Tracer.Anchor;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {
    private static final Set<UUID> processed = ConcurrentHashMap.newKeySet();
    private static final ArrayList<String> trackedMobNames = new ArrayList<>();
    private static final Set<String> glowingMobs = ConcurrentHashMap.newKeySet();


    public static void onNameResolved(Entity entity, Component customName) {
        if (customName == null) return;
        if (!entity.getType().toShortString().equals("armor_stand")) return;

        UUID uuid = entity.getUUID();
        if (processed.contains(uuid)) return;

        // Regex
        String name = EntityUtils.mobNameParse(customName.getString());

        if (name != null){
            if (shouldGlow(name)){
                GlowRegistry.setGlowing(uuid, true);
            }
            if (shouldTrace(name)){
                Tracer.setLine(uuid.toString(), Anchor.player(), Anchor.entity(entity), 3f, ARGB.white(255));
            }
            processed.add(uuid);
        } else {
            //System.out.println("[LOG] no mob entity = "+ customName.getString());
        }


    }

    public static void clearEnityManager(){
        processed.clear();
        //glowingMobs.clear();
    }

    public static void addMobTypeGlowing(String name){
        glowingMobs.add(name);
    }

    private static boolean shouldGlow(String name){
        return glowingMobs.contains(name);
    }

    private static boolean shouldTrace(String name){
        return false;
    }

}
