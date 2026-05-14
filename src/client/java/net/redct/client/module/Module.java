package net.redct.client.module;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ConfigManager;
import net.redct.client.config.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String id;
    private final Category category;
    private boolean enabled = false;
    private boolean expanded = false;
    private final List<Setting> settings = new ArrayList<>();
    private final List<Setting> settingsView = Collections.unmodifiableList(settings);

    public Module(String id, String name, Category category) {
        this.name = name;
        this.id = id;
        this.category = category;
    }

    // ── Settings ──────────────────────────────────────────────
    protected void registerSetting(Setting setting) {
        settings.add(setting);
    }

    public List<Setting> getSettings() {
        return settingsView;
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    // ── Expanded state ────────────────────────────────────────
    public boolean isExpanded() {
        return expanded;
    }

    public void toggleExpanded() {
        if (hasSettings()) expanded = !expanded;
    }

    // ── Getters ──────────────────────────────────────────────
    public String getName()         { return name; }
    public String getID()           { return id; }
    public Category getCategory()   { return category; }
    public boolean isEnabled()      { return enabled; }

    public void setStatus(boolean status) {
        this.enabled = status;
        if (status) onEnable();
        else onDisable();
        ConfigManager.save();
    }

    public void toggle() {
        setStatus(!enabled);
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender(GuiGraphicsExtractor graphics, Font font) {}
}
