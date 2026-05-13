package net.redct.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.redct.client.utils.DungeonSession;
import net.redct.client.utils.DungeonUtils;
import net.redct.client.utils.Utils;

public class EventSubscriber {

    public static void registerToEvents(){
        onServerConnectEVENT();
        onServerDisconnectEVENT();
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
}
