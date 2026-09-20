package net.redct.client.config;

public class SliderSetting extends Setting {
    private double value;
    private final double min, max, step;
    private boolean dragging;

    public SliderSetting(String id, String name, double defaultValue, double min, double max, double step) {
        super(id, name);
        this.min = min;
        this.max = max;
        this.step = step;
        setValue(defaultValue);
    }

    public double getValue() { return value; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public boolean isDragging() { return dragging; }
    public double getStep() { return step; }
    public void setDragging(boolean dragging) { this.dragging = dragging; }

    public void setValue(double value) {
        double temp = Math.clamp(value, min, max);
        temp = Math.floor(temp / step) * step;
        this.value = Math.clamp(temp, min, max);
    }

}