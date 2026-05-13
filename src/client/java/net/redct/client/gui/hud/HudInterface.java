package net.redct.client.gui.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface HudInterface {
    String getId();
    int getX();
    int getY();
    float getScale();
    void setXY(int x, int y);
    void setScale(float scale);
    public void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter);
    int getWidth();
    int getHeight();
}