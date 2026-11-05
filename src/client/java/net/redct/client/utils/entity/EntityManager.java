package net.redct.client.utils.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.redct.client.data.Location;
import net.redct.client.utils.PlayerInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EntityManager {
    private final static EntityManager INSTANCE = new EntityManager();
    // Map<EntityType, Map<CustomName, Settings>>
    private volatile Map<String, List<EntitySettings>> index = Map.of();
    private final Map<UUID, EntitySettings> activeMobs = new ConcurrentHashMap<>();
    private final List<EntitySettings> allSettings = new CopyOnWriteArrayList<>();


    public static EntityManager getInstance(){
        return INSTANCE;
    }


    // TODO: Change all getType() to entity.getType().equals(EntityTypes.ARMOR_STAND)
    public void tryMatch(Entity e) {
        double hp = e instanceof LivingEntity le
                ? le.getAttributeBaseValue(Attributes.MAX_HEALTH) : 0;
        tryMatch(e, hp);
    }

    public void tryMatch(Entity e, double maxHP){

        if (activeMobs.containsKey(e.getUUID())) return;

        List<EntitySettings> candidates = index.get(e.getType().toShortString());
        if (candidates == null) return;


        Location loc = PlayerInfo.INSTANCE.getCurrentLocation();
        for (EntitySettings s : candidates) {
            if (matches(e, maxHP, loc, s.fingerprint)) {
                activeMobs.put(e.getUUID(), s);
                return;
            }
        }

    }

    public void setSettings(Collection<EntitySettings> all) {
        Map<String, List<EntitySettings>> m = new HashMap<>();
        for (EntitySettings s : all)
            m.computeIfAbsent(s.fingerprint.entityType, k -> new ArrayList<>()).add(s);
        index = m;
        activeMobs.clear();

        if (Minecraft.getInstance() == null) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof LivingEntity le) {
                tryMatch(le, le.getAttributeBaseValue(Attributes.MAX_HEALTH));
            }
        }
    }

    private boolean matches(Entity e, double maxHP, Location loc, EntitySettings.EntityFingerprint fp) {
        if (!fp.locationMatches(loc)) return false;
        if (!fp.hpMatches(maxHP)) return false;

        if (e.getType() == EntityTypes.PLAYER){
            boolean real = e.getUUID().version() == 4;
            if (real != fp.RealPlayer) return false;
            if (!fp.name.equalsIgnoreCase(e.getName().getString().trim())) return false;

            // Skin check??
        }

        // Equipment comparation
        if (!fp.equipment.isEmpty()){
            if (!(e instanceof LivingEntity le)) return false;
            for (var entry : fp.equipment.entrySet()){
                if (!entry.getValue().matches(le.getItemBySlot(entry.getKey()))) return false;
            }
        }

        return true;
    }

    public void addSetting(EntitySettings s) {
        if (allSettings.contains(s)) return;
        allSettings.add(s);
        setSettings(allSettings);
    }

    public void removeSetting(EntitySettings s) {
        allSettings.remove(s);
        setSettings(allSettings);
    }

    public void onEntityUnloaded(Entity e) {
        activeMobs.remove(e.getUUID());
    }

    public void onEntityLoaded(Entity e) {
        if (e.getUUID().version() == 4){
            //onHealthUpdated((e), 20);
        }
    }

    public void clearEntities(){
        activeMobs.clear();
    }

    public EntitySettings get(Entity e) { return activeMobs.get(e.getUUID()); }


}