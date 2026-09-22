package net.redct.client.event;

import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.redct.client.data.Location;
import net.redct.client.utils.PlayerInfo;
import net.redct.client.utils.Utils;

public class HypixelApiLocationEvents {
    static HypixelModAPI api = HypixelModAPI.getInstance();

    static void register(){
        api.subscribeToEventPacket(ClientboundLocationPacket.class);

        api.createHandler(ClientboundLocationPacket.class, packet -> {
            // TODO: SKYBLOCK or lobby etc, se recibe cada lobby swap
            packet.getServerType().ifPresent(type -> {
                Utils.inHypixel=true;
                //Logger.log("PACKET", "Current server type: " + type.getName());
            });
            // same output of mode in /locraw
            packet.getMode().ifPresent(type -> {
                PlayerInfo.INSTANCE.setCurrentLocation(Location.fromId(type));
                //Logger.log("PACKET", "Current mode: " + type);
            });
        });
    }
}
