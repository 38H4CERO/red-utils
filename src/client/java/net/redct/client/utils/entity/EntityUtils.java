package net.redct.client.utils.entity;

import net.redct.client.utils.Logger;
import net.redct.client.utils.dungeon.DungeonSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            return  match.group("name");
        }
        //Logger.log("REGEX", "%s", custonName);
        //throw new RuntimeException();
        return null;
    }


}
