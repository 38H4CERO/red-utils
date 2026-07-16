package net.redct.client.utils.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityUtils {
    private static final String regex = "\\[Lv(?<level>\\d+)\\]\\s+(?:\\p{Co}+\\s*)?(?<name>[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+(?<current>[\\d,]+)/(?<max>[\\d,]+)❤";
    private static final Pattern MOB_NAME_PATTERN = Pattern.compile(regex);

    private static Matcher match;

    public static boolean isMob(String name){
        match = MOB_NAME_PATTERN.matcher(name);
        if (match.find()) {
            System.out.println("Name: " + match.group("name"));
            return true;
        }

        return false;
    }


}
