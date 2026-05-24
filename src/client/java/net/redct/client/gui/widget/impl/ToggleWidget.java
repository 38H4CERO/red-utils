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

        // 1. Draw flat setting background
        graphics.fill(x, y, x + width, y + height, UITheme.SETTING_BG);
        graphics.text(font, setting.getName(), x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_SETTING);

        // 2. The Modern Switch Geometry using UILayout
        int switchX = x + width - UILayout.TOGGLE_WIDTH - 4;
        int switchY = y + (height - UILayout.TOGGLE_HEIGHT) / 2;

        int switchBgColor = setting.getValue() ? UITheme.TOGGLE_ON : UITheme.TOGGLE_OFF_BG;
        graphics.fill(switchX, switchY, switchX + UILayout.TOGGLE_WIDTH, switchY + UILayout.TOGGLE_HEIGHT, switchBgColor);

        // 3. The "Knob" (White square that moves)
        // You can also move knobSize to UILayout if you want to be extremely thorough,
        // but deriving it dynamically like this guarantees it always fits inside the toggle height!
        int knobSize = UILayout.TOGGLE_HEIGHT - 2;

        int knobX = setting.getValue() ? (switchX + UILayout.TOGGLE_WIDTH - knobSize - 1) : (switchX + 1);
        int knobY = switchY + 1;

        graphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
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