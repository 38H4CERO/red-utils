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
        if (isHovering(mouseX, mouseY, x, y, width, height) && button == 0) {
            this.isDragging = true;
            this.dragX = (int) (mouseX - this.x);
            this.dragY = (int) (mouseY - this.y);
            return true;
        }

        int moduleY = this.y + this.height;
        for (Module module : modules) {
            if (isHovering(mouseX, mouseY, x, moduleY, width, moduleHeight)) {
                if (button == 0) module.toggle();           // Left Click
                if (button == 1) module.toggleExpanded();   // Right Click
                return true;
            }

            if (module.isExpanded()) {
                for (Setting setting : module.getSettings()) {
                    moduleY += moduleHeight;

                    if (isHovering(mouseX, mouseY, x, moduleY, width, moduleHeight)) {
                        if (button == 0) {
                            if (setting instanceof ToggleSetting toggle) {
                                toggle.setValue(!toggle.getValue());
                                return true;

                            } else if (setting instanceof SliderSetting slider) {
                                // 1. Start the drag!
                                slider.setDragging(true);
                                // 2. Instantly update the value to where the user clicked
                                updateSliderMath(slider, mouseX);
                                return true;
                            }
                        }
                    }
                }
            }
            moduleY += moduleHeight;
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
        for (Module module : modules) {
            if (module.isExpanded()) {
                for (Setting setting : module.getSettings()) {
                    // If a slider is currently being dragged, aggressively update its value
                    if (setting instanceof SliderSetting slider && slider.isDragging()) {
                        updateSliderMath(slider, mouseX);
                    }
                }
            }
        }
    }

    private void updateSliderMath(SliderSetting slider, double mouseX) {
        double percent = (mouseX - (this.x + 4)) / (double) (this.width - 4);
        percent = Math.clamp(percent, 0.0, 1.0);
        double newValue = slider.getMin() + (percent * (slider.getMax() - slider.getMin()));
        slider.setValue(newValue);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false; // Stops frame dragging

            // Turn off dragging for ALL sliders inside this frame
            for (Module module : modules) {
                if (module.isExpanded()) {
                    for (Setting setting : module.getSettings()) {
                        if (setting instanceof SliderSetting slider) {
                            slider.setDragging(false);
                        }
                    }
                }
            }
        }
    }

    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
