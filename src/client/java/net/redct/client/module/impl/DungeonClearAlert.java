package net.redct.client.module.impl;

import net.redct.client.config.ColorSetting;
import net.redct.client.config.SliderSetting;
import net.redct.client.config.ToggleSetting;
import net.redct.client.utils.GuiTextUtils;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.utils.DungeonSession;
import net.redct.client.utils.Utils;

import static net.redct.client.utils.GuiTextUtils.sendTitle;
import static net.redct.client.utils.DungeonUtils.*;


public class DungeonClearAlert extends Module {
    public final ToggleSetting makeSound = new ToggleSetting("makeSound", "Sound", true);
    public final SliderSetting volume = new SliderSetting("volume", "Volume", 100, 0, 100)
            .visibleWhen(makeSound::getValue);
    public final ColorSetting color = new ColorSetting("color", "Color", 0xFFFFFFFF);
    public GuiTextUtils guiText = new GuiTextUtils("dungeonAlert",4,12, 1.2f);


    public DungeonClearAlert(){
        super("dungeonClearAlert","Clear alert", Category.DUNGEONS);
        guiText.setText("Dungeon Clear Enabled");
        HudManager.register(guiText, this);
        registerSetting(makeSound);
        registerSetting(volume);
        registerSetting(color);
    }

    @Override
    public void onEnable() {
        guiText.setVisible(true);
    }

    @Override
    public void onDisable() {
        guiText.setVisible(false);
    }


    public static void getMapData(byte[] mapColors, int playerX, int playerY){
        DungeonSession session = DungeonSession.get();
        if (session == null) return; // not in dungeon

        if (!session.isDataInitialized()) {
            session.initializeData(mapColors);
        }

        detectNewRooms(session, mapColors);
        updateAllRoomStatus(session, mapColors, playerX, playerY);

    }


    /**
     *
     * @param mapColors
     * @param room
     * @return true if room status changed
     */
    private static boolean updateRoomStatus(DungeonSession session, byte[] mapColors, Room room){
        int roomStep = session.getRoomStep();

        Utils.Vec2 pos = room.topLeft;
        int xCoord = pos.x() * roomStep + session.getXOffset() ;
        int yCoord = pos.y() * roomStep + session.getYOffset() ;

        int roomMiddle = session.getRoomSize() / 2;
        xCoord += roomMiddle;
        yCoord += roomMiddle;

        int color = mapColors[yCoord * MAP_SIZE + xCoord] & 0xFF;
        if (Type.fromColor(color) == room.type){return false;}

        Status newStatus = Status.fromColor(color);

        if(newStatus == room.status){return false;}
        room.status = newStatus;
        return true;
    }

    // TODO: Move to utils?
    private static void updateAllRoomStatus(DungeonSession session, byte[] mapColors, int playerX, int playerY){
        boolean changed = false;
        Room currentRoom = getRoom(session, playerX, playerY);
        for (Room room: session.getRooms()){
            if(room.status == Status.COMPLETED){continue;}
            switch (room.type) {
                case FAIRY, UNDISCOVERED, NONE, START -> {}
                default -> {
                    // Overwrite each iteration
                    changed = updateRoomStatus(session, mapColors, room);
                    if(currentRoom != null && currentRoom.equals(room) && changed){
                        noticePlayerStatusChange(room.status);
                    }
                }
            }
        }
    }

    private static void noticePlayerStatusChange(Status status){
        Utils.playLocalClientSound();

        String format = ""; //BOLD
        if (status == Status.COMPLETED){
            format = "§a"; //Green
        }
        String text = format + status.toString();

        sendTitle(text, "");
    }

    private static void detectNewRooms(DungeonSession session, byte[] mapColors){
        int rows = session.getMaxRows();
        int cols = session.getMaxCols();
        int step = session.getRoomStep();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int mapX = session.getXOffset() + col * step;
                int mapY = session.getYOffset() + row * step;
                int color = mapColors[mapY * MAP_SIZE + mapX] & 0xFF;
                Type type = Type.fromColor(color);

                // Skip empty or undiscovered cells
                if (type == Type.NONE || type == Type.UNDISCOVERED) continue;

                // Skip if this grid cell is already part of a known room
                if (isKnownCell(session, col, row)) continue;

                if (type != Type.NORMAL){
                    session.addRoom(new Room(col, row, type));
                    continue;
                }

                // Normal room, iterative search
                Room newRoom = new Room(col, row);
                buildRoom(session, mapColors, col, row, type, newRoom);
                session.addRoom(newRoom);
            }

        }
    }

    private static void buildRoom(DungeonSession session, byte[] mapColors, int col, int row, Type type, Room room) {
        room.type = type;

        int maxRows = session.getMaxRows();
        int maxCols = session.getMaxCols();
        int step = session.getRoomStep();
        int xOffset = session.getXOffset();
        int yOffset = session.getYOffset();
        int roomSize = session.getRoomSize();

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int nextCol = col + dir[0];
            int nextRow = row + dir[1];

            // Out of bounds
            if (nextCol < 0 || nextCol >= maxCols || nextRow < 0 || nextRow >= maxRows) {continue;}
            // Already is part of the room
            if (room.hasSegment(nextCol, nextRow)) {continue;}

            int xCoord = xOffset + col * step + dir[0];
            int yCoord = yOffset + row * step + dir[1];

            if (dir[0]>0){
                xCoord += roomSize;
            }
            if(dir[1]>0){
                yCoord += roomSize;
            }

            int color = mapColors[yCoord * MAP_SIZE + xCoord] & 0xFF;
            if (Type.fromColor(color) == Type.NORMAL){
                room.addSegment(nextCol, nextRow);
                buildRoom(session, mapColors, nextCol, nextRow, type, room);
            }
        }
        room.topLeft = room.topLeftSegment();


    }

    private static boolean isKnownCell(DungeonSession session, int gridX, int gridY) {
        for (Room room : session.getRooms())
            for (Utils.Vec2 seg : room.segments)
                if (seg.x() == gridX && seg.y() == gridY) return true;
        return false;
    }

}
