package net.redct.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.redct.client.config.ConfigManager;
import net.redct.client.gui.hud.impl.TextInputPopup;
import net.redct.client.gui.widget.AbsPanel;
import net.redct.client.gui.widget.RootPanel;
import net.redct.client.gui.widget.VPanel;
import net.redct.client.gui.widget.impl.ButtonWidget;
import net.redct.client.gui.widget.impl.IconButtonWidget;
import net.redct.client.gui.widget.impl.ModuleWidget;
import net.redct.client.gui.widget.impl.WindowWidget;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;

import static net.redct.client.utils.Logger.log;

public class ConfigScreen extends Screen {
    private final RootPanel rootPanel;
    private static final Identifier PROFILES_ICON = Identifier.fromNamespaceAndPath("red-utils", "textures/gui/profiles.png");
    private static final Identifier THEMES_ICON = Identifier.fromNamespaceAndPath("red-utils", "textures/gui/themes.png");

    // Store references so we can update their positions on resize
    private final IconButtonWidget profilesBtn;
    private final IconButtonWidget themesBtn;

    public ConfigScreen() {
        super(Component.literal("Red Utils"));

        // 1. Initialize RootPanel with AbsPanel to support absolute free-dragging
        this.rootPanel = new RootPanel(new AbsPanel());

        // 2. Add each Category column as an independent, draggable window
        addCategoryWindow(Category.DUNGEONS, 20, 20);
        addCategoryWindow(Category.KUUDRA, 140, 20);
        addCategoryWindow(Category.RENDER, 260, 20);
        addCategoryWindow(Category.MISC, 380, 20);
        addCategoryWindow(Category.DEBUG, 500, 20);

        // 3. Create the buttons (but position them later in init())
        this.profilesBtn = new IconButtonWidget(PROFILES_ICON, 16, 16, () -> {
            return;
        });
        rootPanel.add(profilesBtn);

        this.themesBtn = new IconButtonWidget(THEMES_ICON, 16, 16, () -> {
            return;
        });
        rootPanel.add(themesBtn);

        rootPanel.add(new ButtonWidget("Test Input", 80, 20, () -> {
            TextInputPopup content = new TextInputPopup("Enter name...", text -> {
                net.redct.client.RedUtilsClient.LOGGER.info("User confirmed: " + text);
            });

            // 2. Wrap it in a window
            WindowWidget popupWindow = new WindowWidget("Rename", 0, 0, content);

            // 3. Calculate the center of the screen
            int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            net.redct.client.utils.Utils.Vec2 pos = net.redct.client.utils.GuiUtils.centerWindow(
                    screenW, screenH, popupWindow.getWidth(), popupWindow.getHeight()
            );

            // 4. Move the window to the center and display it
            popupWindow.setPosition(pos.x(), pos.y());
            RootPanel.getInstance().addOverlay(popupWindow);
        }));
    }

    // Calls when created and each time it resizes
    @Override
    protected void init() {
        super.init(); // updates this.width and this.height

        int padding = 10; // Distance from the edges of the screen
        int gap = 4;      // Distance between the two buttons
        int btnSize = 16;

        // Position Themes button at the very bottom right
        int themesX = this.width - btnSize - padding;
        int themesY = this.height - btnSize - padding;
        themesBtn.setPosition(themesX, themesY);

        // Position Profiles button directly above the Themes button
        int profilesX = themesX;
        int profilesY = themesY - btnSize - gap;
        profilesBtn.setPosition(profilesX, profilesY);
    }

    private void addCategoryWindow(Category category, int x, int y) {
        VPanel moduleStack = new VPanel();

        for (Module module : ModuleManager.getByCategory(category)) {
            moduleStack.add(new ModuleWidget(module));
        }

        rootPanel.add(new WindowWidget(category.name(), x, y, moduleStack));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        rootPanel.render(graphics, this.font, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (rootPanel.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        rootPanel.mouseReleased(event.x(), event.y(), event.button());
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        rootPanel.mouseDragged(event.x(), event.y(), event.button());
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }


    @Override
    public boolean keyPressed(KeyEvent event) {
        // 1. Let our custom widgets handle typing or popup-closing first
        if (rootPanel.keyPressed(event)) {
            return true;
        }
        // 2. Fall back to Vanilla behavior (e.g., closing the menu if Esc is pressed and no popups are open)
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (rootPanel.charTyped(event)) return true;
        return super.charTyped(event);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        super.onClose();
    }
}