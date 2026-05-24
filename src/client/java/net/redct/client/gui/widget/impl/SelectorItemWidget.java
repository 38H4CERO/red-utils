package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;
import java.util.function.Consumer;

public class SelectorItemWidget extends AbstractWidget {
    private final String name;
    private boolean isActive;
    private final Consumer<String> onClick;

    public SelectorItemWidget(String name, boolean isActive, Consumer<String> onClick) {
        super(UILayout.FRAME_WIDTH, UILayout.MODULE_HEIGHT);
        this.name = name;
        this.isActive = isActive;
        this.onClick = onClick;
    }

    public String getName() {
        return this.name;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int bg = isActive ? UITheme.MODULE_ENABLED : (isHovered(mouseX, mouseY) ? UITheme.MODULE_BG_HOVER : UITheme.SETTING_BG);

        graphics.fill(x, y, x + width, y + height, bg);
        graphics.text(font, name, x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            onClick.accept(name);
            return true;
        }
        return false;
    }
}