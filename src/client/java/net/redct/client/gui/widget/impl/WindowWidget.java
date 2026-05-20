package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.gui.widget.Panel;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;
import net.redct.client.utils.GuiUtils;

public class WindowWidget extends Panel {
    private final String title;
    private final Panel content;

    // Dragging state
    private boolean isDragging = false;
    private int dragX, dragY;

    public WindowWidget(String title, int x, int y, Panel content) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.content = content;
        this.width = UILayout.FRAME_WIDTH;
        this.height = UILayout.FRAME_HEADER_HEIGHT;

        if (this.content != null) {
            this.content.setParent(this);
        }

        layout(); // Position the content initially
    }

    @Override
    public void layout() {
        if (content != null) {
            // Force the content VPanel to realign its children first
            content.layout();

            // Now position the content directly below the window header
            content.setPosition(this.x, this.y + UILayout.FRAME_HEADER_HEIGHT);

            // Calculate height using the FRESH, updated content height
            this.height = UILayout.FRAME_HEADER_HEIGHT + content.getHeight();

            // Adapt window width to content if content specifies a custom width
            this.width = content.getWidth() > 0 ? content.getWidth() : UILayout.FRAME_WIDTH;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        // 1. Process dragging logic
        if (isDragging) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
            layout(); // Instantly update positions of content below the header
        }

        // 2. Render Window Header (Title Bar)
        graphics.fill(x, y, x + width, y + UILayout.FRAME_HEADER_HEIGHT, UITheme.FRAME_BG);
        graphics.text(font, title, x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_PRIMARY);

        // 3. Render content panel
        if (content != null) {
            content.render(graphics, font, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Left clicking the header starts the dragging process
        if (GuiUtils.contains(mouseX, mouseY, x, y, width, UILayout.FRAME_HEADER_HEIGHT)) {
            if (button == 0) {
                this.isDragging = true;
                this.dragX = (int) (mouseX - this.x);
                this.dragY = (int) (mouseY - this.y);
                return true;
            }
        }

        // Clicks below the header are forwarded to the content panel
        return content != null && content.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (content != null) {
            content.mouseDragged(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false; // Stop dragging
        }
        if (content != null) {
            content.mouseReleased(mouseX, mouseY, button);
        }
    }

    public Panel getContent() {
        return content;
    }
}