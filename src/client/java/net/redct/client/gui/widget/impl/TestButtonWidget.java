package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;

public class TestButtonWidget extends AbstractWidget {
    private boolean state = false;
    private final String text;

    public TestButtonWidget(String text, int width, int height) {
        super(width, height);
        this.text = text;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        // Change color depending on if it is toggled or hovered
        int bg;
        if (state) {
            bg = UITheme.MODULE_ENABLED; // Green
        } else {
            bg = isHovered(mouseX, mouseY) ? UITheme.MODULE_BG_HOVER : UITheme.MODULE_BG; // Dark grey
        }

        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, UITheme.BORDER);

        // Draw the text inside the button
        graphics.text(font, text, x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            state = !state;
            return true;
        }
        return false;
    }
}