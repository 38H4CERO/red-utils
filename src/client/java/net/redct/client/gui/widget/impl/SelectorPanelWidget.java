package net.redct.client.gui.widget.impl;

import net.redct.client.gui.widget.Panel;
import net.redct.client.gui.widget.Widget;
import net.redct.client.gui.config.UILayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectorPanelWidget extends Panel {
    private final Supplier<List<String>> listSupplier;
    private final Supplier<String> activeSupplier;
    private final Consumer<String> onSelect;

    public SelectorPanelWidget(Supplier<List<String>> listSupplier, Supplier<String> activeSupplier, Consumer<String> onSelect) {
        this.listSupplier = listSupplier;
        this.activeSupplier = activeSupplier;
        this.onSelect = onSelect;
        this.width = UILayout.FRAME_WIDTH;

        updateList(); // Initial list generation
    }

    // Dynamic list re-builder
    public void updateList() {
        children.clear(); // Safely clear old children without triggering layout loops

        List<String> items = listSupplier.get();
        String activeItem = activeSupplier.get();

        for (String item : items) {
            boolean isActive = item.equalsIgnoreCase(activeItem);

            // Add directly to children list to bypass layout recursion
            children.add(new SelectorItemWidget(item, isActive, selected -> {
                onSelect.accept(selected); // Trigger selection callback
                updateList();             // Re-fetch and highlight the new active item!
            }));
        }

        layout(); // Trigger vertical alignment once
    }

    @Override
    public void layout() {
        int currentY = this.y;
        for (Widget child : children) {
            child.setPosition(this.x, currentY);
            currentY += child.getHeight();
        }
        this.height = currentY - this.y;
    }
}
