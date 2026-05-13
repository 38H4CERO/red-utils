package net.redct.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.redct.client.RedUtilsClient;
import net.redct.client.module.impl.DungeonClearAlert;
import net.redct.client.utils.DungeonSession;
import net.redct.client.utils.DungeonUtils;
import net.redct.client.utils.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static net.redct.client.module.ModuleManager.isModuleEnabled;
import static net.redct.client.utils.DungeonUtils.MAP_SIZE;

@Mixin(ClientPacketListener.class)
public class MapDataMixin {
    private int mapId = 1024;
    private int playerX, playerY;

    // We inject at HEAD this time, just to see if the packet arrived at all
    @Inject(method = "handleMapItemData", at = @At("TAIL"))
    private void onMapUpdate(ClientboundMapItemDataPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if(!isModuleEnabled("dungeonClearAlert")) return;
        if(!Utils.inHypixel) return;
        if(DungeonSession.get() == null) return;

        if (packet.mapId().id() != mapId) return;

        // Get the map data that was just updatedinDungeons
        MapItemSavedData mapData = mc.level.getMapData(packet.mapId());

        // Safety check to ensure the map actually has colors and decorations
        if (mapData == null || mapData.colors == null) return;


        //dumpMapToFile(mapData.colors);

        for (MapDecoration decoration : mapData.getDecorations()){
            // Check if decoration is the player
            if (decoration.getSpriteLocation().getPath().equals("frame")) {
                playerX = (decoration.x() + MAP_SIZE) / 2;
                playerY = (decoration.y() + MAP_SIZE) / 2;
                DungeonClearAlert.getMapData(mapData.colors, playerX, playerY);
                //getColorAt( mapData.colors, playerX, playerY);
            }
        }
        // packet.mapId().id() gets the actual integer ID of the map being updated.
        //RedUtilsClient.LOGGER.info("Map Packet Received! Map ID: {}", packet.mapId().id());

    }
    private int getColorAt(byte[] mapColors, int x, int y) {
        return mapColors[y * MAP_SIZE + x];
        //RedUtilsClient.LOGGER.info("Color at {} {} is {} ---", x, y, color);
    }

    // TODO: Cada 4 pixeles mirar 4 pixeles perperdicularmente a cada lado hasta llegar al fondo, si es una L llorar
    private void dumpMapToFile(byte[] mapColors) {
        // This saves the file directly into your client's 'run' folder
        File dumpFile = new File(Minecraft.getInstance().gameDirectory, "map_dump_" + mapId + ".txt");

        try (PrintWriter writer = new PrintWriter(dumpFile)) {
            for (int y = 0; y < 128; y++) {
                StringBuilder row = new StringBuilder();

                for (int x = 0; x < 128; x++) {
                    int index = x + y * 128;
                    int rawColorByte = getColorAt(mapColors, x, y);

                    // The %3d formatting ensures every number takes up exactly 3 spaces.
                    // This prevents the grid from shifting when a 1-digit number is next to a 3-digit number!
                    row.append(String.format("%3d ", rawColorByte));
                }

                // Write the full 128-block row to the file
                writer.println(row.toString());
            }

        } catch (IOException e) {
            RedUtilsClient.LOGGER.error("Failed to dump map to file!", e);
        }
    }

    private void scanPixelsAroundPlayer(byte[] mapColors, int playerX, int playerY) {
        // Scan a small 5x5 grid centered exactly on the player
        for (int x = playerX - 2; x <= playerX + 2; x++) {
            for (int y = playerY - 2; y <= playerY + 2; y++) {

                // Make sure we don't check off the edge of the map
                if (x >= 0 && x < 128 && y >= 0 && y < 128) {

                    int index = x + y * 128;
                    byte colorByte = mapColors[index];

                    // Print out every single color byte we find!
                    RedUtilsClient.LOGGER.info("Color Byte Found: {}", colorByte);
                }
            }
        }
    }
}