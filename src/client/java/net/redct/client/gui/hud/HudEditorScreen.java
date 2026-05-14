package net.redct.client.gui.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.redct.client.config.ConfigManager;
import net.redct.client.module.Module;

import java.util.List;

public class HudEditorScreen extends Screen {
    private HudInterface element = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    public void init() {
        HudManager.setEditorOpen(true);
    }

    @Override
    public void onClose() {
        HudManager.setEditorOpen(false);
        ConfigManager.save();
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Dark background
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        for (HudInterface element : HudManager.getElements()) {
            Module module = HudManager.getModule(element);
            if (module != null && !module.isEnabled()) continue;
            element.render(graphics);
            boolean hovering = element.getRect().withPadding(3).contains(mouseX, mouseY);
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

            if (element.getRect().withPadding(3).contains(event.x(), event.y())) {
                HudManager.bringToFront(element); // bring to front
                this.element = element;
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
        if (element != null) {
            element.setXY(
                    (int) (event.x() - dragOffsetX),
                    (int) (event.y() - dragOffsetY)
            );
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (element != null) {
            element = null;
            ConfigManager.save(); // persist new position
            return true;
        }
        return super.mouseReleased(event);
    }

    private void drawBorder(GuiGraphicsExtractor graphics, HudInterface element, int color) {
        int x = element.getX() - 2;
        int y = element.getY() - 2;
        int w = element.getWidth() + 2;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<HudInterface> elements = HudManager.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudInterface element = elements.get(i);
            if (element.getRect().withPadding(3).contains(mouseX, mouseY)) {
                float newScale = element.getScale() + (float) scrollY * 0.1f;
                newScale = Math.round(newScale * 10) / 10f;
                element.setScale(Math.max(0.1f, newScale));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // TODO: Only works while mouse is pressed, refactor all to make it work
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (element != null) {
            int amount = event.hasShiftDown() ? 10 : 1;

            if (event.isLeft())  { element.setXY(element.getX() - amount, element.getY()); return true; }
            if (event.isRight()) { element.setXY(element.getX() + amount, element.getY()); return true; }
            if (event.isUp())    { element.setXY(element.getX(), element.getY() - amount); return true; }
            if (event.isDown())  { element.setXY(element.getX(), element.getY() + amount); return true; }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}