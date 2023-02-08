package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.io.File;
import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;

public class Engine {
    /** @source Persistence conducted here using ideas outlined in the course's
     * proj2 skeleton and using P. N. Hilfinger's files copied into the Core folder.
     */
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private ArrayList<ArrayList<Integer>> rooms = new ArrayList<>();
    private static final File CWD = new File(System.getProperty("user.dir"));
    private static final File SAVE = Utils.join(CWD, "save.txt");
    private static final File NAME = Utils.join(CWD, "name.txt");
    private static final File SIGHT = Utils.join(CWD, "sight.txt");
    private int avatarX;
    private int avatarY;
    private int recursiveCount = 0;
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        TETile [][] world;
        String localState = "";
        String avatarName = "avatar";
        boolean sightToggle = false;
        createMainMenu();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                String key = Character.toString(StdDraw.nextKeyTyped()).toLowerCase();
                if (key.equals("n")) {
                    localState = key + readSeed();
                    world = interactWithInputString(localState);
                    break;
                } else if (key.equals("l")) {
                    if (!SAVE.exists()) {
                        System.exit(0);
                    }
                    world = interactWithInputString(Utils.readObject(SAVE, String.class));
                    avatarName = Utils.readObject(NAME, String.class);
                    sightToggle = Utils.readObject(SIGHT, Boolean.class);
                    break;
                } else if (key.equals("q")) {
                    System.exit(0);
                } else if (key.equals("a")) {
                    avatarName = readName();
                    createMainMenu();
                }
            }
        }
        ter.initialize(WIDTH, HEIGHT);
        TETile [][] sightWorld = lineOfSight(world);
        chooseRender(sightToggle, world, sightWorld);
        String pendingKey = null;
        while (true) {
            if (pendingKey != null || StdDraw.hasNextKeyTyped()) {
                String key;
                if (pendingKey != null) {
                    key = pendingKey;
                    pendingKey = null;
                } else {
                    key = Character.toString(StdDraw.nextKeyTyped()).toLowerCase();
                }
                if (key.equals("w") || key.equals("a") || key.equals("s") || key.equals("d")) {
                    moveAvatar(world, key);
                    sightWorld = lineOfSight(world);
                    localState = localState + key;
                } else if (key.equals(":")) {
                    while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                            String key2 = Character.toString(StdDraw.nextKeyTyped()).toLowerCase();
                            if (key2.equals("q")) {
                                if (localState.startsWith("n")) {
                                    Utils.writeObject(SAVE, localState);
                                } else {
                                    String saveState = Utils.readObject(SAVE, String.class);
                                    Utils.writeObject(SAVE, saveState + localState);
                                }
                                Utils.writeObject(NAME, avatarName);
                                Utils.writeObject(SIGHT, sightToggle);
                                System.exit(0);
                            } else {
                                pendingKey = key2;
                            }
                            break;
                        }
                    }
                } else if (key.equals("t")) {
                    sightToggle = !sightToggle;
                    sightWorld = lineOfSight(world);
                }
                chooseRender(sightToggle, world, sightWorld);
            }
            if (sightToggle) {
                drawHUD(sightWorld, avatarName);
            } else {
                drawHUD(world, avatarName);
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        //prepares world for drawing
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
        //creates HUD tiles on top left so rooms wont generate over HUD
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = HEIGHT - 3; y < HEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.HUD_TILE;
            }
        }
        // creates world
        char [] keys = input.toCharArray();
        int keyPointer = 0;
        while (keyPointer < keys.length) {
            String keyVal = Character.toString(keys[keyPointer]).toLowerCase();
            if (keyVal.equals("n")) {
                String seedString = "";
                keyPointer += 1;
                while (!Character.toString(keys[keyPointer]).toLowerCase().equals("s")) {
                    seedString = seedString + keys[keyPointer];
                    keyPointer += 1;
                }
                Long seed = Long.parseLong(seedString);
                Random r = new Random(seed);
                createRandomRooms(finalWorldFrame, r);
                placeHallways(finalWorldFrame, r);
                //places avatar at default location
                avatarX = rooms.get(0).get(0);
                avatarY = rooms.get(0).get(1);
                finalWorldFrame[avatarX][avatarY] = Tileset.AVATAR;
            } else if (keyVal.equals("l")) {
                if (!SAVE.exists()) {
                    return null;
                }
                return interactWithInputString(Utils.readObject(SAVE, String.class)
                        + input.substring(keyPointer + 1));
            } else if (keyVal.equals("w") || keyVal.equals("a")
                    || keyVal.equals("s") || keyVal.equals("d")) {
                moveAvatar(finalWorldFrame, keyVal);
            } else if (keyVal.equals(":") && keyPointer + 1 < keys.length
                    && Character.toString(keys[keyPointer + 1]).toLowerCase().equals("q")) {
                Utils.writeObject(SAVE, input.substring(0, input.length() - 2));
                keyPointer += 1;
            }
            keyPointer += 1;
        }
        return finalWorldFrame;
    }

    private void createRoom(TETile[][] tiles, Random r) {
        //random width and height
        int roomW = r.nextInt(6) + 5;
        int roomH = r.nextInt(6) + 5;
        //generates random location
        int newX = r.nextInt(WIDTH - roomW);
        int newY = r.nextInt(HEIGHT - roomH);
        //checks if nothing is intersecting
        for (int i = newX; i < newX + roomW; i++) {
            for (int j = newY; j < newY + roomH; j++) {
                if (tiles[i][j] != Tileset.NOTHING) {
                    if (recursiveCount < 100) {
                        recursiveCount += 1;
                        createRoom(tiles, r);
                        return;
                    }
                    recursiveCount = 0;
                    return;
                }
            }
        }
        recursiveCount = 0;
        //draws room
        int centerX = newX + roomW / 2;
        int centerY = newY + roomH / 2;
        ArrayList<Integer> room = new ArrayList<>();
        room.add(centerX);
        room.add(centerY);
        rooms.add(room);
        for (int i = newX; i < newX + roomW; i++) {
            for (int j = newY; j < newY + roomH; j++) {
                tiles[i][j] = Tileset.WALL;
            }
        }

        for (int i = newX + 1; i < newX + roomW - 1; i++) {
            for (int j = newY + 1; j < newY + roomH - 1; j++) {
                tiles[i][j] = Tileset.FLOOR;
            }
        }

    }

    private void createRandomRooms(TETile[][] tiles, Random r) {
        int numRooms = r.nextInt(10) + 10;
        for (int i = 0; i < numRooms; i++) {
            createRoom(tiles, r);
        }
    }

    private class CoordinateComparator implements Comparator<ArrayList<Integer>> {
        @Override
        public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
            return o1.get(0) - o2.get(0);
        }
    }

    private void placeHallways(TETile[][] tiles, Random r) {
        CoordinateComparator c = new CoordinateComparator();
        rooms.sort(c);
        for (int i = 0; i < rooms.size() - 1; i++) {
            int x1 = rooms.get(i).get(0);
            int y1 = rooms.get(i).get(1);
            int x2 = rooms.get(i + 1).get(0);
            int y2 = rooms.get(i + 1).get(1);
            createHallway(tiles, x1, y1, x2, y2, r);
        }
    }

    private void createHallway(TETile[][] tiles, int x1, int y1, int x2, int y2, Random r) {
        int path = r.nextInt(2);
        if (y1 < y2) {
            if (path == 0) {
                for (int i = y1; i <= y2; i++) {
                    tiles[x1][i] = Tileset.FLOOR;
                    nothingToWall(tiles, x1 - 1, i);
                    nothingToWall(tiles, x1 + 1, i);
                }
                nothingToWall(tiles, x1 - 1, y2 + 1);
                for (int i = x1; i <= x2; i++) {
                    tiles[i][y2] = Tileset.FLOOR;
                    nothingToWall(tiles, i, y2 - 1);
                    nothingToWall(tiles, i, y2 + 1);
                }
            } else {
                for (int i = x1; i <= x2; i++) {
                    tiles[i][y1] = Tileset.FLOOR;
                    nothingToWall(tiles, i, y1 - 1);
                    nothingToWall(tiles, i, y1 + 1);
                }
                nothingToWall(tiles, x2 + 1, y1 - 1);
                for (int i = y1; i <= y2; i++) {
                    tiles[x2][i] = Tileset.FLOOR;
                    nothingToWall(tiles, x2 - 1, i);
                    nothingToWall(tiles, x2 + 1, i);
                }
            }
        } else {
            if (path == 0) {
                for (int i = y1; i >= y2; i--) {
                    tiles[x1][i] = Tileset.FLOOR;
                    nothingToWall(tiles, x1 - 1, i);
                    nothingToWall(tiles, x1 + 1, i);
                }
                nothingToWall(tiles, x1 - 1, y2 - 1);
                for (int i = x1; i <= x2; i++) {
                    tiles[i][y2] = Tileset.FLOOR;
                    nothingToWall(tiles, i, y2 - 1);
                    nothingToWall(tiles, i, y2 + 1);
                }
            } else {
                for (int i = x1; i <= x2; i++) {
                    tiles[i][y1] = Tileset.FLOOR;
                    nothingToWall(tiles, i, y1 - 1);
                    nothingToWall(tiles, i, y1 + 1);
                }
                nothingToWall(tiles, x2 + 1, y1 + 1);
                for (int i = y1; i >= y2; i--) {
                    tiles[x2][i] = Tileset.FLOOR;
                    nothingToWall(tiles, x2 - 1, i);
                    nothingToWall(tiles, x2 + 1, i);
                }
            }
        }
    }

    private void nothingToWall(TETile[][] tiles, int x, int y) {
        if (tiles[x][y] == Tileset.NOTHING) {
            tiles[x][y] = Tileset.WALL;
        }
    }

    //gets the tile type of the tile that is being hovered over by mouse
    //returns a String indicating the tile type
    private static String getTile(TETile[][] tiles, String avatarName) {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        if (y > HEIGHT - 1) { //for off by 1 out of bounds exception
            y = HEIGHT - 1;
        }
        TETile tile = tiles[x][y];
        if (tile == Tileset.FLOOR) {
            return "floor";
        } else if (tile == Tileset.WALL) {
            return "wall";
        } else if (tile == Tileset.AVATAR) {
            return avatarName;
        } else {
            return "nothing";
        }
    }

    //draws the HUD with updates
    private static void drawHUD(TETile[][] tiles, String avatarName) {
        String hud = getTile(tiles, avatarName);
        StdDraw.setPenColor(StdDraw.DARK_GRAY);
        //replacement for clear(); draw black rectangle on top left to erase previous HUD
        StdDraw.filledRectangle(4, 28.5, 4, 1.5);
        StdDraw.filledRectangle(68, 28.5, 8, 1.5);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH * 0.05, 0.95 * HEIGHT, hud); //write the HUD
        StdDraw.text(WIDTH * 0.85, 0.95 * HEIGHT, "Toggle Line of Sight (T)");
        StdDraw.show();
    }

    private void moveAvatar(TETile[][] tiles, String key) {
        if (key.equals("w") && tiles[avatarX][avatarY + 1] == Tileset.FLOOR) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            tiles[avatarX][avatarY + 1] = Tileset.AVATAR;
            avatarY += 1;
        } else if (key.equals("a") && tiles[avatarX - 1][avatarY] == Tileset.FLOOR) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            tiles[avatarX - 1][avatarY] = Tileset.AVATAR;
            avatarX -= 1;
        } else if (key.equals("s") && tiles[avatarX][avatarY - 1] == Tileset.FLOOR) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            tiles[avatarX][avatarY - 1] = Tileset.AVATAR;
            avatarY -= 1;
        } else if (key.equals("d") && tiles[avatarX + 1][avatarY] == Tileset.FLOOR) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            tiles[avatarX + 1][avatarY] = Tileset.AVATAR;
            avatarX += 1;
        }
    }

    private void createMainMenu() {
        int width = 40;
        int height = 40;
        StdDraw.setCanvasSize(width * 20, height * 20);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(width / 2, 0.8 * height, "CS61B: THE GAME");
        StdDraw.text(0.5 * width, 0.6 * height, "NEW GAME (N)");
        StdDraw.text(0.5 * width, 0.55 * height, "LOAD GAME (L)");
        StdDraw.text(0.5 * width, 0.5 * height, "CREATE/CHANGE NAME (A)");

        StdDraw.text(0.5 * width, 0.35 * height, "QUIT (Q)");

        StdDraw.show();
    }

    private void createEnterString(String s) {
        int width = 40;
        int height = 40;
        StdDraw.clear(Color.BLACK);
        StdDraw.text(width / 2, 0.8 * height, "Enter seed. Press S to finish");
        StdDraw.text(0.5 * width, 0.5 * height, s);

        StdDraw.show();
    }

    private String readSeed() {
        String holder = "";
        createEnterString(holder);
        while (!holder.toLowerCase().contains("s")) {
            if (StdDraw.hasNextKeyTyped()) {
                holder += Character.toString(StdDraw.nextKeyTyped());
                createEnterString(holder);
            }
        }
        return holder;
    }
    //Add a menu option to give your avatar a name which is displayed on the HUD.
    private void createNameString(String s) {
        int width = 40;
        int height = 40;
        StdDraw.clear(Color.BLACK);
        StdDraw.text(width / 2, 0.8 * height,
                "Give your avatar a username; press spacebar to finish");
        StdDraw.text(0.5 * width, 0.5 * height, s);

        StdDraw.show();
    }

    private String readName() {
        String holder = "";
        createNameString(holder);
        while (!holder.contains(" ")) {
            if (StdDraw.hasNextKeyTyped()) {
                holder += Character.toString(StdDraw.nextKeyTyped());
                createNameString(holder);
            }
        }
        return holder;
    }

    private TETile[][] lineOfSight(TETile[][] world) {
        TETile [][] onlyInSight = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                onlyInSight[i][j] = Tileset.NOTHING;
            }
        }
        int leftBound = 0;
        int rightBound = WIDTH;
        int bottomBound = 0;
        int topBound = HEIGHT;
        if (avatarX - 2 > leftBound) {
            leftBound = avatarX - 2;
        }
        if (avatarX + 2 < rightBound) {
            rightBound = avatarX + 2;
        }
        if (avatarY - 2 > bottomBound) {
            bottomBound = avatarY - 2;
        }
        if (avatarY + 2 < topBound) {
            topBound = avatarY + 2;
        }
        for (int i = leftBound; i <= rightBound; i++) {
            for (int j = bottomBound; j <= topBound; j++) {
                onlyInSight[i][j] = world[i][j];
            }
        }
        return onlyInSight;
    }

    private void chooseRender(boolean sightToggle, TETile[][] world, TETile[][] sightWorld) {
        if (sightToggle) {
            ter.renderFrame(sightWorld);
        } else {
            ter.renderFrame(world);
        }
    }
}
