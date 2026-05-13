package net.redct.client.gui.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.redct.client.module.Module;
import java.util.ArrayList;
import java.util.List;

import static net.redct.client.RedUtilsClient.MOD_ID;

public class HudManager {
    private static boolean isEditorOpen = false;
    private record HudEntry(HudInterface element, Module module) {}

    private static final List<HudEntry> entries = new ArrayList<>();

    public static void init() {
        HudElementRegistry.addFirst(
                Identifier.fromNamespaceAndPath(MOD_ID, "hud_overlay"),
                HudManager::renderAll
        );
    }

    public static void register(HudInterface element, Module module) {
        entries.add(new HudEntry(element, module));
    }

    public static Module getModule(HudInterface element) {
        return entries.stream()
                .filter(e -> e.element() == element)
                .findFirst()
                .map(HudEntry::module)
                .orElse(null);
    }

    private static void renderAll(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (isEditorOpen) return; // HudEditorScreen handles rendering itself
        for (HudEntry entry : entries) {
            if (entry.element().isVisible()) {
                entry.element().render(graphics);
            }
        }
    }

    public static List<HudInterface> getElements() {
        return entries.stream()
                .map(HudEntry::element)
                .toList();
    }

    public static HudInterface getById(String id) {
        return entries.stream()
                .map(HudEntry::element)
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static void bringToFront(HudInterface element) {
        entries.stream()
                .filter(e -> e.element() == element)
                .findFirst()
                .ifPresent(e -> {
                    entries.remove(e);
                    entries.add(e);
                });
    }

    public static void setEditorOpen(boolean open) { isEditorOpen = open; }
}