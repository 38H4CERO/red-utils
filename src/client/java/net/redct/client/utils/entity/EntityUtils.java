package net.redct.client.utils.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.redct.client.utils.Logger;
import org.jspecify.annotations.Nullable;

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
            String name = match.group("name");

            return name;
        }
        //Logger.log("REGEX", "%s", custonName);
        return null;
    }

    @Nullable
    public static String getPlayerSkin(ItemStack item){
            ResolvableProfile profile = item.get(DataComponents.PROFILE);
            if (profile != null ) {
                var textures = profile.partialProfile().properties().get("textures");
                if (!textures.isEmpty()) return textures.iterator().next().value();
            }
            return null;
    }

    @Nullable
    public static Long captureHp(ArmorStand stand) {
        Component c = stand.getCustomName();
        if (c == null) return null;

        String text = c.getString();
        String name = mobNameParse(text);
        if (name == null) return null;                     // not a mob nametag

        Entity mob = stand.level().getEntity(stand.getId() - 1);
        if (!(mob instanceof LivingEntity le) || mob instanceof ArmorStand) return null;
        if (Math.abs(mob.getX() - stand.getX()) > 1 || Math.abs(mob.getZ() - stand.getZ()) > 1) return null;

        long attr = Math.round(le.getAttributeBaseValue(Attributes.MAX_HEALTH));
        //Logger.log("Capture", "%s attr=%d stand=%s", name, attr, mobMaxHpParse(text));
        return attr;
    }

    @Nullable
    public static Long mobMaxHpParse(String customName) {
        Matcher match = MOB_NAME_PATTERN.matcher(customName);
        if (!match.find()) return null;

        String max = match.group("max");      // null in dungeons, where only current HP is shown
        if (max == null) return null;

        try {
            return parseHp(max);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long parseHp(String s) {
        s = s.replace(",", "");
        double mult = 1;
        char last = s.charAt(s.length() - 1);
        if (last == 'k') { mult = 1_000;     s = s.substring(0, s.length() - 1); }
        else if (last == 'M') { mult = 1_000_000; s = s.substring(0, s.length() - 1); }
        return Math.round(Double.parseDouble(s) * mult);
    }


}
