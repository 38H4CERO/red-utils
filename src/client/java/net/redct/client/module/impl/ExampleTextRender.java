package net.redct.client.module.impl;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.redct.client.gui.hud.GuiTextUtils;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import static net.redct.client.RedUtilsClient.MOD_ID;

public class ExampleTextRender extends Module{
    public GuiTextUtils guiText = new GuiTextUtils("coords",4,12, 1.2f);

    public ExampleTextRender(){
        super("example_text","Example Text Render", Category.MISC);
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(MOD_ID, "coordinates_overlay"), this::render);
    }

    // TODO
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
    }


    private void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (!this.isEnabled()) return;  // Hide when disabled

        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        double x = mc.player.xo;
        double y = mc.player.yo;
        double z = mc.player.zo;

        guiText.setText(String.format("XYZ: %.1f / %.1f / %.1f", x, y, z));
        guiText.render(graphics, tickCounter);


    }
}
