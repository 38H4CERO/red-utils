package net.redct.client.gui.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.redct.client.module.Category;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private List<Frame> frames = new ArrayList<>();

    public ClickGuiScreen() {
        super(Component.literal("Red Utils"));

        frames.add(new Frame(Category.DUNGEONS, 20, 20));
        frames.add(new Frame(Category.KUUDRA, 140, 20));
        frames.add(new Frame(Category.RENDER, 260, 20));
        frames.add(new Frame(Category.MISC, 380, 20));
    }

    @Override
    public void extractRenderState( GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Draw the dark background
        super.extractRenderState(graphics, mouseX, mouseY, a);

        for (Frame frame : frames) {
            // Pass the graphics, the font, and the mouse data!
            frame.extractRenderState(graphics, this.font, mouseX, mouseY, a);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (int i = frames.size() - 1; i >= 0; i--) {
            Frame frame = frames.get(i);
            if (frame.mouseClicked(event.x(), event.y(), event.button())){
                frames.remove(i);
                frames.add(frame);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);

    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        // 1. Extract the raw numbers from the new event object
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        // 2. Pass those numbers down to your custom frames so they stop dragging
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }

        // 3. Let Minecraft run that default code you pasted to handle normal widgets
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {

        // 1. Extract the raw numbers from the new Mojang event object
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        for (Frame frame : frames) {
            // 2. Forward the extracted numbers to your custom frames!
            frame.mouseDragged(mouseX, mouseY, button);
        }

        // 3. Pass the new event object back to the superclass
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean isInGameUi() {
        return true; // Keeps the background dark
    }
}