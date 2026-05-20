package net.redct.client.gui.widget;

import net.redct.client.utils.GuiUtils;

public abstract class AbstractWidget implements Widget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    // 1. Hold a reference to the parent Panel
    protected Panel parent;

    public AbstractWidget(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public Panel getParent() { return parent; }
    @Override public void setParent(Panel parent) { this.parent = parent; }

    // 2. Bubble up layout changes to the parent containers
    public void revalidate() {
        if (parent != null) {
            parent.layout();      // Force parent to realign its children
            parent.revalidate();  // Ask parent to tell its parent, and so on
        }
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return GuiUtils.contains(mouseX, mouseY, x, y, width, height);
    }

    @Override public void mouseDragged(double mouseX, double mouseY, int button) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int button) {}
}