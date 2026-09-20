package net.redct.client.data;

public enum Location {
    PRIVATE_ISLAND("dynamic"),
    HUB("hub"),
    DUNGEON_HUB("dungeon_hub"),
    THE_BARN("farming_1"),
    THE_PARK("foraging_1"),
    GOLD_MINES("mining_1"),
    DEEP_CAVERNS("mining_2"),
    DWARVEN_MINES("mining_3"),
    CRYSTAL_HOLLOWS("crystal_hollows"),
    MINESHAFT("mineshaft"),
    SPIDERS_DEN("combat_1"),
    THE_END("combat_3"),
    CRIMSON_ISLE("crimson_isle"),
    GARDEN("garden"),
    BACKWATER_BAYOU("fishing_1"),
    LOTUS_ATOLL("lotus_atoll"),
    GALATEA("foraging_2"),
    TORRHUS_CANYON("foraging_3"),
    SAFARI("safari"),
    THE_RIFT("rift"),
    DARK_AUCTION("dark_auction"),
    THE_CATACOMBS("dungeon"),
    KUUDRA("kuudra"),
    JERRYS_WORKSHOP("winter"),
    UNKNOWN("unknown");

    private final String id;

    Location(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Reverse lookup: id string -> enum constant
    public static Location fromId(String id) {
        for (Location loc : values()) {
            if (loc.id.equals(id)) {
                return loc;
            }
        }
        return UNKNOWN;
    }
}
