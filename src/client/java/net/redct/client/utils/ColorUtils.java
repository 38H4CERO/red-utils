package net.redct.client.utils;

public class ColorUtils {

    /**
     *
     * @param RGB 0xFFFFFF white
     * @param alpha 0-255, 0xFF for opaque
     */
    public static int makeARGB(int RGB, int alpha) {
        return ((alpha & 0xFF) << 24) | (RGB & 0x00FFFFFF);
    }

    public static String colorToHex(int color) {
        return String.format("0x%08X", color);
    }

    public static int hexToColor(String hex) {
        String clean = hex.replace("0x", "").replace("#", "");
        return (int) Long.parseLong(clean, 16);
    }
}
