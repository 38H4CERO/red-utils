package net.redct.client.gui.widget.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ColorSetting;
import net.redct.client.gui.hud.impl.ColorPickerPopup;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.widget.RootPanel;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;
import net.redct.client.utils.Utils;

import static net.redct.client.utils.GuiUtils.centerWindow;

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
            int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            // 1. Create the plain ColorPickerPopup
            ColorPickerPopup picker = new ColorPickerPopup(setting);

            // 2. Wrap it inside the draggable WindowWidget at 0,0 first
            // so it can initialize its layout and calculate its total width/height
            WindowWidget draggableWindow = new WindowWidget("Color Picker", 0, 0, picker);

            // 3. Calculate the center using the WINDOW'S dimensions, not the ColorWidget's dimensions
            Utils.Vec2 pos = centerWindow(screenW, screenH, draggableWindow.getWidth(), draggableWindow.getHeight());

            // 4. Update the window's position before pushing it to the overlay stack
            draggableWindow.setPosition(pos.x(), pos.y());

            RootPanel.getInstance().addOverlay(draggableWindow);
            return true;
        }
        return false;
    }

    public ColorSetting getSetting() { return setting; }
}