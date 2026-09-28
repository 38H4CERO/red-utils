package net.redct.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final String id;
    private final String name;
    private BooleanSupplier visibilityCondition = () -> true; // visible by default
    private final List<Runnable> changeListeners = new ArrayList<>();

    public Setting(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T extends Setting> T visibleWhen(BooleanSupplier condition) {
        this.visibilityCondition = condition;
        return (T) this;
    }

    public boolean isVisible() {
        return visibilityCondition.getAsBoolean();
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T extends Setting> T onChange(Runnable listener) {
        changeListeners.add(listener);
        return (T) this;
    }

    protected void notifyChange() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

}