package net.redct.client.gui.config;

import net.minecraft.client.gui.Font;
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

            moduleY += 16;
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
            if (isHovering(mouseX, mouseY, x, moduleY, width, moduleHeight) && button == 0) {
                module.toggle();
                return true;
            }
            moduleY += moduleHeight;
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
