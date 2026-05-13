package net.redct.client.gui.config;

import net.minecraft.client.gui.Font;
import net.redct.client.config.Setting;
import net.redct.client.config.SliderSetting;
import net.redct.client.config.ToggleSetting;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class Frame {
    public int x, y, width, height;
    public int moduleHeight = 16;
    public int dragX, dragY;
    public Category category;
    public boolean isDragging;

    private final List<Module> modules;

    public Frame(Category category, int x, int y){
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 18; // The height of the header bar
        this.modules = ModuleManager.getByCategory(category);
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float a) {
        // 1. Dragging math
        if (isDragging) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
        }

        // 2. Draw Header
        graphics.fill(x, y, x + width, y + height, 0xFF222222);
        graphics.text(font, category.name(), x + 4, y + 4, 0xFFFFFFFF);

        // 3. Draw Modules
        int moduleY = this.y + this.height;
        for (Module module : modules) {
            boolean hovering = isHovering(mouseX, mouseY, x, moduleY, width, moduleHeight);

            // Background color logic
            int bgColor = module.isEnabled() ? 0xFF2E7D32 : (hovering ? 0xFF333333 : 0xFF1A1A1A);

            graphics.fill(x, moduleY, x + width, moduleY + moduleHeight, bgColor);

            // Text color logic
            int textColor = module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA;
            graphics.text(font, module.getName(), x + 4, moduleY + 4, textColor);

            if (module.isExpanded()) {
                for (Setting setting : module.getSettings()) {
                    moduleY += moduleHeight;

                    if (setting instanceof ToggleSetting toggle) {
                        int bg = toggle.getValue() ? 0xFF1B5E20 : 0xFF111111;
                        graphics.fill(x + 4, moduleY, x + width, moduleY + moduleHeight, bg);
                        graphics.text(font, setting.getName(), x + 8, moduleY + 4, 0xFFCCCCCC);

                    } else if (setting instanceof SliderSetting slider) {
                        graphics.fill(x + 4, moduleY, x + width, moduleY + moduleHeight, 0xFF111111);
                        // Draw filled portion
                        int filledWidth = (int) ((slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin()) * (width - 4));
                        graphics.fill(x + 4, moduleY, x + 4 + filledWidth, moduleY + moduleHeight, 0xFF1565C0);
                        graphics.text(font, setting.getName() + ": " + (int) slider.getValue(), x + 8, moduleY + 4, 0xFFCCCCCC);
                    }
                }
            }
            moduleY += moduleHeight;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. Check Header Dragging
        if (isHovering(mouseX, mouseY, x, y, width, height) && button == 0) {
            this.isDragging = true;
            this.dragX = (int) (mouseX - this.x);
            this.dragY = (int) (mouseY - this.y);
            return true;
        }

        int moduleY = this.y + this.height;
        for (Module module : modules) {
            // 2. Check Module Clicks
            if (isHovering(mouseX, mouseY, x, moduleY, width, moduleHeight)) {
                if (button == 0) {
                    module.toggle(); // Left click = Toggle
                } else if (button == 1) {
                    module.toggleExpanded(); // Right click = Expand Settings
                }
                return true;
            }

            // 3. Check Setting Clicks (if expanded)
            if (module.isExpanded()) {
                for (Setting setting : module.getSettings()) {
                    moduleY += moduleHeight; // Move down for the setting

                    if (isHovering(mouseX, mouseY, x, moduleY, width, moduleHeight)) {
                        if (button == 0) {
                            if (setting instanceof ToggleSetting toggle) {
                                toggle.setValue(!toggle.getValue()); // Or toggle.toggle() if you have that method
                                return true;
                            } else if (setting instanceof SliderSetting slider) {
                                // Slider logic goes here!
                                return true;
                            }
                        }
                    }
                }
            }
            moduleY += moduleHeight; // Move down for the next module
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
        }
    }

    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
