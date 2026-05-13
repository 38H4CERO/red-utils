package net.redct.client.config;



import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import net.redct.client.RedUtilsClient;
import net.redct.client.module.ModuleManager;
import net.redct.client.module.Module;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "redutils.json");

    public static int save(){
        JsonObject jsonObject = new JsonObject();

        for (Module module : ModuleManager.getModules()){
            jsonObject.addProperty(module.getID(), module.isEnabled());
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(jsonObject, writer);
        } catch (IOException e) {
            RedUtilsClient.LOGGER.error("Failed to save config");
            return -1;
        }
        return 0;
    }

    public static int load(){
        if (!CONFIG_FILE.exists()) {
            save();
            return 1;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)){
            var element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()){
                RedUtilsClient.LOGGER.error("Config file is not a JSON object");
                return -1;
            }
            JsonObject jsonObject = element.getAsJsonObject();

            for (Module module : ModuleManager.getModules()) {
                // Check if the module name exists in the JSON file
                if (jsonObject.has(module.getID())) {
                    boolean isEnabled = jsonObject.get(module.getID()).getAsBoolean();
                    // If the file says it should be enabled, but it currently isn't, turn it on!
                    if (isEnabled) {
                        module.setStatus(true);
                    }
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            RedUtilsClient.LOGGER.error("Failed to load config", e);
            return -1;
        }

        return 0;
    }

}
