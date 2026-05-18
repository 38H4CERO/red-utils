package net.redct.client.gui.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.KeyEvent;
import net.redct.client.config.ColorSetting;
import net.redct.client.config.Setting;
import net.redct.client.config.SliderSetting;
import net.redct.client.config.ToggleSetting;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.redct.client.utils.GuiUtils;

import java.util.List;
import java.util.function.Consumer;

public class Frame {
    public int x, y, width, height;
    public int moduleHeight = 16;
    public int dragX, dragY;
    public Category category;
    public boolean isDragging;
    private SliderSetting activeSlider = null;
    private SliderSetting lastSlider = null;

    private final List<Module> modules;
    private final Consumer<ColorSetting> onColorPickerOpen;

    public Frame(Category category, int x, int y, Consumer<ColorSetting> onColorPickerOpen) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 18;
        this.modules = ModuleManager.getByCategory(category);
        this.onColorPickerOpen = onColorPickerOpen;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float a) {
        if (isDragging) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
        }

        graphics.fill(x, y, x + width, y + height, 0xFF222222);
        graphics.text(font, category.name(), x + 4, y + 4, 0xFFFFFFFF);

        int moduleY = this.y + this.height;
        for (Module module : modules) {
            boolean hovering = GuiUtils.contains(mouseX, mouseY, x, moduleY, width, moduleHeight);
            int bgColor = module.isEnabled() ? 0xFF2E7D32 : (hovering ? 0xFF333333 : 0xFF1A1A1A);
            graphics.fill(x, moduleY, x + width, moduleY + moduleHeight, bgColor);
            int textColor = module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA;
            graphics.text(font, module.getName(), x + 4, moduleY + 4, textColor);

            if (module.isExpanded()) {
                for (Setting setting : module.getSettings()) {
                    if (!setting.isVisible()) continue;
                    moduleY += moduleHeight;

                    if (setting instanceof ToggleSetting toggle) {
                        int bg = toggle.getValue() ? 0xFF1B5E20 : 0xFF111111;
                        graphics.fill(x + 4, moduleY, x + width, moduleY + moduleHeight, bg);
                        graphics.text(font, setting.getName(), x + 8, moduleY + 4, 0xFFCCCCCC);

                    } else if (setting instanceof SliderSetting slider) {
                        graphics.fill(x + 4, moduleY, x + width, moduleY + moduleHeight, 0xFF111111);
                        int filledWidth = (int) ((slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin()) * (width - 4));
                        graphics.fill(x + 4, moduleY, x + 4 + filledWidth, moduleY + moduleHeight, 0xFF1565C0);
                        graphics.text(font, setting.getName() + ": " + (int) slider.getValue(), x + 8, moduleY + 4, 0xFFCCCCCC);

                    } else if (setting instanceof ColorSetting colorSetting) {
                        graphics.fill(x + 4, moduleY, x + width, moduleY + moduleHeight, 0xFF111111);
                        graphics.text(font, setting.getName(), x + 8, moduleY + 4, 0xFFCCCCCC);
                        // Color preview box on the right
                        graphics.fill(x + width - 14, moduleY + 2, x + width - 2, moduleY + moduleHeight - 2, colorSetting.getColor());
                        graphics.outline(x + width - 14, moduleY + 2, 12, moduleHeight - 4, 0xFF444444);
                    }
                }
            }
            moduleY += moduleHeight;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (GuiUtils.contains(mouseX, mouseY, x, y, width, height) && button == 0) {
            this.isDragging = true;
            this.dragX = (int) (mouseX - this.x);
            this.dragY = (int) (mouseY - this.y);
            return true;
        }

        int moduleY = this.y + this.height;
        for (Module module : modules) {
            if (GuiUtils.contains(mouseX, mouseY, x, moduleY, width, moduleHeight)) {
                if (button == 0) module.toggle();
                if (button == 1) module.toggleExpanded();
                return true;
            }

            if (module.isExpanded()) {
                for (Setting setting : module.getSettings()) {
                    if (!setting.isVisible()) continue;
                    moduleY += moduleHeight;

                    if (GuiUtils.contains(mouseX, mouseY, x, moduleY, width, moduleHeight)) {
                        if (button == 0) {
                            if (setting instanceof ToggleSetting toggle) {
                                toggle.setValue(!toggle.getValue());
                                return true;

                            } else if (setting instanceof SliderSetting slider) {
                                activeSlider = slider;
                                lastSlider = slider;
                                updateSliderMath(slider, mouseX);
                                return true;

                            } else if (setting instanceof ColorSetting colorSetting) {
                                onColorPickerOpen.accept(colorSetting);
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
        if (activeSlider != null) {
            updateSliderMath(activeSlider, mouseX);
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
            this.isDragging = false;
            activeSlider = null;
        }
    }

    // TODO: Habria que moverlo a ClickGui sino mueves en varios frames a la vez
    public boolean keyPressed(KeyEvent event) {
        if (lastSlider == null) return false;
        double amount = event.hasShiftDown() ? 1.0 : 0.1;
        if (event.isLeft())  { lastSlider.setValue(lastSlider.getValue() - amount); return true; }
        if (event.isRight()) { lastSlider.setValue(lastSlider.getValue() + amount); return true; }
        return false;
    }
}