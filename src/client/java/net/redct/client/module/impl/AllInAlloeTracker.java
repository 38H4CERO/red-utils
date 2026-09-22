package net.redct.client.module.impl;

import net.minecraft.world.entity.Entity;
import net.redct.client.config.ColorSetting;
import net.redct.client.config.SliderSetting;
import net.redct.client.data.Location;
import net.redct.client.data.PlayerHeadSkin;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;
import net.redct.client.utils.Logger;
import net.redct.client.utils.PlayerInfo;
import net.redct.client.utils.Utils;
import net.redct.client.utils.entity.HiddenArmorStands;
import net.redct.client.utils.render.GuiTextUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.redct.client.module.ModuleManager.isModuleEnabled;
import static net.redct.client.utils.entity.EntityUtils.getArmorStandPlayerHeadSkin;
import static net.redct.client.utils.entity.EntityUtils.rescanLoadedArmorStands;

public class AllInAlloeTracker extends Module {
    public static GuiTextUtils guiText = new GuiTextUtils("alloe_tracker", 4, 12, 1.2f);
    public final SliderSetting trigger = new SliderSetting("trigger", "Stage", 14, 0, 27, 1);
    public final ColorSetting color = new ColorSetting("color", "Color", 0xFFFFFFFF);

    public AllInAlloeTracker() {
        super("all_in_alloe_tracker", "Alloe Track", Category.GARDEN);
        HudManager.register(guiText, this); // register so HudEditorScreen can see and move it
        guiText.setVisible(false);
        registerSetting(trigger);
        registerSetting(color);

    }

    @Override
    public void onEnable() {
        HiddenArmorStands.INSTANCE.clear();
        rescanLoadedArmorStands();
        guiText.setVisible(true);

    }

    @Override
    public void onDisable() {
        guiText.setVisible(false);
        HiddenArmorStands.INSTANCE.clear();
    }


    private static final Map<UUID, MutationStand> mutations = new ConcurrentHashMap<>();
    private static List<MutationStand> orderedStages;
    public record MutationStand(Utils.Vec2 pos, int stage) {

    }



    public static void addMutation(Entity entity, int stage) {
        Utils.Vec2 pos = new Utils.Vec2((int)Math.floor(entity.getX()), (int)Math.floor(entity.getZ()));
        mutations.put(entity.getUUID(), new MutationStand(pos ,stage));
    }

    public static void removeMutation(UUID uuid) {
        mutations.remove(uuid);
    }

    public static MutationStand get(UUID uuid) {
        return mutations.get(uuid);
    }

    public static void clearMutations() {
        mutations.clear();
    }

    public static void sortListByStage() {
        List<MutationStand> list = new ArrayList<>(mutations.values());
        list.sort(Comparator.comparingInt(MutationStand::stage));
        orderedStages = list;
        guiText.setText(list.size() + " alloe");
    }

    public static void checkJellyBeans(Entity entity) {
        if (PlayerInfo.INSTANCE.getCurrentLocation() == Location.GARDEN) {
            if (isModuleEnabled("all_in_alloe_tracker")) {
                if (HiddenArmorStands.INSTANCE.isProcessed(entity.getUUID())) return;
                if (PlayerHeadSkin.MAGIC_JELLYBEAM.getId().equals(getArmorStandPlayerHeadSkin(entity))) {
                    HiddenArmorStands.INSTANCE.setProcessed(entity.getUUID());
                    HiddenArmorStands.INSTANCE.hide(entity.getUUID());
                }
            }
        }
    }

    public static void manageAlloe(Entity entity) {
        Logger.log("Fase1", "%s, %s, %s", ModuleManager.isModuleEnabled("all_in_alloe_tracker"), PlayerInfo.INSTANCE.getCurrentLocation(), entity.getCustomName().getString());
        if (!ModuleManager.isModuleEnabled("all_in_alloe_tracker")) return;
        if (!PlayerInfo.INSTANCE.getCurrentLocation().equals(Location.GARDEN)) return;

        String name = entity.getCustomName().getString();
        int stage = checkAlloeStage(name);
        if (stage == -1) return;
        Logger.log("Fase2", "%s", stage);
        addMutation(entity, stage);
        sortListByStage();
    }

    private static int checkAlloeStage(String name) {
        if (!name.startsWith("Stage ")) return -1;
        try {
            return Integer.parseInt(name, 6, name.length(), 10);
        } catch (Exception e) {
            Logger.log("ERROR", "AllInAlloe (%s)", e);
            return -1;
        }

    }
}
