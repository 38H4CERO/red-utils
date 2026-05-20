package net.redct.client.gui.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ColorSetting;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.utils.GuiUtils;

public class ColorPickerPopup extends AbstractWidget {
    // Layout Constants
    private static final int SB_SIZE   = 100;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_GAP   = 6;
    private static final int PADDING   = 8;
    private static final int HEX_HEIGHT = 14;

    private static final int WIDTH  = PADDING + SB_SIZE + BAR_GAP + BAR_WIDTH + BAR_GAP + BAR_WIDTH + PADDING;
    private static final int HEIGHT = PADDING + SB_SIZE + BAR_GAP + HEX_HEIGHT + PADDING;

    private final ColorSetting target;

    // HSB state
    private float hue, saturation, brightness;
    private int alpha;

    // Drag states (Only for the color controls themselves!)
    private boolean draggingSB    = false;
    private boolean draggingHue   = false;
    private boolean draggingAlpha = false;

    public ColorPickerPopup(ColorSetting setting) {
        super(WIDTH, HEIGHT);
        this.target = setting;

        this.hue        = setting.getHue();
        this.saturation = setting.getSaturation();
        this.brightness = setting.getBrightness();
        this.alpha      = setting.getAlpha();
    }

    // ── Coordinates (Simple, no offsets!) ──────────────────────
    private int sbX()    { return x + PADDING; }
    private int sbY()    { return y + PADDING; }
    private int hueX()   { return sbX() + SB_SIZE + BAR_GAP; }
    private int hueY()   { return sbY(); }
    private int alphaX() { return hueX() + BAR_WIDTH + BAR_GAP; }
    private int alphaY() { return sbY(); }
    private int hexY()   { return sbY() + SB_SIZE + BAR_GAP; }

