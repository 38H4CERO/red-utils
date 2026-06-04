package net.redct.client.gui.widget;

public class VPanel extends Panel {
    @Override
    public void recalculateLayout() {
        int currentY = this.y;
        int maxWidth = 0;

        for (Widget w : children) {
            w.setPosition(this.x, currentY);
            currentY += w.getHeight();

            if (w.getWidth() > maxWidth) {
                maxWidth = w.getWidth();
            }
        }

        this.width = maxWidth;
        this.height = currentY - this.y;
    }

    @Override
    public int getHeight() {
        int totalHeight = 0;
        for (Widget w : children) {
            totalHeight += w.getHeight();
        }
        return totalHeight;
    }
}