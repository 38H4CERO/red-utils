package net.redct.client.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import net.redct.client.RedUtilsClient;
import net.redct.client.gui.hud.HudInterface;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.ModuleManager;
import net.redct.client.module.Module;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "redutils.json");

    public static void save() {
        JsonObject root = new JsonObject();

        // Modules
        for (Module module : ModuleManager.getModules()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("enabled", module.isEnabled());
            for (Setting setting : module.getSettings()) {
                if (setting instanceof ToggleSetting toggle) {
                    moduleObj.addProperty(setting.getId(), toggle.getValue());
                } else if (setting instanceof SliderSetting slider) {
                    moduleObj.addProperty(setting.getId(), slider.getValue());
                }
            }
            root.add(module.getID(), moduleObj);
        }

        // HUD
        JsonObject hudObj = new JsonObject();
        for (HudInterface element : HudManager.getElements()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", element.getX());
            obj.addProperty("y", element.getY());
            obj.addProperty("scale", element.getScale());
            hudObj.add(element.getId(), obj);
        }
        root.add("hud", hudObj);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            RedUtilsClient.LOGGER.error("Failed to save config", e);
        }
    }

    public static void load() {
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

            // Modules
            for (Module module : ModuleManager.getModules()) {
                if (!root.has(module.getID())) continue;
                JsonObject moduleObj = root.getAsJsonObject(module.getID());
                if (moduleObj.has("enabled")) {
                    module.setStatus(moduleObj.get("enabled").getAsBoolean());
                }
                for (Setting setting : module.getSettings()) {
                    if (!moduleObj.has(setting.getId())) continue;
                    if (setting instanceof ToggleSetting toggle) {
                        toggle.setValue(moduleObj.get(setting.getId()).getAsBoolean());
                    } else if (setting instanceof SliderSetting slider) {
                        slider.setValue(moduleObj.get(setting.getId()).getAsDouble());
                    }
                }
            }

            // HUD
            if (root.has("hud")) {
                JsonObject hudObj = root.getAsJsonObject("hud");
                for (HudInterface element : HudManager.getElements()) {
                    if (!hudObj.has(element.getId())) continue;
                    JsonObject obj = hudObj.getAsJsonObject(element.getId());
                    element.setXY(obj.get("x").getAsInt(), obj.get("y").getAsInt());
                    element.setScale(obj.get("scale").getAsFloat());
                }
            }

        } catch (IOException | JsonSyntaxException e) {
            RedUtilsClient.LOGGER.error("Failed to load config", e);
        }
    }
}