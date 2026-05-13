package net.redct.client.utils;

import net.redct.client.RedUtilsClient;

import java.util.ArrayList;
import java.util.List;

public class DungeonUtils {
    // ── Sizes ────────────────────────────────────────────────────────────────
    // TODO: igual hay que hacerlo de forma dinamica en los casos que la dungeon no es un rectangulo
    public static final int[] FLOOR_ROOM_SIZE = {18, 18, 18, 18, 18, 16, 16, 16};

    // TODO: Creo que funciona pero lo he hecho dinamico :(
    //public static final int[] FLOOR_ROOM_XOFFSET = {22, 22, 13, 13, 13, 5, 5, 5};
    //public static final int[] FLOOR_ROOM_YOFFSET = {11, 11, 11, 11, 11, 5, 5, 5};
    public static final int ROOM_GAP = 4;
    public static final int MAP_SIZE = 128;


    // ── Enums ────────────────────────────────────────────────────────────────

    public enum Type {
        UNDISCOVERED(85),
        NORMAL(63),
        PUZZLE(66),
        TRAP( 62),
        BLOOD(18),
        MINIBOSS(74),
        START(30),
        FAIRY(82),
        RUSH(119),
        NONE(0);

        private final int color;

        Type(int color){
            this.color = color;
        }

        public int getColor(){
            return color;
        }

        public static Type fromColor(int color){
            for (Type t : values())
                if (t.color == color) return t;
            return NONE;
        }
    }

    public enum Status{
        NORMAL(-1),
        CLEAR(34),        // White check
        COMPLETED(30),      // Green check
        UNDISCOVERED(119),  // Black question
        UNKNOWN(-1);

        private final int color;

        Status(int color){
            this.color = color;
        }

        public int getColor(){return color;}

        public static Status fromColor(int color){
            for (Status s : values())
                if (s.color == color) return s;
            return UNKNOWN;
        }
    }

    // ── Data classes ─────────────────────────────────────────────────────────
        public record Position(int x, int y) {

        @Override
            public String toString() {
                return String.format("[%d,%d]", x, y);
            }

            @Override
            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                }

                if (!(o instanceof Position p)) {
                    return false;
                }
                return (this.x == p.x && this.y == p.y);
            }
        }

    public static class Room{

        public Position topLeft;
        public Status status;
        public Type type;
        // Room shape in dungeon
        public List<Position> segments;

        public Room(int x, int y, Type type){
            this.topLeft = new Position(x, y);
            this.status = Status.UNKNOWN;
            this.type = type;
            this.segments = new ArrayList<>();
            addSegment(x, y);
        }

        public Room(int x, int y){
            this(x, y, Type.NONE);
        }


        public void addSegment(int x, int y) {
            segments.add(new Position(x, y));
        }

        public boolean hasSegment(int x, int y){
            for (Position segment: segments){
                if (segment.x == x && segment.y == y) {
                    return true;
                }
            }
            return false;
        }

        public Position topLeftSegment(){
            if (segments == null || segments.isEmpty()) {return null;}
            Position topLeft = segments.get(0);

            for(Position segment: segments){
                if (segment.y < topLeft.y || (segment.y == topLeft.y && segment.x < topLeft.x)) {
                    topLeft = segment;
                }
            }
            return topLeft;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Room r)) {
                return false;
            }
            return (this.topLeft.equals(r.topLeft));
        }
    }

    /* Never used???
    public static Type getRoomColor(byte[] mapColors, int gridX, int gridY){
        int xCoord = gridX * roomStep - ROOM_GAP + XOffset;
        int yCoord = gridY * roomStep - ROOM_GAP + YOffset;

        int color = mapColors[yCoord * MAP_SIZE + xCoord] & 0xFF;

        return Type.fromColor(color);
    }
    */

    public static Room getRoom(DungeonSession session, int xCoord, int yCoord){

        int roomStep = session.getRoomStep();

        int gridX = (xCoord - session.getXOffset()) / roomStep;
        int gridY = (yCoord - session.getYOffset()) / roomStep;
        for (Room room: session.getRooms()){
            if(room.hasSegment(gridX, gridY)){
                return room;
            }
        }
        return null;
    }

    public static Position getTopLeft(byte[] mapColors){
        for (int x = 0; x < MAP_SIZE; x++){
            for (int y = 0; y < MAP_SIZE; y++){
                if ((mapColors[y * MAP_SIZE + x] & 0xFF)!= 0){
                    return new Position(x, y);
                }
            }
        }
        return null;
    }
    /*
    ---------------------------------------------------------------------
     */


    public static boolean checkDungeon(){
        List<String> scoreboardLines = ScoreboardUtils.scoreboardLines;

        if (scoreboardLines == null){
            if(DungeonSession.get() != null){DungeonSession.end();}
            return false;
        }
        int floor = 0;
        boolean isMasterMode = false;

        for (String line: scoreboardLines){
            if (line.contains("Catacombs")){
                floor = line.charAt(18) - '0';
                switch (line.charAt(17)){
                    case 'M':
                        isMasterMode = true;
                        break;
                    case 'E': //Entrance
                        floor = 0;
                        break;
                    case 'F':
                        break;
                    default:
                        RedUtilsClient.LOGGER.error("[ERROR] Detected wrong dungeon mode at line \"{}\" char '{}' ",line, line.charAt(17));
                        return false;
                        //Da hell?
                }
                if (floor < 0 || floor >= DungeonUtils.FLOOR_ROOM_SIZE.length) {
                    RedUtilsClient.LOGGER.error("[ERROR] Invalid floor '{}' detected in scoreboard line: {}", floor, line);
                    return false;
                }

                DungeonSession instance = DungeonSession.get();
                if (instance == null || instance.getFloor() != floor || instance.isMasterMode() != isMasterMode) {
                    DungeonSession.end();
                    DungeonSession.start(floor, isMasterMode);
                }

                return true;
            }
        }

        if(DungeonSession.get() != null){DungeonSession.end();}
        return false;
    }
}
