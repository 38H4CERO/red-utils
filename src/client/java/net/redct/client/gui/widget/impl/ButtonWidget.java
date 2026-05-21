package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;
import net.redct.client.utils.GuiUtils;

public class ButtonWidget extends AbstractWidget {
    private final String text;
    private final Runnable onClick;

    public ButtonWidget(String text, int width, int height, Runnable onClick) {
        super(width, height);
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int bg = isHovered(mouseX, mouseY) ? UITheme.MODULE_BG_HOVER : UITheme.MODULE_BG;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, UITheme.BORDER);

        // Render text centered inside the button
        int textW = font.width(text);
        int textX = x + (width - textW) / 2;
        int textY = y + (height - 9) / 2; // Minecraft font height is always 9

        graphics.text(font, text, textX, textY, UITheme.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            onClick.run();
            return true;
        }
        return false;
    }
}