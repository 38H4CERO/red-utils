package net.redct.client.utils;

import net.redct.client.utils.Utils.Vec2;

public class GuiUtils {
    public static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width
                && mouseY > y && mouseY < y + height;
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return GuiUtils.contains(mouseX, mouseY, x, y, width, height);
        }

        public Rect withPadding(int pad) {
            return new Rect(x - pad, y - pad, width + pad * 2, height + pad * 2);
        }
    }

    public static Vec2 centerWindow(int screenWidth, int screenHeight, int windowWidth, int windowHeight){
        int x = (screenWidth - windowWidth) / 2;
        int y = (screenHeight - windowHeight) / 2;
        return new Vec2(x, y);
    }
}
