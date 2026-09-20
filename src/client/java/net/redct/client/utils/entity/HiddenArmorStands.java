package net.redct.client.utils.entity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum HiddenArmorStands {
    INSTANCE;

    private static final Set<UUID> processed = ConcurrentHashMap.newKeySet();
    private final Set<UUID> hidden = ConcurrentHashMap.newKeySet();

    public void hide(UUID uuid) {
        hidden.add(uuid);
    }

    public void show(UUID uuid) {
        hidden.remove(uuid);
    }

    public void setProcessed (UUID uuid){
        processed.add(uuid);
    }

    public void removeProccesed(UUID uuid) {
        processed.remove(uuid);
    }

    public boolean isProcessed (UUID uuid){
        return processed.contains(uuid);
    }

    public boolean isHidden(UUID uuid) {
        return hidden.contains(uuid);
    }

    public void clear() {
        hidden.clear();
        processed.clear();
    }
}