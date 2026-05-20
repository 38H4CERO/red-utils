package net.redct.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;

public class RootPanel {
    // 1. Static instance to allow global access to overlays
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

    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        content.render(graphics, font, mouseX, mouseY);
        for (Widget overlay : overlays) {
            overlay.render(graphics, font, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = overlays.size() - 1; i >= 0; i--) {
            Widget overlay = overlays.get(i);
            if (overlay.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return content.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
        content.mouseDragged(mouseX, mouseY, button);
        for (Widget overlay : overlays) {
            overlay.mouseDragged(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        content.mouseReleased(mouseX, mouseY, button);
        for (Widget overlay : overlays) {
            overlay.mouseReleased(mouseX, mouseY, button);
        }
    }

    public Panel getContent() {
        return content;
    }

    public List<Widget> getOverlays() {
        return overlays;
    }
}