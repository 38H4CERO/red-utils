package net.redct.client.module;

import net.redct.client.module.impl.AllInAlloeTracker;
import net.redct.client.module.impl.DungeonClearAlert;
import net.redct.client.module.impl.EntityDebugModule;
import net.redct.client.module.impl.ExampleTextRender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    private static final Map<String, Module> byId = new HashMap<>();
    private static final Map<String, Module> byName = new HashMap<>();

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
        if (byId.putIfAbsent(module.getID(), module) != null) {
            throw new IllegalStateException("Duplicate module id: " + module.getID());
        }
        byName.putIfAbsent(module.getName(), module);
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

    public static Module getByID(String id) {
        return byId.get(id);
    }

    public static Module getByName(String name) {
        return byName.get(name);
    }

    public static boolean isModuleEnabled(String id) {
        Module module = byId.get(id);
        return module != null && module.isEnabled();
    }

}
