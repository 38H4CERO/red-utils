package net.redct.client.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static String activeProfileName = ConfigManager.activeProfile;

    // Scans the folder dynamically on every call
    public static List<String> getAvailableProfiles() {
        List<String> list = new ArrayList<>();
        File dir = ConfigManager.PROFILE_DIR;
        if (!dir.exists()) return list;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().length() - 5);
                list.add(name);
            }
        }
        return list;
    }

    public static void createProfile(String name) {
        File file = new File(ConfigManager.PROFILE_DIR, name + ".json");
        if (file.exists()) return;

        // Delegates directly to ConfigManager's existing saveProfile method!
        ConfigManager.saveProfile(name);
    }

    public static void duplicateProfile(String sourceName, String targetName) {
        File sourceFile = new File(ConfigManager.PROFILE_DIR, sourceName + ".json");
        File targetFile = new File(ConfigManager.PROFILE_DIR, targetName + ".json");

        if (sourceFile.exists() && !targetFile.exists()) {
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void selectProfile(String name) {
        // 1. Save current positions to the OLD profile
        saveActiveProfile();

        // 2. Switch the active profile name pointer
        activeProfileName = name;

        // 3. Delegate directly to ConfigManager's existing loadProfile method!
        ConfigManager.loadProfile(name);
    }

    public static void renameProfile(String oldName, String newName) {
        File oldFile = new File(ConfigManager.PROFILE_DIR, oldName + ".json");
        File newFile = new File(ConfigManager.PROFILE_DIR, newName + ".json");

        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile);

            if (activeProfileName.equals(oldName)) {
                activeProfileName = newName;
            }
        }
    }

    public static void saveActiveProfile() {
        // Delegates directly to ConfigManager's existing saveProfile method!
        ConfigManager.saveProfile(activeProfileName);
    }

    public static String getActiveProfileName() {
        return activeProfileName;
    }

    public static void setActiveProfileName(String name) {
        activeProfileName = name;
    }
}