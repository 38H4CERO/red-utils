package net.redct.client.gui.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;


public class GuiTextUtils implements HudInterface {
    private String id;
    private float scale = 1.0f;
    private int x = 0;
    private int y = 0;
    private int color = 0xFFFFFFFF;
    private String text = "";

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
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setColor(int color) {
        this.color = color;
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
        if (font == null) return 0;
        return (int)(font.width(getText()) * scale);
    }

    @Override
    public int getHeight() {
        return (int)(9 * scale); // minecraft font height is always 9
    }


    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        // TODO: crear un GUIManager y inicializar font alli
        Font font = Minecraft.getInstance().font;

        if (scale != 1.0f){
            var pose = graphics.pose();
            pose.pushMatrix();
            try {
                pose.translate(x, y);
                pose.scale(scale, scale);
                graphics.text(font, text, 0, 0, color);
            } finally {
                pose.popMatrix();
            }
        } else {
            graphics.text(font, text, x, y, color);
        }
    }

    public static void sendTitle(String title, String subtitle) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.gui.setTitle(Component.literal(title));
        client.gui.setSubtitle(Component.literal(subtitle));
        client.gui.setTimes(0, 30, 10); // fadeIn, stay, fadeOut in ticks
    }
}
