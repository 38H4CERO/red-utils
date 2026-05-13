package net.redct.client.gui.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class HudEditorScreen extends Screen {
    private static final int HANDLE_COLOR       = 0x80FFFFFF;
    private static final int HANDLE_HOVER_COLOR = 0xFFFFFFFF;
    private static final int HANDLE_SIZE        = 6;
    private static final float SCALE_STEP       = 0.1f;
    private static final float SCALE_MIN        = 0.5f;
    private static final float SCALE_MAX        = 5.0f;

    @Nullable private HudInterface dragging;
    @Nullable
    private HudInterface scaling;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

}