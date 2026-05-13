package net.redct.client.gui.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.redct.client.config.ConfigManager;

public class HudEditorScreen extends Screen {
    private HudInterface dragging = null;

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
        for (HudInterface element : HudManager.getElements()) {
            if (isHovering(event.x(), event.y(), element)) {
                dragging = element;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) {
            // No need to track dragOffset — just add the delta directly
            dragging.setXY(
                    (int) (dragging.getX() + dx),
                    (int) (dragging.getY() + dy)
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
        int h = element.getHeight() + 4;
        // top, bottom, left, right
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h, x + w, y + h + 1, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w, y, x + w + 1, y + h, color);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}