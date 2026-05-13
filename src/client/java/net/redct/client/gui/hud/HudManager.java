package net.redct.client.gui.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HudManager {
    private static final List<HudInterface> elements = new ArrayList<>();

    public static void register(HudInterface element) {
        elements.add(element);
    }

    public static List<HudInterface> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public static HudInterface getById(String id) {
        return elements.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}