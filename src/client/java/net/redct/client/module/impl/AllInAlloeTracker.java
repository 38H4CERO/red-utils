package net.redct.client.module.impl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
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
import net.redct.client.utils.render.Tracer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.redct.client.module.ModuleManager.isModuleEnabled;
import static net.redct.client.utils.entity.EntityUtils.getArmorStandPlayerHeadSkin;
import static net.redct.client.utils.entity.EntityUtils.rescanLoadedArmorStands;

public class AllInAlloeTracker extends Module {
    public static GuiTextUtils guiText = new GuiTextUtils("alloe_tracker", 4, 12, 1.2f);
    public static final SliderSetting trigger = new SliderSetting("trigger", "Stage", 14, 0, 27, 1);
    public static final ColorSetting color = new ColorSetting("color", "Color", 0xFFFFFFFF);

    public AllInAlloeTracker() {
        super("all_in_alloe_tracker", "Alloe Track", Category.GARDEN);
        HudManager.register(guiText, this); // register so HudEditorScreen can see and move it
        guiText.setVisible(true);
        guiText.setColor(0xdd7878, 255);
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
    private static final Set<String> lines = ConcurrentHashMap.newKeySet();
    private static final double alloe_y = 75.5;
    public record MutationStand(Utils.Vec2 pos, int stage, UUID uuid) {

    }



    public static void addMutation(Entity entity, int stage) {
        Utils.Vec2 pos = new Utils.Vec2((int)Math.floor(entity.getX()), (int)Math.floor(entity.getZ()));
        mutations.put(entity.getUUID(), new MutationStand(pos ,stage,entity.getUUID()));
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
        list.sort(Comparator.comparingInt(MutationStand::stage).reversed());
        orderedStages = list;
        guiText.setText(buildString());
        traceAllAlloes();
    }

    private static String buildString() {
        if (orderedStages.isEmpty()) {
            clearAllLines();
            return "";
        }
        StringBuilder text = new StringBuilder("All-in-Alloe:");
        int stage = orderedStages.getFirst().stage();
        int sum = 0;

        for (MutationStand mutation : orderedStages) {
            if (mutation.stage() == stage) {
                sum++;
            } else {
                text.append(String.format("\n - %-2d x%d", stage, sum));
                stage = mutation.stage();
                sum = 1;
            }
        }
        text.append(String.format("\n - %-2d x%d", stage, sum));
        return text.toString();
    }

    private static void traceAllAlloes(){
        for (MutationStand mutation : orderedStages) {
            if (mutation.stage() >= trigger.getValue()) {
                Tracer.setLine(mutation.uuid.toString(), Tracer.Anchor.player(), Tracer.Anchor.fixed(new Vec3(mutation.pos.x()+0.5, alloe_y, mutation.pos.y()+0.5)), 2, color.getColor());
            } else {
                Tracer.removeLine(mutation.uuid.toString());
            }
        }
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
        if (!ModuleManager.isModuleEnabled("all_in_alloe_tracker")) return;
        if (!PlayerInfo.INSTANCE.getCurrentLocation().equals(Location.GARDEN)) return;
        //if (!entity.hasCustomName()) return;
        String name = entity.getCustomName().getString();
        int stage = checkAlloeStage(name);
        if (stage == -1) return;
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

    private static void clearAllLines(){
        for  (String id : lines) {
            Tracer.removeLine(id);
        }
    }
}
