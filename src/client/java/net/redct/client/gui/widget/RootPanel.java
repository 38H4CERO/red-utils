package net.redct.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.utils.GuiUtils;
import java.util.ArrayList;
import java.util.List;

public class RootPanel {
    private static RootPanel instance;

    public static RootPanel getInstance() {
        return instance;
    }

    private final Panel content;
    private final List<Widget> overlays = new ArrayList<>();

    public RootPanel(Panel layout) {
        this.content = layout;
        instance = this;
    }

    public void add(Widget w) {
        content.add(w);
    }

    public void addOverlay(Widget overlay) {
        overlays.add(overlay);
    }

    public void clearOverlays() {
        overlays.clear();
    }

    public void overlayPop(){
        overlays.removeLast();
    }

    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        content.render(graphics, font, mouseX, mouseY);
        for (Widget overlay : overlays) {
            overlay.render(graphics, font, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. If an overlay is open, handle it modally
        if (!overlays.isEmpty()) {
            Widget topOverlay = overlays.getLast();

            // Check if the click occurred inside the overlay's bounding box
            boolean clickedInside = GuiUtils.contains(mouseX, mouseY,
                    topOverlay.getX(), topOverlay.getY(),
                    topOverlay.getWidth(), topOverlay.getHeight()
            );

            if (clickedInside) {
                topOverlay.mouseClicked(mouseX, mouseY, button);
            } else if (topOverlay.isDismissible()) {
                // Only close if the widget allows it!

                overlayPop();
            }
            return true; // Always return true to consume the click and block background
        }

        // 2. Standard background click handling (runs only if no overlays are open)
        return content.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
        // If an overlay is open, block background dragging
        if (!overlays.isEmpty()) {
            overlays.get(overlays.size() - 1).mouseDragged(mouseX, mouseY, button);
        } else {
            content.mouseDragged(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        // If an overlay is open, block background releases
        if (!overlays.isEmpty()) {
            overlays.get(overlays.size() - 1).mouseReleased(mouseX, mouseY, button);
        } else {
            content.mouseReleased(mouseX, mouseY, button);
        }
    }

    public Panel getContent() {
        return content;
    }

    public List<Widget> getOverlays() {
        return overlays;
    }
}