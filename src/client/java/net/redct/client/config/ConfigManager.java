package net.redct.client.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import net.redct.client.RedUtilsClient;
import net.redct.client.gui.config.UITheme;
import net.redct.client.gui.hud.HudInterface;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.ModuleManager;
import net.redct.client.module.Module;

import static net.redct.client.utils.GuiTextUtils.colorToHex;
import static net.redct.client.utils.GuiTextUtils.hexToColor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "redutils");
    static final File THEME_DIR = new File(CONFIG_DIR, "themes");
    static final File PROFILE_DIR = new File(CONFIG_DIR, "profiles");
    static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");

    static final String defaultName = "default";

    // Global placeholders for active theme and profile configurations
    public static String activeTheme = defaultName;
    public static String activeProfile = defaultName;


    public static void save() {
        createMissingDirs(); // Ensure folders exist on disk

        JsonObject root = new JsonObject();

        // 1. Add root-level placeholders
        root.addProperty("active_theme", activeTheme);
        root.addProperty("active_hud_profile", activeProfile);

        // 2. Nest all module configurations using the helper
        JsonObject configObj = new JsonObject();
        for (Module module : ModuleManager.getModules()) {
            configObj.add(module.getID(), serializeModule(module));
        }
        root.add("config", configObj);

        // 3. Save module settings to config.json
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            RedUtilsClient.LOGGER.error("Failed to save config", e);
        }

        // 4. Save active HUD positions to the active profile file on disk (Zero redundancy!)
        saveProfile(activeProfile);
    }

    public static void load() {
        createMissingDirs();

        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            var json = JsonParser.parseReader(reader);
            if (!json.isJsonObject()) {
                RedUtilsClient.LOGGER.error("Config file is not a JSON object");
                return;
            }
            JsonObject root = json.getAsJsonObject();

            // 1. Load active theme and profile if present
            if (root.has("active_theme")) {
                activeTheme = root.get("active_theme").getAsString();
            }
            if (root.has("active_hud_profile")) {
                activeProfile = root.get("active_hud_profile").getAsString();
            }

            // Fallback to default if the selected files don't exist on disk
            File themeFile = new File(THEME_DIR, activeTheme + ".json");
            if (!themeFile.exists()) {
                activeTheme = defaultName;
            }

            File profileFile = new File(PROFILE_DIR, activeProfile + ".json");
            if (!profileFile.exists()) {
                activeProfile = defaultName;
            }

            // 2. Apply theme colors and HUD coordinates dynamically on startup!
            loadTheme(activeTheme);
            loadProfile(activeProfile);

            // 3. Load Modules settings directly from the "config" block
            if (root.has("config")) {
                JsonObject configObj = root.getAsJsonObject("config");
                for (Module module : ModuleManager.getModules()) {
                    if (!configObj.has(module.getID())) continue;
                    JsonObject moduleObj = configObj.getAsJsonObject(module.getID());

                    if (moduleObj.has("enabled")) {
                        module.setStatus(moduleObj.get("enabled").getAsBoolean());
                    }
                    for (Setting setting : module.getSettings()) {
                        if (!moduleObj.has(setting.getId())) continue;

                        switch (setting) {
                            case ToggleSetting toggle -> toggle.setValue(moduleObj.get(setting.getId()).getAsBoolean());
                            case SliderSetting slider -> slider.setValue(moduleObj.get(setting.getId()).getAsDouble());
                            case ColorSetting color -> color.setColor(hexToColor(moduleObj.get(setting.getId()).getAsString()));
                            default -> {}
                        }
                    }
                }
            }

        } catch (IOException | JsonSyntaxException e) {
            RedUtilsClient.LOGGER.error("Failed to load config", e);
        }
    }

    private static JsonObject serializeModule(Module module) {
        JsonObject moduleObj = new JsonObject();
        moduleObj.addProperty("enabled", module.isEnabled());

        for (Setting setting : module.getSettings()) {
            switch (setting) {
                case ToggleSetting toggle -> moduleObj.addProperty(setting.getId(), toggle.getValue());
                case SliderSetting slider -> moduleObj.addProperty(setting.getId(), slider.getValue());
                case ColorSetting color   -> moduleObj.addProperty(setting.getId(), colorToHex(color.getColor()));
                default -> {}
            }
        }
        return moduleObj;
    }

    private static void createMissingDirs(){
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!THEME_DIR.exists()) THEME_DIR.mkdirs();
        if (!PROFILE_DIR.exists()) PROFILE_DIR.mkdirs();

        // 1. Ensure "default.json" theme always exists
        File defaultTheme = new File(THEME_DIR, defaultName + ".json");
        if (!defaultTheme.exists()) {
            saveTheme(defaultTheme);
        }

        // 2. Ensure "default.json" HUD profile always exists
        File defaultProfile = new File(PROFILE_DIR, defaultName + ".json");
        if (!defaultProfile.exists()) {
            saveProfile(defaultProfile);
        }
    }

    public static void saveProfile(String profileName) {
        File file = new File(PROFILE_DIR, profileName + ".json");
        saveProfile(file);
    }

    private static void saveProfile(File file) {
        JsonObject hudObj = new JsonObject();
        for (HudInterface element : HudManager.getElements()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", element.getX());
            obj.addProperty("y", element.getY());
            obj.addProperty("scale", element.getScale());
            hudObj.add(element.getId(), obj);
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(hudObj, writer);
        } catch (IOException e) {
            RedUtilsClient.LOGGER.error("Failed to write HUD profile", e);
        }
    }

    public static void saveTheme(String themeName) {
        File file = new File(THEME_DIR, themeName + ".json");
        saveTheme(file);
    }

    private static void saveTheme(File file) {
        JsonObject themeObj = new JsonObject();
        try {
            for (java.lang.reflect.Field field : UITheme.class.getFields()) {
                if (field.getType() == int.class) {
                    int value = field.getInt(null);
                    themeObj.addProperty(field.getName(), String.format("0x%08X", value));
                }
            }
        } catch (IllegalAccessException e) {
            RedUtilsClient.LOGGER.error("Failed to read UITheme fields via reflection", e);
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(themeObj, writer);
        } catch (IOException e) {
            RedUtilsClient.LOGGER.error("Failed to write theme", e);
        }
    }

    public static void loadTheme(String themeName) {
        File themeFile = new File(THEME_DIR, themeName + ".json");
        if (!themeFile.exists()) return;

        try (FileReader reader = new FileReader(themeFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            for (java.lang.reflect.Field field : UITheme.class.getFields()) {
                if (field.getType() == int.class && root.has(field.getName())) {
                    String hexValue = root.get(field.getName()).getAsString();
                    int color = (int) Long.parseLong(hexValue.replace("0x", ""), 16);
                    field.setInt(null, color);
                }
            }
        } catch (Exception e) {
            RedUtilsClient.LOGGER.error("Failed to load theme: " + themeName, e);
        }
    }

    public static void loadProfile(String profileName) {
        File profileFile = new File(PROFILE_DIR, profileName + ".json");
        if (!profileFile.exists()) return;

        try (FileReader reader = new FileReader(profileFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            for (HudInterface element : HudManager.getElements()) {
                if (root.has(element.getId())) {
                    JsonObject obj = root.getAsJsonObject(element.getId());
                    element.setXY(obj.get("x").getAsInt(), obj.get("y").getAsInt());
                    element.setScale(obj.get("scale").getAsFloat());
                }
            }
        } catch (Exception e) {
            RedUtilsClient.LOGGER.error("Failed to load HUD profile: " + profileName, e);
        }
    }
}