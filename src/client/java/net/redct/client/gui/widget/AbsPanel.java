package net.redct.client.gui.widget;

public class AbsPanel extends Panel {
    @Override
    public void layout() {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. Loop backwards (top-to-bottom) to find which window was clicked
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget child = children.get(i);

            if (child.mouseClicked(mouseX, mouseY, button)) {
                // 2. Click detected! Remove the window from its current index...
                children.remove(i);
                // 3. ...and add it to the very end of the list so it draws ON TOP of the others
                children.add(child);
                return true;
            }
        }
        return false;
    }

    @Override public int getWidth() { return 0; }
    @Override public int getHeight() { return 0; }
}