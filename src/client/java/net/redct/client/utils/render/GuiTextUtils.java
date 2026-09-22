package net.redct.client.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.redct.client.gui.hud.HudInterface;

import java.util.List;

import static net.redct.client.utils.ColorUtils.makeARGB;


public class GuiTextUtils implements HudInterface {
    private final String id;
    private boolean isVisible = true;
    private float scale = 1.0f;
    private int x = 0;
    private int y = 0;
    private int color = 0xFFFFFFFF;
    private String text;
    private int lineHeight = 9; // minecraft font line height

    public GuiTextUtils(String id, int x, int y, float scale) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public GuiTextUtils(String id,int x, int y) {
        this(id, x, y, 1);
    }
    public GuiTextUtils(String id) {
        this(id,0, 0, 1);
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }

    public void setLines(List<String> lines) {
        this.text = String.join("\n", lines);
    }

    public List<String> getLines() {
        return text == null ? List.of() : List.of(text.split("\n", -1));
    }

    public int getLineCount() {
        return getLines().size();
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public String getId() { return id; }
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public float getScale() { return scale; }
    @Override public void setXY(int x, int y) { this.x = x; this.y = y;}
    @Override public void setScale(float scale) { this.scale = scale; }

    @Override
    public int getWidth() {
        Font font = Minecraft.getInstance().font;
        if (text == null ||text.isEmpty()) return 0;
        int maxWidth = 0;
        for (String line : text.split("\n", -1)) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }
        return (int)(maxWidth * scale);
    }

    @Override
    public int getHeight() {
        if (text == null || text.isEmpty()) return 0;
        int lineCount = text.split("\n", -1).length;
        return (int)(lineHeight * lineCount * scale);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        if (!isVisible || text == null || text.isEmpty()) return;

        // TODO: crear un GUIManager y inicializar font alli
        Font font = Minecraft.getInstance().font;
        String[] lines = text.split("\n", -1);

        if (scale != 1.0f){
            var pose = graphics.pose();
            pose.pushMatrix();
            try {
                pose.translate(x, y);
                pose.scale(scale, scale);
                for (int i = 0; i < lines.length; i++) {
                    graphics.text(font, lines[i], 0, i * lineHeight, color);
                }
            } finally {
                pose.popMatrix();
            }
        } else {
            for (int i = 0; i < lines.length; i++) {
                graphics.text(font, lines[i], x, y + i * lineHeight, color);
            }
        }
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    /**
     *
     * @param RGB 0xFFFFFF white
     * @param alpha 0-255, 0xFF for opaque
     */
    public void setColor(int RGB, int alpha) {
        this.color = makeARGB(RGB, alpha);
    }


    public void setColor(int color) {
        this.color = color;
    }

    public static void sendTitle(String title, String subtitle) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.gui.setTitle(Component.literal(title));
        client.gui.setSubtitle(Component.literal(subtitle));
        client.gui.setTimes(0, 30, 10); // fadeIn, stay, fadeOut in ticks
    }
}