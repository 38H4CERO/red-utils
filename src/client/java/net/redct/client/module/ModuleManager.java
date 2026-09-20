package net.redct.client.module;

import net.redct.client.module.impl.AllInAlloeTracker;
import net.redct.client.module.impl.DungeonClearAlert;
import net.redct.client.module.impl.EntityDebugModule;
import net.redct.client.module.impl.ExampleTextRender;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();
    private static boolean debugMode = true;

    public static void init() {
        // TODO: for module in list = register
        register(new ExampleTextRender());
        register(new DungeonClearAlert());
        register(new AllInAlloeTracker());

        if (debugMode){
            register(new EntityDebugModule());
        }
    }

    public static void register(Module module) {
        modules.add(module);
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static List<Module> getByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .toList();
    }

    public static Module getByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static Module getByID(String id) {
        return modules.stream()
                .filter(m -> m.getID().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public static boolean isModuleEnabled(String moduleid) {
        Module module = getByID(moduleid);
        return module.isEnabled();
    }

}
