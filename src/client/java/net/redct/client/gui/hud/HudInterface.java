package net.redct.client.gui.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.utils.GuiUtils;

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

    default GuiUtils.Rect getRect() {
        return new GuiUtils.Rect(getX(), getY(), getWidth(), getHeight());
    }
}