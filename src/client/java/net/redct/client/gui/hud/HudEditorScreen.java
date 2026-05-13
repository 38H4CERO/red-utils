package net.redct.client.gui.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.redct.client.config.ConfigManager;
import java.util.List;

public class HudEditorScreen extends Screen {
    private HudInterface dragging = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Dark background
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        for (HudInterface element : HudManager.getElements()) {
            // Render each element normally
            element.render(graphics, null);

            // Draw a highlight box around it
            boolean hovering = isHovering(mouseX, mouseY, element);
            int borderColor = hovering ? 0xFFFFFFFF : 0x88AAAAAA;
            drawBorder(graphics, element, borderColor);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Loop BACKWARDS so if two elements overlap, you grab the one on top!
        List<HudInterface> elements = HudManager.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudInterface element = elements.get(i);

            if (isHovering(event.x(), event.y(), element)) {
                dragging = element;
                // Record exactly where on the element the mouse grabbed it
                dragOffsetX = (int) (event.x() - element.getX());
                dragOffsetY = (int) (event.y() - element.getY());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) {
            dragging.setXY(
                    (int) (event.x() - dragOffsetX),
                    (int) (event.y() - dragOffsetY)
            );
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            ConfigManager.save(); // persist new position
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean isHovering(double mouseX, double mouseY, HudInterface element) {
        return mouseX >= element.getX() && mouseX <= element.getX() + element.getWidth()
                && mouseY >= element.getY() && mouseY <= element.getY() + element.getHeight();
    }

    private void drawBorder(GuiGraphicsExtractor graphics, HudInterface element, int color) {
        int x = element.getX() - 2;
        int y = element.getY() - 2;
        int w = element.getWidth() + 4;
        int h = element.getHeight() + 2;

        // Top line
        graphics.fill(x + 1, y, x + w, y + 1, color);
        // Bottom line
        graphics.fill(x, y + h, x + w + 1, y + h + 1, color);
        // Left line
        graphics.fill(x, y, x + 1, y + h, color);
        // Right line
        graphics.fill(x + w, y, x + w + 1, y + h, color);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}