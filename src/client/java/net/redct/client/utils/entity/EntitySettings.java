package net.redct.client.utils.entity;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.redct.client.data.Location;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntitySettings {
    public String label;
    public TrackStyle style = new TrackStyle();
    public EntityFingerprint fingerprint = new EntityFingerprint();

    public EntitySettings() {
    }

    public static final class TrackStyle {
        public boolean hide = false;
        public boolean glow = true;
        public int glowColor = 0xFFFFFFFF;

        public boolean trace = false;
        public int traceColor = 0xFFFFFFFF;
        public float traceThickness = 1.0f;
        public TraceType traceType = TraceType.NEAREST;

        public enum TraceType {
            NEAREST,
            ALL;
        }
    }

    public static final class EntityFingerprint {
        public Set<Location> locations = new HashSet<>();
        public String entityType;
        public boolean realPlayer = false;
        // If isPlayer=true then player name
        public String name;
        // in dungeons doesnt work, runic neither (x4 hp)
        public Set<Long> maxHealth = new HashSet<>();
        public Map<EquipmentSlot, ItemMatch> equipment = new EnumMap<>(EquipmentSlot.class);

        public String skinHash;

        public boolean hpMatches(double hp) {
            return maxHealth.isEmpty() || maxHealth.contains(Math.round(hp));
        }

        public boolean locationMatches(Location loc) {
            return locations.isEmpty() || locations.contains(loc);
        }

        public static final class ItemMatch {
            public Item item;   // "air" = must be empty. null = any
            public String skin;

            public ItemMatch() {
                this.item = null;
                this.skin = null;
            }

            public ItemMatch(Item item, String skin) {
                this.item = item;
                this.skin = skin;
            }

            public boolean matches(ItemStack st) {
                return (item == null || st.is(item))
                        && (skin == null || skin.equals(EntityUtils.getPlayerSkin(st)));
            }
        }
    }
}

