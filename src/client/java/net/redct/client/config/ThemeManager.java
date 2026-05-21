package net.redct.client.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static String activeThemeName = ConfigManager.activeTheme;

    // Scans the themes folder dynamically on every call
    public static List<String> getAvailableThemes() {
        List<String> list = new ArrayList<>();
        File dir = ConfigManager.THEME_DIR;
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

    public static void createTheme(String name) {
        File file = new File(ConfigManager.THEME_DIR, name + ".json");
        if (file.exists()) return;

        // Delegates directly to ConfigManager's saveTheme method!
        ConfigManager.saveTheme(name);
    }

    public static void duplicateTheme(String sourceName, String targetName) {
        File sourceFile = new File(ConfigManager.THEME_DIR, sourceName + ".json");
        File targetFile = new File(ConfigManager.THEME_DIR, targetName + ".json");

        if (sourceFile.exists() && !targetFile.exists()) {
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void selectTheme(String name) {
        // 1. Save any custom modifications to the OLD active theme first
        saveActiveTheme();

        // 2. Switch the active theme name pointer
        activeThemeName = name;

        // 3. Delegate directly to ConfigManager's loadTheme method!
        ConfigManager.loadTheme(name);
    }

    public static void renameTheme(String oldName, String newName) {
        File oldFile = new File(ConfigManager.THEME_DIR, oldName + ".json");
        File newFile = new File(ConfigManager.THEME_DIR, newName + ".json");

        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile);

            if (activeThemeName.equals(oldName)) {
                activeThemeName = newName;
            }
        }
    }

    public static void saveActiveTheme() {
        // Delegates directly to ConfigManager's saveTheme method!
        ConfigManager.saveTheme(activeThemeName);
    }

    public static String getActiveThemeName() {
        return activeThemeName;
    }

    public static void setActiveThemeName(String name) {
        activeThemeName = name;
    }
}
