package net.redct.client.gui.widget.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ColorSetting;
import net.redct.client.gui.hud.ColorPickerPopup;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.widget.RootPanel;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;

public class ColorWidget extends AbstractWidget {
    private final ColorSetting setting;

    public ColorWidget(ColorSetting setting) {
        super(UILayout.FRAME_WIDTH - UILayout.SETTING_X_OFFSET, UILayout.SETTING_HEIGHT);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        graphics.fill(x, y, x + width, y + height, UITheme.SETTING_BG);
        graphics.text(font, setting.getName(), x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, UITheme.TEXT_SETTING);

        int boxSize = UILayout.COLOR_BOX_SIZE;
        int boxPadding = UILayout.COLOR_BOX_PADDING;
        int boxX = x + width - boxSize - boxPadding;
        int boxY = y + boxPadding;
        int boxHeight = height - (boxPadding * 2);

        graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxHeight, setting.getColor());
        graphics.outline(boxX, boxY, boxSize, boxHeight, UITheme.BORDER);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (setting.isVisible() && isHovered(mouseX, mouseY) && button == 0) {
            // Get screen coordinates to center the popup
            int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            // Create your color picker popup (we will make ColorPickerPopup implement Widget next)
            RootPanel.getInstance().addOverlay(new ColorPickerPopup(setting, screenW / 2, screenH / 2));
            return true;
        }
        return false;
    }

    public ColorSetting getSetting() { return setting; }
}