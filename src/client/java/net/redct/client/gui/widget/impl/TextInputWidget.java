package net.redct.client.gui.widget.impl;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.redct.client.gui.widget.AbstractWidget;
import net.redct.client.gui.config.UITheme;

import java.util.function.Consumer;
import static net.redct.client.utils.Logger.log;

public class TextInputWidget extends AbstractWidget {
    private String text = "";
    private boolean focused = false;
    private final String placeholder;
    private final Consumer<String> onEnter;

    public TextInputWidget(int width, int height, String placeholder, Consumer<String> onEnter) {
        super(width, height);
        this.placeholder = placeholder;
        this.onEnter = onEnter;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int borderColor = focused ? UITheme.BORDER_ACTIVE : UITheme.BORDER;

        graphics.fill(x, y, x + width, y + height, UITheme.SETTING_BG);
        graphics.outline(x, y, width, height, borderColor);

        String display = text.isEmpty() && !focused ? placeholder : text;
        int textColor = text.isEmpty() && !focused ? UITheme.TEXT_SECONDARY : UITheme.TEXT_PRIMARY;

        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            display += "_";
        }

        graphics.text(font, display, x + 4, y + (height - 9) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = isHovered(mouseX, mouseY);
        return focused;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!focused) return false;

        int chr = event.codepoint();
        if (chr >= 32 && chr != 127) {
            // FIX: Append the string version of the codepoint, not the raw integer!
            text += event.codepointAsString();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!focused) return false;

        // Use the native InputConstants you provided
        if (event.key() == InputConstants.KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }

        // Use the native isConfirmation (handles both Enter and Numpad Enter)
        if (event.isConfirmation() && !text.isEmpty()) {
            onEnter.accept(text);
            focused = false;
            return true;
        }
        return false;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isFocused() { return focused; }
    public void setFocused(boolean focused) { this.focused = focused; }
}