package net.redct.client.module.impl;

import net.minecraft.client.Minecraft;
import net.redct.client.utils.GuiTextUtils;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.Category;
import net.redct.client.module.Module;

import static net.redct.client.utils.Logger.log;

public class ExampleTextRender extends Module{
    public GuiTextUtils guiText = new GuiTextUtils("coords",4,12, 1.2f);

    public ExampleTextRender(){
        super("example_text","Example Text Render", Category.MISC);
        HudManager.register(guiText, this); // register so HudEditorScreen can see and move it
    }

    @Override
    public void onEnable() {
        guiText.setVisible(true);
    }

    @Override
    public void onDisable() {
        guiText.setVisible(false);
    }

    @Override
    public void onTick(){
        if (!isEnabled()) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            guiText.setText(null);
            return;
        }
        guiText.setText(String.format("XYZ: %.1f / %.1f / %.1f",
                mc.player.xo, mc.player.yo, mc.player.zo));
    }

}
