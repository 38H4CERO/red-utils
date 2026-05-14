package net.redct.client.utils;

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
}
