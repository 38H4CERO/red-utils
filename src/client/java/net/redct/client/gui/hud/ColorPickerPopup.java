package net.redct.client.gui.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ColorSetting;
import net.redct.client.utils.GuiUtils;

/*
    TODO:
    - Alpha color reversed
    - ARGB text
    - Text input editable
    - Color square in right side of color code
    - movable popup
    - save in config
 */
public class ColorPickerPopup {
    // Layout
    private static final int SB_SIZE   = 100; // saturation/brightness square
    private static final int BAR_WIDTH = 12;  // hue and alpha bars
    private static final int BAR_GAP   = 6;   // gap between square and bars
    private static final int PADDING   = 8;
    private static final int HEX_HEIGHT = 14;

    // Total popup size
    private static final int WIDTH  = PADDING + SB_SIZE + BAR_GAP + BAR_WIDTH + BAR_GAP + BAR_WIDTH + PADDING;
    private static final int HEIGHT = PADDING + SB_SIZE + BAR_GAP + HEX_HEIGHT + PADDING;

    private final ColorSetting target;
    private final int x, y; // top-left of popup

    // Current HSB + alpha state
    private float hue, saturation, brightness;
    private int alpha;

    // Drag state
    private boolean draggingSB   = false;
    private boolean draggingHue  = false;
    private boolean draggingAlpha = false;

    public ColorPickerPopup(ColorSetting setting, int centerX, int centerY) {
        this.target = setting;
        this.x = centerX - WIDTH / 2;
        this.y = centerY - HEIGHT / 2;

        // Load current color into HSB
        this.hue        = setting.getHue();
        this.saturation = setting.getSaturation();
        this.brightness = setting.getBrightness();
        this.alpha      = setting.getAlpha();
    }

    // ── Coordinates ────────────────────────────────────────────
    private int sbX()    { return x + PADDING; }
    private int sbY()    { return y + PADDING; }
    private int hueX()   { return sbX() + SB_SIZE + BAR_GAP; }
    private int hueY()   { return sbY(); }
    private int alphaX() { return hueX() + BAR_WIDTH + BAR_GAP; }
    private int alphaY() { return sbY(); }
    private int hexY()   { return sbY() + SB_SIZE + BAR_GAP; }

    // ── Rendering ──────────────────────────────────────────────
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        // Background
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF1A1A1A);
        graphics.outline(x, y, WIDTH, HEIGHT, 0xFF444444);

        renderSBSquare(graphics);
        renderHueBar(graphics);
        renderAlphaBar(graphics);
        renderHex(graphics, font);
        renderIndicators(graphics);
    }

    private void renderSBSquare(GuiGraphicsExtractor graphics) {
        int hueColor = hsbToArgb(hue, 1f, 1f, 255);

        // Draw SB square using thin vertical strips for horizontal gradient
        // Each strip goes from (white blended with hue) at top to black at bottom
        for (int i = 0; i < SB_SIZE; i++) {
            float s = (float) i / SB_SIZE;
            int topColor    = blendColors(0xFFFFFFFF, hueColor, s);
            int bottomColor = 0xFF000000;
            graphics.fillGradient(sbX() + i, sbY(), sbX() + i + 1, sbY() + SB_SIZE, topColor, bottomColor);
        }
    }

    private void renderHueBar(GuiGraphicsExtractor graphics) {
        // 6 hue stops: red → yellow → green → cyan → blue → magenta → red
        float[] hueStops = {0f, 1f/6f, 2f/6f, 3f/6f, 4f/6f, 5f/6f, 1f};
        int segmentHeight = SB_SIZE / 6;

        for (int i = 0; i < 6; i++) {
            int col1 = hsbToArgb(hueStops[i],     1f, 1f, 255);
            int col2 = hsbToArgb(hueStops[i + 1], 1f, 1f, 255);
            graphics.fillGradient(
                    hueX(), hueY() + i * segmentHeight,
                    hueX() + BAR_WIDTH, hueY() + (i + 1) * segmentHeight,
                    col1, col2
            );
        }
        graphics.outline(hueX(), hueY(), BAR_WIDTH, SB_SIZE, 0xFF444444);
    }

    private void renderAlphaBar(GuiGraphicsExtractor graphics) {
        int currentColor = hsbToArgb(hue, saturation, brightness, 255);
        graphics.fillGradient(
                alphaX(), alphaY(),
                alphaX() + BAR_WIDTH, alphaY() + SB_SIZE,
                currentColor, 0x00000000
        );
        graphics.outline(alphaX(), alphaY(), BAR_WIDTH, SB_SIZE, 0xFF444444);
    }

    private void renderHex(GuiGraphicsExtractor graphics, Font font) {
        int argb = hsbToArgb(hue, saturation, brightness, alpha);
        String hex = String.format("#%08X", argb);

        graphics.fill(sbX(), hexY(), sbX() + SB_SIZE, hexY() + HEX_HEIGHT, 0xFF111111);
        graphics.outline(sbX(), hexY(), SB_SIZE, HEX_HEIGHT, 0xFF444444);
        graphics.text(font, hex, sbX() + 4, hexY() + 3, 0xFFFFFFFF);
    }

    private void renderIndicators(GuiGraphicsExtractor graphics) {
        // SB square — small circle at current saturation/brightness
        int dotX = sbX() + (int)(saturation * SB_SIZE);
        int dotY = sbY() + (int)((1f - brightness) * SB_SIZE);
        graphics.outline(dotX - 2, dotY - 2, 4, 4, 0xFFFFFFFF);

        // TODO: graphics.horizontalLine();
        // Hue bar — horizontal line at current hue
        int hueIndicatorY = hueY() + (int)(hue * SB_SIZE);
        graphics.fill(hueX() - 1, hueIndicatorY - 1, hueX() + BAR_WIDTH + 1, hueIndicatorY + 1, 0xFFFFFFFF);

        // Alpha bar — horizontal line at current alpha
        int alphaIndicatorY = alphaY() + (int)((1f - alpha / 255f) * SB_SIZE);
        graphics.fill(alphaX() - 1, alphaIndicatorY - 1, alphaX() + BAR_WIDTH + 1, alphaIndicatorY + 1, 0xFFFFFFFF);
    }

    // ── Interaction ────────────────────────────────────────────
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
        return true; // consume click even if inside popup but not on a control
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (draggingSB)    updateSB(mouseX, mouseY);
        if (draggingHue)   updateHue(mouseY);
        if (draggingAlpha) updateAlpha(mouseY);
    }

    public void mouseReleased() {
        draggingSB    = false;
        draggingHue   = false;
        draggingAlpha = false;
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

    // ── Helpers ─────────────────────────────────────────────────
    public boolean isInsidePopup(double mouseX, double mouseY) {
        return GuiUtils.contains(mouseX, mouseY, x, y, WIDTH, HEIGHT);
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