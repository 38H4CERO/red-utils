package net.redct.client.gui.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface HudInterface {
    String getId();
    int getX();
    int getY();
    float getScale();
    void setXY(int x, int y);
    void setScale(float scale);
    void render(GuiGraphicsExtractor graphics);
    boolean isVisible();
    void setVisible(boolean visible);
    int getWidth();
    int getHeight();
}