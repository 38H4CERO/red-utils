package net.redct.client.utils.entity;

import net.minecraft.world.entity.Entity;
import net.redct.client.utils.Logger;
import net.redct.client.utils.dungeon.DungeonSession;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.redct.client.utils.entity.EntityManager.onNameResolved;

public class EntityUtils {
    //private static final String regex = "\\[Lv(?<level>\\d+)\\]\\s+(?:\\p{Co}+\\s*)?(?<name>[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+(?<current>[\\d,]+)/(?<max>[\\d,]+)❤";

    // TODO: maybe separar dungeons o no
    private static final String regex = (
            "(?:\\[Lv\\d+\\]\\s+)?" +                               // Lvl [Lv 500]
            "(?<icons>\\p{Co}+)?\\s*" +                             // MobTypes
            "(?<star>✯)?\\s*" +                                     // Dungeon Star
            "(?:Flaming|Stormy|Speedy|Fortified|Healthy)?\\s*" +    // Dungeon modifier
            "(?<name>[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+" +            // Mob name
            "(?<current>[\\d,.]+[kM]?)" +                           // Current HP
            "(?:/(?<max>[\\d,.]+[kM]?))?" +                         // Max HP (En dungeons unos no tienen)
            "❤"
    );

    private static final Pattern MOB_NAME_PATTERN = Pattern.compile(regex);


    public static String mobNameParse(String custonName){
        Matcher match = MOB_NAME_PATTERN.matcher(custonName);
        if (match.find()) {
            String name = match.group("name");
            String icon = match.group("icons");

            StringBuilder iconsHex = new StringBuilder();
            icon.codePoints().forEach(codepoint -> {
                if (!iconsHex.isEmpty()) iconsHex.append(' ');
                iconsHex.append("U+").append(Integer.toHexString(codepoint).toUpperCase());
            });

            Logger.log("MOB", "%s, %s", name, iconsHex);
            return name;
        }
        //Logger.log("REGEX", "%s", custonName);
        //throw new RuntimeException();
        return null;
    }

    public static void rescanLoadedArmorStands() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) return;
        for (Entity entity : level.entitiesForRendering()) {
            if (entity.getType().toShortString().equals("armor_stand") && entity.hasCustomName()) {
                onNameResolved(entity);
            }
        }
    }


}
