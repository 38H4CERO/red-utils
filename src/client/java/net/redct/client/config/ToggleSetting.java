package net.redct.client.config;

public class ToggleSetting extends Setting {
    private boolean value;

    public ToggleSetting(String id, String name, boolean defaultValue) {
        super(id, name);
        this.value = defaultValue;
    }

    public boolean getValue() { return value; }
    public void toggle() { value = !value; }
}