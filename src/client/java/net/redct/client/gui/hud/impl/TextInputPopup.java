package net.redct.client.gui.hud.impl;

import net.redct.client.gui.widget.AbsPanel;
import net.redct.client.gui.widget.RootPanel;
import net.redct.client.gui.widget.VPanel;
import net.redct.client.gui.widget.impl.ButtonWidget;
import net.redct.client.gui.widget.impl.TextInputWidget;

import java.util.function.Consumer;

public class TextInputPopup extends VPanel {

    public TextInputPopup(String placeholder, Consumer<String> onConfirm) {

        // 1. Create the input box
        TextInputWidget input = new TextInputWidget(120, 16, placeholder, text -> {
            onConfirm.accept(text);
            RootPanel.getInstance().overlayPop(); // Close window on Enter
        });

        // Auto-focus so the user can type immediately
        input.setFocused(true);

        // 2. Create the manual confirm button
        ButtonWidget confirmBtn = new ButtonWidget("Confirm", 120, 16, () -> {
            if (!input.getText().isEmpty()) {
                onConfirm.accept(input.getText());
                RootPanel.getInstance().overlayPop(); // Close window on Click
            }
        });

        // 3. Add to the VPanel (Panel.java handles the layout automatically!)
        this.add(input);
        this.add(confirmBtn);
    }
}