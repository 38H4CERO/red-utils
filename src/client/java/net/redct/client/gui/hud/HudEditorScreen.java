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
    private HudInterface lastElement = null;
    private final int BORDER_PADDING = 3;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private final GuiTextUtils rectPosition = new GuiTextUtils("rectPosition");

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    public void init() {
        HudManager.setEditorOpen(true);
        rectPosition.setColor(0xC0C0C0, 0xFF);
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

        // Little coordinates below gui
        // TODO: view why is BORDER_PADDING + 1
        if (lastElement != null) {
            String info = String.format("[%d, %d] x%.1f",
                    lastElement.getX() - BORDER_PADDING, lastElement.getY() - BORDER_PADDING + 1, lastElement.getScale());
            rectPosition.setText(info);
            // below the border
            int textX = lastElement.getX();
            int textY = lastElement.getY() + lastElement.getHeight() + 4;
            rectPosition.setXY(textX, textY);
            rectPosition.render(graphics);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Loop BACKWARDS so if two elements overlap, you grab the one on top!
        List<HudInterface> elements = HudManager.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudInterface element = elements.get(i);

            if (element.getRect().withPadding(BORDER_PADDING).contains(event.x(), event.y())) {
                HudManager.bringToFront(element); // bring to front
                this.element = element;
                this.lastElement = element;
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
        int x = element.getX() - BORDER_PADDING + 1;
        int y = element.getY() - BORDER_PADDING + 1 ;
        int w = element.getWidth() + BORDER_PADDING + 1;
        int h = element.getHeight() + BORDER_PADDING + 1;

        graphics.outline(x, y, w, h, color);
        /*
        // Top line
        graphics.fill(x + 1, y, x + w, y + 1, color);
        // Bottom line
        graphics.fill(x, y + h, x + w + 1, y + h + 1, color);
        // Left line
        graphics.fill(x, y, x + 1, y + h, color);
        // Right line
        graphics.fill(x + w, y, x + w + 1, y + h, color);
        */
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

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (lastElement != null) {
            int amount = event.hasShiftDown() ? 10 : 1;

            if (event.isLeft())  { lastElement.setXY(lastElement.getX() - amount, lastElement.getY()); return true; }
            if (event.isRight()) { lastElement.setXY(lastElement.getX() + amount, lastElement.getY()); return true; }
            if (event.isUp())    { lastElement.setXY(lastElement.getX(), lastElement.getY() - amount); return true; }
            if (event.isDown())  { lastElement.setXY(lastElement.getX(), lastElement.getY() + amount); return true; }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}