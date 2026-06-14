package net.redct.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.sounds.SoundEvents;

import java.util.regex.Pattern;

public class Utils {
    public static boolean inHypixel = false;

    public static boolean isOnHypixel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        ServerData server = mc.getCurrentServer();
        if (server == null) return false;

        inHypixel = server.ip.endsWith(".hypixel.net");
        return inHypixel;
    }
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§.");

    //[19:33:24] [ENTITY] type=armor_stand | name=Wither Key | customName=Wither Key | pos=[-123.0,68.6,-82.0]
    public static String trimFormatedText(String rawText){
        if (rawText == null) return "";

        StringBuilder sb = new StringBuilder(rawText.length());
        int len = rawText.length();

        for (int i = 0; i < len; i++) {
            char c = rawText.charAt(i);
            if (c == '§') {
                i++; // skip the next char (the color code)
            } else {
                sb.append(c);
            }
        }

        return sb.toString().trim();
    }


    /*
      mc.player.playSound(SoundEvents.GHAST_SCREAM, volume, 0.75F);
      mc.player.playSound(SoundEvents.ARROW_HIT_PLAYER, volume, 1.5F);
      mc.player.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, volume, pitch);
     */
    public static void playLocalClientSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            //mc.player.playSound(SoundEvents.PISTON_CONTRACT, 1.0F, 1.2F);
            //mc.player.playSound(SoundEvents.WOOD_PLACE, 0.9F, 1.2F);
        }
    }

    public record Vec2(int x, int y) {
        @Override
        public String toString() {
            return String.format("[%d,%d]", x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Vec2 p)) {
                return false;
            }
            return (this.x == p.x && this.y == p.y);
        }
    }

}
