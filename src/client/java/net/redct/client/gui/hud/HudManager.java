package net.redct.client.gui.hud;

import java.util.LinkedHashMap;
import java.util.Map;

public class HudManager {
    private static final Map<String, HudInterface> elements = new LinkedHashMap<>();

    public static void init(){

    }

    public static void register(HudInterface gui, String id) {
        elements.put(id, gui);
    }

    public static  Map<String, HudInterface> getElements() {
        return elements;
    }

    public static HudInterface getByName(String name) {
        // TODO
        return null;
    }
}