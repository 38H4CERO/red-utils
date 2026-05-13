package net.redct.client.config;

public class SliderSetting extends Setting {
    private double value;
    private final double min, max;
    private boolean dragging; // <--- NEW

    public SliderSetting(String id, String name, double defaultValue, double min, double max) {
        super(id, name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    public double getValue() { return value; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public boolean isDragging() { return dragging; }
    public void setDragging(boolean dragging) { this.dragging = dragging; }

    public void setValue(double value) {
        this.value = Math.clamp(value, min, max);
    }

}