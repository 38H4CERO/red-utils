package net.redct.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface Widget {
    int getX();
    int getY();
    void setPosition(int x, int y);

    int getWidth();
    int getHeight();

    // 1. Add parent management to the contract
    Panel getParent();
    void setParent(Panel parent);

    void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    default void mouseDragged(double mouseX, double mouseY, int button) {}
    default void mouseReleased(double mouseX, double mouseY, int button) {}
}