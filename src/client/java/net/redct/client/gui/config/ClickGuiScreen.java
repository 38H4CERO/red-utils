package net.redct.client.gui.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.redct.client.config.ConfigManager;
import net.redct.client.config.ProfileManager;
import net.redct.client.config.ThemeManager;
import net.redct.client.gui.widget.AbsPanel;
import net.redct.client.gui.widget.RootPanel;
import net.redct.client.gui.widget.VPanel;
import net.redct.client.gui.widget.impl.IconButtonWidget;
import net.redct.client.gui.widget.impl.ModuleWidget;
import net.redct.client.gui.widget.impl.SelectorPanelWidget;
import net.redct.client.gui.widget.impl.WindowWidget;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;

public class ClickGuiScreen extends Screen {
    private final RootPanel rootPanel;
    private static final Identifier PROFILES_ICON = Identifier.fromNamespaceAndPath("red-utils", "textures/gui/profiles.png");
    private static final Identifier THEMES_ICON = Identifier.fromNamespaceAndPath("red-utils", "textures/gui/themes.png");

    public ClickGuiScreen() {
        super(Component.literal("Red Utils"));

        // 1. Initialize RootPanel with AbsPanel to support absolute free-dragging
        this.rootPanel = new RootPanel(new AbsPanel());

        // 2. Add each Category column as an independent, draggable window
        addCategoryWindow(Category.DUNGEONS, 20, 20);
        addCategoryWindow(Category.KUUDRA, 140, 20);
        addCategoryWindow(Category.RENDER, 260, 20);
        addCategoryWindow(Category.MISC, 380, 20);

        IconButtonWidget profilesBtn = new IconButtonWidget(PROFILES_ICON, 16, 16, () -> {
            openManagerPopup("Profiles Manager", new SelectorPanelWidget(
                    ProfileManager::getAvailableProfiles,
                    ProfileManager::getActiveProfileName,
                    ProfileManager::selectProfile
            ));
        });
        profilesBtn.setPosition(20, 10);
        rootPanel.add(profilesBtn);

        // 2. ADDED: Draggable Themes popup triggered by a clean Icon button
        IconButtonWidget themesBtn = new IconButtonWidget(THEMES_ICON, 16, 16, () -> {
            openManagerPopup("Themes Manager", new SelectorPanelWidget(
                    ThemeManager::getAvailableThemes,
                    ThemeManager::getActiveThemeName,
                    ThemeManager::selectTheme
            ));
        });
        themesBtn.setPosition(40, 10); // Placed immediately next to Profiles button
        rootPanel.add(themesBtn);
    }

    private void openManagerPopup(String title, SelectorPanelWidget panel) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        WindowWidget popupWindow = new WindowWidget(title, centerX, centerY, panel);
        rootPanel.addOverlay(popupWindow);
    }

    private void addCategoryWindow(Category category, int x, int y) {
        // Create a vertical stack container
        VPanel moduleStack = new VPanel();

        // Populate the stack with ModuleWidgets belonging to this category
        for (Module module : ModuleManager.getByCategory(category)) {
            moduleStack.add(new ModuleWidget(module));
        }

        // Wrap the vertical stack inside a draggable WindowWidget and add it to our root layout
        rootPanel.add(new WindowWidget(category.name(), x, y, moduleStack));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Render the transparent background
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // Render the entire widget tree
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
        return true; // Keeps the background dark
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        super.onClose();
    }
}