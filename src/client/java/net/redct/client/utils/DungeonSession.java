package net.redct.client.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.redct.client.utils.DungeonUtils.FLOOR_ROOM_SIZE;
import static net.redct.client.utils.DungeonUtils.getTopLeft;

public class DungeonSession {
    private static DungeonSession instance;

    // State
    private final boolean isMasterMode;
    private final int floor;
    private final List<DungeonUtils.Room> rooms = new ArrayList<>();
    private final List<DungeonUtils.Room> roomsView = Collections.unmodifiableList(rooms);
    private boolean isDataInitialized = false;

    // Sizes
    private int xOffset = -1, yOffset = -1;
    private int maxCols = -1, maxRows = -1;
    private int roomStep = -1;

    private DungeonSession(int floor, boolean masterMode) {
        this.floor = floor;
        this.isMasterMode = masterMode;

    }

    // Lifecycle
    public static DungeonSession start(int floor, boolean masterMode) {
        instance = new DungeonSession(floor, masterMode);
        return instance;
    }

    public static void end() {
        instance = null;
    }

    public static DungeonSession get() {
        return instance;
    }

    public static boolean isActive() {
        return instance != null;
    }

    // Write
    public void initializeData(byte[] mapColors){
        if (isDataInitialized) return;
        this.roomStep = getRoomSize() + DungeonUtils.ROOM_GAP;

        Utils.Vec2 tempPos = calculateRoomOffset(mapColors);
        if(tempPos == null) return;
        xOffset = tempPos.x();
        yOffset = tempPos.y();

        maxCols = (DungeonUtils.MAP_SIZE-(xOffset*2)+DungeonUtils.ROOM_GAP)/roomStep;
        maxRows = (DungeonUtils.MAP_SIZE-(yOffset*2)+DungeonUtils.ROOM_GAP)/roomStep;

        isDataInitialized = true;
    }

    private Utils.Vec2 calculateRoomOffset( byte[] mapColors) {

        Utils.Vec2 base = getTopLeft(mapColors);
        if (base == null) return null;

        int x = base.x();
        int y = base.y();

        while ((x - roomStep) > 0){
            x-= roomStep;
        }
        while ((y - roomStep) > 0){
            y-= roomStep;
        }
        return new Utils.Vec2(x, y);
    }

    public void addRoom(DungeonUtils.Room room) {
        rooms.add(room);
    }

    // Reads
    public int getFloor()              { return floor; }
    public boolean isMasterMode()      { return isMasterMode; }
    public int getXOffset()            { return xOffset; }
    public int getYOffset()            { return yOffset; }
    public int getMaxRows()            { return maxRows; }
    public int getMaxCols()            { return maxCols; }
    public int getRoomStep()           { return roomStep; }
    public boolean isDataInitialized() { return isDataInitialized; }

    public int getRoomSize() {
        return FLOOR_ROOM_SIZE[floor];
    }

    public List<DungeonUtils.Room> getRooms() {
        return roomsView;
    }
}
