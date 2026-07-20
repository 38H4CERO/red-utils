package net.redct.client.utils.entity;

import net.redct.client.utils.Logger;
import net.redct.client.utils.dungeon.DungeonSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityUtils {
    /*
     TODO: Fix dungeons
     - Tienen una estrella unos
     - La vida no es HP/MAXHP
     - Pueden ser Flaming, Stormy, Speedy, Fortified, or Healthy
     - Runicos...
     [17:11:31] [REGEX]  ✯ Healthy Super Tank Zombie 39,772❤

     */
    //private static final String regex = "\\[Lv(?<level>\\d+)\\]\\s+(?:\\p{Co}+\\s*)?(?<name>[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+(?<current>[\\d,]+)/(?<max>[\\d,]+)❤";

    private static final String regex = (
            "(?:\\[Lv\\d+\\]\\s+)?" +                               // Lvl
            "(?<icons>\\p{Co}+)?\\s*" +                             // MobTypes
            "(?<star>✯)?\\s*" +                                     // Dungeon Star
            "(?:Flaming|Stormy|Speedy|Fortified|Healthy)?\\s*" +    // Dungeon modifier
            "(?<name>[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+" +            // Mob name
            "(?<current>[\\d,.]+[kM]?)" +                           // Current HP
            "(?:/(?<max>[\\d,.]+[kM]?))?" +                         // Max HP
            "❤"
    );

    private static final Pattern MOB_NAME_PATTERN = Pattern.compile(regex);


    public static String mobNameParse(String custonName){
        Matcher match = MOB_NAME_PATTERN.matcher(custonName);
        if (match.find()) {
            return  match.group("name");
        }
        //Logger.log("REGEX", "%s", custonName);
        //throw new RuntimeException();
        return null;
    }


}
