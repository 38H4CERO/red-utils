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

    // Only call this when files are actually added or removed
    public void updateList() {
        children.clear();
        List<String> items = listSupplier.get();
        String activeItem = activeSupplier.get();

        for (String item : items) {
            boolean isActive = item.equalsIgnoreCase(activeItem);

            children.add(new SelectorItemWidget(item, isActive, selected -> {
                onSelect.accept(selected); // Tell the backend the selection changed
                updateVisualSelection(selected); // Zero-allocation UI update!
            }));
        }

        revalidate(); // Bubble up layout to parent WindowWidget in case list height changed
    }

    // Zero-allocation method to update highlights
    public void updateVisualSelection(String newActiveItem) {
        for (Widget child : children) {
            if (child instanceof SelectorItemWidget itemWidget) {
                itemWidget.setActive(itemWidget.getName().equalsIgnoreCase(newActiveItem));
            }
        }
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