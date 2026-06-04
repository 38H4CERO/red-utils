package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.gui.widget.Panel;
import net.redct.client.gui.widget.Widget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;
import net.redct.client.utils.GuiUtils;

public class WindowWidget extends Panel {
    private final String title;
    private final Widget content;

    private boolean isDragging = false;
    private int dragX, dragY;

    public WindowWidget(String title, int x, int y, Widget content) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.content = content;
        this.width = UILayout.FRAME_WIDTH;
        this.height = UILayout.FRAME_HEADER_HEIGHT;

        if (this.content != null) {
            this.add(this.content);
        }

        recalculateLayout();
    }

    @Override
    public void recalculateLayout() {
        if (content != null) {
            if (content instanceof Panel panel) panel.recalculateLayout();
            content.setPosition(this.x, this.y + UILayout.FRAME_HEADER_HEIGHT);
            this.height = UILayout.FRAME_HEADER_HEIGHT + content.getHeight();
            this.width = content.getWidth() > 0 ? content.getWidth() : UILayout.FRAME_WIDTH;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (isDragging) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
            recalculateLayout(); // Move children with the window
        }

        // Draw Window Header
        graphics.fill(x, y, x + width, y + UILayout.FRAME_HEADER_HEIGHT, UITheme.FRAME_BG);
        graphics.text(font, title, x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_PRIMARY);

        // THE FIX: Let Panel handle drawing the children!
        super.render(graphics, font, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. Check if the user clicked the header to drag
        if (GuiUtils.contains(mouseX, mouseY, x, y, width, UILayout.FRAME_HEADER_HEIGHT)) {
            if (button == 0) {
                this.isDragging = true;
                this.dragX = (int) (mouseX - this.x);
                this.dragY = (int) (mouseY - this.y);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) this.isDragging = false;
        super.mouseReleased(mouseX, mouseY, button);
    }
}