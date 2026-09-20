package net.redct.client.data;

public enum PlayerHeadSkin {

    MAGIC_JELLYBEAM("ewogICJ0aW1lc3RhbXAiIDogMTc2MzAyMjYxNTkzNywKICAicHJvZmlsZUlkIiA6ICIzMzU3MWJiY2UyMDE0MTRiYmNkMDYyMjEyZTI4MjBlMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFkb21JbmF0b3I0NzgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzUyNmE1NmI4MGY1NmE2ODcwZjg5MWQxZDQ2ZmE3ZjhjNzE0OTRjYWQyNGU5NDMyNmRhODRiMzgyOTQxN2I4MSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"),
    UNKNOWN("unknown");

    private final String id;

    PlayerHeadSkin(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Reverse lookup: id string -> enum constant
    public static PlayerHeadSkin fromId(String id) {
        for (PlayerHeadSkin skinBase64 : values()) {
            if (skinBase64.id.equals(id)) {
                return skinBase64;
            }
        }
        return UNKNOWN;
    }
}
