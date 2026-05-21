package net.redct.client.gui.widget.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.config.UILayout;

public class IconButtonWidget extends AbstractWidget {
    private final Identifier texture;
    private final Runnable onClick;

    public IconButtonWidget(Identifier texture, int width, int height, Runnable onClick) {
        super(width, height);
        this.texture = texture;
        this.onClick = onClick;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        // Render button background
        int bg = isHovered(mouseX, mouseY) ? UITheme.MODULE_BG_HOVER : UITheme.MODULE_BG;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, UITheme.BORDER);

        // Calculate centered icon coordinates (with a 2-pixel inner padding)
        int iconSize = Math.min(width, height) - 4;
        int iconX = x + (width - iconSize) / 2;
        int iconY = y + (height - iconSize) / 2;

        // VERIFIED API: Using your environment's exact blit texture rendering pipeline
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                iconX,
                iconY,
                0,
                0,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            onClick.run();
            return true;
        }
        return false;
    }
}