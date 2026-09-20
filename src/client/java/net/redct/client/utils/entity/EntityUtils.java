package net.redct.client.utils.entity;

import com.mojang.authlib.properties.Property;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.redct.client.utils.Logger;
import net.redct.client.utils.dungeon.DungeonSession;
import org.jspecify.annotations.Nullable;

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

            if (icon != null){
                StringBuilder iconsHex = new StringBuilder();
                icon.codePoints().forEach(codepoint -> {
                    if (!iconsHex.isEmpty()) iconsHex.append(' ');
                    iconsHex.append("U+").append(Integer.toHexString(codepoint).toUpperCase());
                });

                //Logger.log("MOB", "%s, %s", name, iconsHex);
            }

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

    @Nullable
    public static String getArmorStandPlayerHeadSkin(Entity entity) {
        if (entity instanceof ArmorStand armorStand) {
            return getPlayerSkin(armorStand.getItemBySlot(EquipmentSlot.HEAD));
        }
        return null;
    }

    @Nullable
    public static String getPlayerSkin(ItemStack item){
            ResolvableProfile profile = item.get(DataComponents.PROFILE);
            if (profile != null) {
                return profile.partialProfile().properties().get("textures").iterator().next().value();
            }
            return null;
    }


}
