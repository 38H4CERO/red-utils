package net.redct.client.config;

import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final String id;
    private final String name;
    private BooleanSupplier visibilityCondition = () -> true; // visible by default

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

}