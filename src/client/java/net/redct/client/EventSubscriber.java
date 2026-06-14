package net.redct.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;
import net.redct.client.utils.dungeon.DungeonSession;
import net.redct.client.utils.Utils;

public class EventSubscriber {

    public static void registerToEvents(){
        onServerConnectEVENT();
        onServerDisconnectEVENT();
        onTickEVENT();
        onWorldRenderEVENT();
    }

    private static void onServerConnectEVENT() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Utils.isOnHypixel();
        });
    }

    private static void onServerDisconnectEVENT() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Utils.inHypixel = false;
            DungeonSession.end();
        });
    }

    private static void onTickEVENT(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Module module : ModuleManager.getModules()) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        });
    }

    private static void onWorldRenderEVENT() {
        LevelRenderEvents.END_MAIN.register(context -> {
            for (Module module : ModuleManager.getModules()) {
                if (module.isEnabled()) {
                    module.onWorldRender(context);
                }
            }
            //context.bufferSource().endBatch();
        });
    }
}
