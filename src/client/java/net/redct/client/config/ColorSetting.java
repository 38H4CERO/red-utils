package net.redct.client.config;

public class ColorSetting extends Setting {
    private int color; // ARGB

    public ColorSetting(String id, String name, int defaultColor) {
        super(id, name);
        this.color = defaultColor;
    }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    // Helpers to extract components
    public float[] getHSB(){
        return java.awt.Color.RGBtoHSB(
                (color >> 16) & 0xFF,
                (color >> 8)  & 0xFF,
                color        & 0xFF, null);
    }

    public float getHue()        { return getHSB()[0]; }
    public float getSaturation() { return getHSB()[1]; }
    public float getBrightness() { return getHSB()[2]; }

    public int getAlpha() { return (color >> 24) & 0xFF; }

    public void setFromHSBA(float h, float s, float b, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(h, s, b);
        this.color = ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }
}