    // ── Rendering ──────────────────────────────────────────────
    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        // Render pure content background
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, UITheme.MODULE_BG);
        graphics.outline(x, y, WIDTH, HEIGHT, UITheme.BORDER);

        renderSBSquare(graphics);
        renderHueBar(graphics);
        renderAlphaBar(graphics);
        renderHex(graphics, font);
        renderIndicators(graphics);
    }

    private void renderSBSquare(GuiGraphicsExtractor graphics) {
        int hueColor = hsbToArgb(hue, 1f, 1f, 255);
        for (int i = 0; i < SB_SIZE; i++) {
            float s = (float) i / SB_SIZE;
            int topColor    = blendColors(0xFFFFFFFF, hueColor, s);
            int bottomColor = 0xFF000000;
            graphics.fillGradient(sbX() + i, sbY(), sbX() + i + 1, sbY() + SB_SIZE, topColor, bottomColor);
        }
    }

    private void renderHueBar(GuiGraphicsExtractor graphics) {
        float[] hueStops = {0f, 1f/6f, 2f/6f, 3f/6f, 4f/6f, 5f/6f, 1f};

        int lastY = hueY();
        for (int i = 0; i < 6; i++) {
            int nextY = hueY() + (int) Math.round((double)(i + 1) * SB_SIZE / 6.0);

            int col1 = hsbToArgb(hueStops[i],     1f, 1f, 255);
            int col2 = hsbToArgb(hueStops[i + 1], 1f, 1f, 255);

            graphics.fillGradient(
                    hueX(), lastY,
                    hueX() + BAR_WIDTH, nextY,
                    col1, col2
            );

            lastY = nextY;
        }
        graphics.outline(hueX(), hueY(), BAR_WIDTH, SB_SIZE, UITheme.BORDER);
    }

    private void renderAlphaBar(GuiGraphicsExtractor graphics) {
        int currentColor = hsbToArgb(hue, saturation, brightness, 255);
        graphics.fillGradient(
                alphaX(), alphaY(),
                alphaX() + BAR_WIDTH, alphaY() + SB_SIZE,
                currentColor, 0x00000000
        );
        graphics.outline(alphaX(), alphaY(), BAR_WIDTH, SB_SIZE, UITheme.BORDER);
    }

    private void renderHex(GuiGraphicsExtractor graphics, Font font) {
        int argb = hsbToArgb(hue, saturation, brightness, alpha);
        String hex = String.format("#%08X", argb);

        graphics.fill(sbX(), hexY(), sbX() + SB_SIZE, hexY() + HEX_HEIGHT, UITheme.SETTING_BG);
        graphics.outline(sbX(), hexY(), SB_SIZE, HEX_HEIGHT, UITheme.BORDER);
        graphics.text(font, hex, sbX() + 4, hexY() + 3, UITheme.TEXT_PRIMARY);
    }

    private void renderIndicators(GuiGraphicsExtractor graphics) {
        int dotX = sbX() + (int)(saturation * SB_SIZE);
        int dotY = sbY() + (int)((1f - brightness) * SB_SIZE);
        graphics.outline(dotX - 2, dotY - 2, 4, 4, UITheme.TEXT_PRIMARY);

        int hueIndicatorY = hueY() + (int)(hue * SB_SIZE);
        graphics.fill(hueX() - 1, hueIndicatorY - 1, hueX() + BAR_WIDTH + 1, hueIndicatorY + 1, UITheme.TEXT_PRIMARY);

        int alphaIndicatorY = alphaY() + (int)((1f - alpha / 255f) * SB_SIZE);
        graphics.fill(alphaX() - 1, alphaIndicatorY - 1, alphaX() + BAR_WIDTH + 1, alphaIndicatorY + 1, UITheme.TEXT_PRIMARY);
    }

    // ── Interaction ────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isInsidePopup(mouseX, mouseY)) return false;

        if (GuiUtils.contains(mouseX, mouseY, sbX(), sbY(), SB_SIZE, SB_SIZE)) {
            draggingSB = true;
            updateSB(mouseX, mouseY);
            return true;
        }
        if (GuiUtils.contains(mouseX, mouseY, hueX(), hueY(), BAR_WIDTH, SB_SIZE)) {
            draggingHue = true;
            updateHue(mouseY);
            return true;
        }
        if (GuiUtils.contains(mouseX, mouseY, alphaX(), alphaY(), BAR_WIDTH, SB_SIZE)) {
            draggingAlpha = true;
            updateAlpha(mouseY);
            return true;
        }
        return true;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (draggingSB)    updateSB(mouseX, mouseY);
        if (draggingHue)   updateHue(mouseY);
        if (draggingAlpha) updateAlpha(mouseY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingSB    = false;
            draggingHue   = false;
            draggingAlpha = false;
        }
    }

    // ── Update logic ────────────────────────────────────────────
    private void updateSB(double mouseX, double mouseY) {
        saturation = (float) Math.clamp((mouseX - sbX()) / SB_SIZE, 0.0, 1.0);
        brightness = 1f - (float) Math.clamp((mouseY - sbY()) / SB_SIZE, 0.0, 1.0);
        applyColor();
    }

    private void updateHue(double mouseY) {
        hue = (float) Math.clamp((mouseY - hueY()) / SB_SIZE, 0.0, 1.0);
        applyColor();
    }

    private void updateAlpha(double mouseY) {
        alpha = (int)(255 * (1f - Math.clamp((mouseY - alphaY()) / SB_SIZE, 0.0, 1.0)));
        applyColor();
    }

    private void applyColor() {
        target.setFromHSBA(hue, saturation, brightness, alpha);
    }

    public boolean isInsidePopup(double mouseX, double mouseY) {
        return GuiUtils.contains(mouseX, mouseY, x, y, width, height);
    }

    private int hsbToArgb(float h, float s, float b, int a) {
        int rgb = java.awt.Color.HSBtoRGB(h, s, b);
        return ((a & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    private int blendColors(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}