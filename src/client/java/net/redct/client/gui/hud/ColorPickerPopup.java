package net.redct.client.gui.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ColorSetting;
import net.redct.client.gui.config.UILayout;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.utils.GuiUtils;

public class ColorPickerPopup extends AbstractWidget {
    // TODO: move constants to UiLayout.java
    private static final int TITLE_BAR_HEIGHT = 14;

    // Layout Constants
    private static final int SB_SIZE   = 100; // saturation/brightness square
    private static final int BAR_WIDTH = 12;  // hue and alpha bars
    private static final int BAR_GAP   = 6;   // gap between square and bars
    private static final int PADDING   = 8;
    private static final int HEX_HEIGHT = 14;

    // Total popup size
    private static final int WIDTH  = PADDING + SB_SIZE + BAR_GAP + BAR_WIDTH + BAR_GAP + BAR_WIDTH + PADDING;
    private static final int HEIGHT = PADDING + SB_SIZE + BAR_GAP + TITLE_BAR_HEIGHT + HEX_HEIGHT + PADDING;

    private final ColorSetting target;

    // Current HSB + alpha state
    private float hue, saturation, brightness;
    private int alpha;

    // Drag state
    private boolean draggingSB   = false;
    private boolean draggingHue  = false;
    private boolean draggingAlpha = false;

    // Title Bar dragging state fields
    private boolean draggingTitle = false;
    private int dragX, dragY;

    public ColorPickerPopup(ColorSetting setting, int centerX, int centerY) {
        // 1. Pass total dimensions to the parent Widget class
        super(WIDTH, HEIGHT);

        this.target = setting;

        // 2. Set the starting coordinates on the screen
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
    private int sbY()    { return y + TITLE_BAR_HEIGHT + PADDING; }
    private int hueX()   { return sbX() + SB_SIZE + BAR_GAP; }
    private int hueY()   { return sbY(); }
    private int alphaX() { return hueX() + BAR_WIDTH + BAR_GAP; }
    private int alphaY() { return sbY(); }
    private int hexY()   { return sbY() + SB_SIZE + BAR_GAP; }

    // ── Rendering ──────────────────────────────────────────────
    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {

        // Process title dragging logic
        if (draggingTitle) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
        }

        // Render Title Bar
        graphics.fill(x, y, x + WIDTH, y + TITLE_BAR_HEIGHT, UITheme.FRAME_BG);
        graphics.outline(x, y, WIDTH, TITLE_BAR_HEIGHT, UITheme.BORDER);
        graphics.text(font, "Color Picker", x + UILayout.TEXT_X_OFFSET, y + 2, UITheme.TEXT_PRIMARY);

        // Background
        int bodyY = y + TITLE_BAR_HEIGHT;
        int bodyHeight = HEIGHT - TITLE_BAR_HEIGHT;
        graphics.fill(x, bodyY, x + WIDTH, bodyY + bodyHeight, UITheme.MODULE_BG);
        graphics.outline(x, bodyY, WIDTH, bodyHeight, UITheme.BORDER);

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
        // TODO: close if clicked outside
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
        if (GuiUtils.contains(mouseX, mouseY, x, y, WIDTH, TITLE_BAR_HEIGHT)) {
            if (button == 0) {
                this.draggingTitle = true;
                this.dragX = (int) (mouseX - this.x);
                this.dragY = (int) (mouseY - this.y);
                return true;
            }
        }
        return true; // consumes click inside bounds
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
            draggingTitle = false;
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

    // ── Helpers ─────────────────────────────────────────────────
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