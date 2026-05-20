package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.config.ColorSetting;
import net.redct.client.config.Setting;
import net.redct.client.config.SliderSetting;
import net.redct.client.config.ToggleSetting;
import net.redct.client.gui.widget.Panel;
import net.redct.client.gui.widget.Widget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;
import net.redct.client.module.Module;
import net.redct.client.utils.GuiUtils;

import java.util.function.Consumer;

public class ModuleWidget extends Panel {
    private final Module module;

    public ModuleWidget(Module module) {
        this.module = module;
        this.width = UILayout.FRAME_WIDTH;
        this.height = UILayout.MODULE_HEIGHT;

        // Populate this module's settings as child widgets
        for (Setting setting : module.getSettings()) {
            if (setting instanceof ToggleSetting toggle) {
                add(new ToggleWidget(toggle));
            } else if (setting instanceof SliderSetting slider) {
                add(new SliderWidget(slider));
            } else if (setting instanceof ColorSetting color) {
                add(new ColorWidget(color));
            }
        }
    }

    @Override
    public void layout() {
        if (!module.isExpanded()) {
            this.height = UILayout.MODULE_HEIGHT;
            return;
        }

        // Stack visible settings vertically below the main module bar
        int currentY = this.y + UILayout.MODULE_HEIGHT;
        for (Widget child : children) {
            if (!isChildVisible(child)) continue;

            // Indent settings slightly from the left of the frame
            int indentX = this.x + UILayout.SETTING_X_OFFSET;
            child.setPosition(indentX, currentY);
            currentY += child.getHeight();
        }

        this.height = currentY - this.y;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        // 1. Render the main module button bar
        boolean hoveringBar = GuiUtils.contains(mouseX, mouseY, x, y, width, UILayout.MODULE_HEIGHT);
        int bgColor = module.isEnabled() ? UITheme.MODULE_ENABLED : (hoveringBar ? UITheme.MODULE_BG_HOVER : UITheme.MODULE_BG);

        graphics.fill(x, y, x + width, y + UILayout.MODULE_HEIGHT, bgColor);

        int textColor = module.isEnabled() ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY;
        graphics.text(font, module.getName(), x + UILayout.TEXT_X_OFFSET, y + UILayout.TEXT_Y_OFFSET, textColor);

        // 2. Render the child setting widgets only if expanded
        if (module.isExpanded()) {
            for (Widget child : children) {
                if (isChildVisible(child)) {
                    child.render(graphics, font, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Left click toggles module, Right click expands module settings
        if (GuiUtils.contains(mouseX, mouseY, x, y, width, UILayout.MODULE_HEIGHT)) {
            if (button == 0) {
                module.toggle();
                return true;
            } else if (button == 1) {
                module.toggleExpanded();
                layout();
                revalidate();
                return true;
            }
        }

        // Forward click down to the settings if expanded
        if (module.isExpanded()) {
            for (int i = children.size() - 1; i >= 0; i--) {
                Widget child = children.get(i);
                if (isChildVisible(child) && child.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Helper to check if the setting represented by a child widget is visible
    private boolean isChildVisible(Widget child) {
        if (child instanceof ToggleWidget t) return t.getSetting().isVisible();
        if (child instanceof SliderWidget s) return s.getSetting().isVisible();
        if (child instanceof ColorWidget c) return c.getSetting().isVisible();
        return true;
    }

    // Exposed getter for ModuleWidget checks
    public Module getModule() {
        return module;
    }
}