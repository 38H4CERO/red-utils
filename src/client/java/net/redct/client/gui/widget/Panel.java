package net.redct.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class Panel extends AbstractWidget {
    protected final List<Widget> children = new ArrayList<>();

    public Panel() {
        super(0, 0);
    }

    public void add(Widget widget) {
        children.add(widget);
        widget.setParent(this);
        recalculateLayout();
    }

    public void remove(Widget widget) {
        children.remove(widget);
        widget.setParent(null);
        recalculateLayout();
    }

    public abstract void recalculateLayout();

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        recalculateLayout();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        for (Widget child : children) {
            child.render(graphics, font, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        for (Widget child : children) {
            child.mouseDragged(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Widget child : children) {
            child.mouseReleased(mouseX, mouseY, button);
        }
    }

    // ─── THE FIX: Routing Keyboard Events to Children ───

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).charTyped(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).keyPressed(event)) {
                return true;
            }
        }
        return false;
    }
}