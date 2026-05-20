package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.SliderSetting;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;

public class SliderWidget extends AbstractWidget {
    private final SliderSetting setting;
    private boolean isDragging = false;

    public SliderWidget(SliderSetting setting) {
        super(UILayout.FRAME_WIDTH - UILayout.SETTING_X_OFFSET, UILayout.SETTING_HEIGHT);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        graphics.fill(x, y, x + width, y + height, UITheme.SETTING_BG);

        double percent = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int filledWidth = (int) (percent * width);

        graphics.fill(x, y, x + filledWidth, y + height, UITheme.SLIDER_FILL);

        String text = setting.getName() + ": " + (int) setting.getValue();
        graphics.text(font, text, x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_SETTING);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (setting.isVisible() && isHovered(mouseX, mouseY) && button == 0) {
            this.isDragging = true;
            setting.setDragging(true); // Synchronize with your backend flag!
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (isDragging && button == 0) {
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
            setting.setDragging(false); // Synchronize with your backend flag!
        }
    }

    private void updateValue(double mouseX) {
        double percent = (mouseX - this.x) / (double) this.width;
        percent = Math.clamp(percent, 0.0, 1.0);
        double newValue = setting.getMin() + (percent * (setting.getMax() - setting.getMin()));
        setting.setValue(newValue);
    }

    public SliderSetting getSetting() {
        return setting;
    }
}