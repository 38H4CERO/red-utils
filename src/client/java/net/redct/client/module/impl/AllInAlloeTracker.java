package net.redct.client.module.impl;

import net.minecraft.world.entity.Entity;
import net.redct.client.config.ColorSetting;
import net.redct.client.config.SliderSetting;
import net.redct.client.data.Location;
import net.redct.client.data.PlayerHeadSkin;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.utils.PlayerInfo;
import net.redct.client.utils.entity.HiddenArmorStands;
import net.redct.client.utils.render.GuiTextUtils;

import static net.redct.client.module.ModuleManager.isModuleEnabled;
import static net.redct.client.utils.entity.EntityUtils.getArmorStandPlayerHeadSkin;

public class AllInAlloeTracker extends Module {
    public GuiTextUtils guiText = new GuiTextUtils("alloe_tracker",4,12, 1.2f);
    public final SliderSetting trigger = new SliderSetting("trigger", "Stage", 14, 0, 27, 1);
    public final ColorSetting color = new ColorSetting("color", "Color", 0xFFFFFFFF);


    public AllInAlloeTracker() {
        super("all_in_alloe_tracker", "Alloe Track", Category.GARDEN);
        HudManager.register(guiText, this); // register so HudEditorScreen can see and move it
        guiText.setText("Alloe");
        registerSetting(trigger);
        registerSetting(color);

    }

    @Override
    public void onEnable() {
        //guiText.setVisible(true);

    }

    @Override
    public void onDisable() {
        guiText.setVisible(false);
        HiddenArmorStands.INSTANCE.clear();
    }

    public static void checkJellyBeans(Entity entity){
        if (PlayerInfo.INSTANCE.getCurrentLocation() == Location.GARDEN){
            if (isModuleEnabled("all_in_alloe_tracker")){
                if (HiddenArmorStands.INSTANCE.isProcessed(entity.getUUID())) return;
                if(PlayerHeadSkin.MAGIC_JELLYBEAM.getId().equals(getArmorStandPlayerHeadSkin(entity))){
                    HiddenArmorStands.INSTANCE.setProcessed(entity.getUUID());
                    HiddenArmorStands.INSTANCE.hide(entity.getUUID());
                }
            }
        }
    }
}
