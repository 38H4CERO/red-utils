package net.redct.client.utils.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityUtils {
    private static final String regex = "\\[Lv(?<level>\\d+)\\]\\s+(?:\\p{Co}+\\s*)?(?<name>[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+(?<current>[\\d,]+)/(?<max>[\\d,]+)❤";
    private static final Pattern MOB_NAME_PATTERN = Pattern.compile(regex);


    public static String mobNameParse(String custonName){
        Matcher match = MOB_NAME_PATTERN.matcher(custonName);
        if (match.find()) {
            return  match.group("name");
        }
        //throw new RuntimeException();
        return null;
    }


}
