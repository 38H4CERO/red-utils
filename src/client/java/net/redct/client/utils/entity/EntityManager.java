package net.redct.client.utils.entity;

import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.render.Tracer;
import net.redct.client.utils.render.Tracer.Anchor;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {
    private static final Set<UUID> processed = ConcurrentHashMap.newKeySet();
    private static final Set<String> trackedMobNames = ConcurrentHashMap.newKeySet();


    //TODO: Change name to a more descriptibe one
    public static void onNameResolved(Entity entity) {
        if (entity.getCustomName() == null) return;
        if (!entity.getType().toShortString().equals("armor_stand")) return;

        UUID uuid = entity.getUUID();

        // TODO: ns si meter solo los que brillan
        if (processed.contains(uuid)) return;

        // Regex
        String name = EntityUtils.mobNameParse(entity.getCustomName().getString());

        if (name != null){
            if (shouldGlow(name)){
                GlowRegistry.setGlowing(entity.getId()-1, true);
            }
            if (shouldTrace(name)){
                Tracer.setLine(uuid.toString(), Anchor.player(), Anchor.entity(entity), 3f, ARGB.white(255));
            }
            processed.add(uuid);
        } else {
            //System.out.println("[LOG] no mob entity = "+ customName.getString());
        }



    }

    public static void clearProcessedMobs(){
        processed.clear();
    }

    public static void addMobTypeGlowing(String name){
        trackedMobNames.add(name);
    }

    // TODO: esto esta mal, si algo brilla seguira brillando
    public static void removeMobTypeGlowing(String name){
        trackedMobNames.remove(name);
    }

    public static void removeProcessedMob(UUID uuid){
        processed.remove(uuid);
    }

    public static List<String> getTrackedNames() {
        return List.copyOf(trackedMobNames);
    }

    private static boolean shouldGlow(String name){
        return trackedMobNames.contains(name);
    }

    private static boolean shouldTrace(String name){
        return false;
    }

}
