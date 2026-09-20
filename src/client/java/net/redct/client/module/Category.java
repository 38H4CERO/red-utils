package net.redct.client.module;

public enum Category {
    RENDER("Render"),
    DUNGEONS("Dungeons"),
    KUUDRA("Kuudra"),
    GARDEN("Garden"),
    DEBUG("Debug");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}