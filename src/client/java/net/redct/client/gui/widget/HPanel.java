package net.redct.client.gui.widget;

public class HPanel extends Panel {
    @Override
    public void layout() {
        int currentX = this.x;
        int maxHeight = 0;

        for (Widget w : children) {
            w.setPosition(currentX, this.y);
            currentX += w.getWidth();

            if (w.getHeight() > maxHeight) {
                maxHeight = w.getHeight();
            }
        }

        this.width = currentX - this.x;
        this.height = maxHeight;
    }

    @Override
    public int getWidth() {
        int totalWidth = 0;
        for (Widget w : children) {
            totalWidth += w.getWidth();
        }
        return totalWidth;
    }
}