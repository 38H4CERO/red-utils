package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ToggleSetting;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;

public class ToggleWidget extends AbstractWidget {
    private final ToggleSetting setting;

    public ToggleWidget(ToggleSetting setting) {
        super(UILayout.FRAME_WIDTH - UILayout.SETTING_X_OFFSET, UILayout.SETTING_HEIGHT);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        // Uses setting.getValue()
        int bg = setting.getValue() ? UITheme.MODULE_ENABLED : UITheme.SETTING_BG;

        graphics.fill(x, y, x + width, y + height, bg);
        graphics.text(font, setting.getName(), x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_SETTING);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (setting.isVisible() && isHovered(mouseX, mouseY) && button == 0) {
            setting.toggle();
            revalidate();
            return true;
        }
        return false;
    }

    public ToggleSetting getSetting() {
        return setting;
    }
}