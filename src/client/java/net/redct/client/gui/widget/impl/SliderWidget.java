package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.SliderSetting;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;

public class SliderWidget extends AbstractWidget {
    private final SliderSetting setting;
    private String cachedValueString; // Prevents string allocation in render loop

    public SliderWidget(SliderSetting setting) {
        super(UILayout.FRAME_WIDTH - UILayout.SETTING_X_OFFSET, UILayout.SETTING_HEIGHT);
        this.setting = setting;
        updateCache();
    }

    private void updateCache() {
        // Format to 1 decimal place (or drop the decimal if you prefer integers)
        this.cachedValueString = String.format("%.1f", setting.getValue());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        graphics.fill(x, y, x + width, y + height, UITheme.SETTING_BG);

        // Name on the left, Value on the right
        graphics.text(font, setting.getName(), x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_SETTING);
        int valW = font.width(cachedValueString);
        graphics.text(font, cachedValueString, x + width - valW - 4, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_PRIMARY);

        // The Minimalist Track (2px tall at the bottom)
        int trackHeight = 2;
        int trackY = y + height - trackHeight;
        graphics.fill(x, trackY, x + width, trackY + trackHeight, UITheme.SLIDER_TRACK_BG);

        // The Colored Fill Line
        double percent = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int filledWidth = (int) (percent * width);
        graphics.fill(x, trackY, x + filledWidth, trackY + trackHeight, UITheme.SLIDER_FILL);

        // The Thumb / Knob
        int thumbSize = 6;
        int thumbX = x + filledWidth - (thumbSize / 2);
        // Clamp thumb so it doesn't draw outside the widget bounds
        thumbX = Math.max(x, Math.min(x + width - thumbSize, thumbX));
        int thumbY = trackY - (thumbSize / 2) + (trackHeight / 2);
        graphics.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (setting.isVisible() && isHovered(mouseX, mouseY) && button == 0) {
            setting.setDragging(true);
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (setting.isDragging() && button == 0) updateValue(mouseX);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            setting.setDragging(false);
        }
    }

    private void updateValue(double mouseX) {
        double percent = (mouseX - this.x) / (double) this.width;
        percent = Math.clamp(percent, 0.0, 1.0);
        double newValue = setting.getMin() + (percent * (setting.getMax() - setting.getMin()));
        setting.setValue(newValue);
        updateCache(); // Update string cache only when value changes!
    }

    public SliderSetting getSetting() { return setting; }
}