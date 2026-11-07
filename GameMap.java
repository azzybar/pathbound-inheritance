import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Used for setting up the map
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.stream.Collectors;

// For holding map images
import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class GameMap {
    // Information regarding map and screen size
    private final int TILE_SIZE;
    private final int MAP_WIDTH;
    private final int MAP_HEIGHT;
    private GridPane MAP;
    private int[][] mapData;
    
    // Map tiles and entity sprites
    private Map<String,Image> mapTiles;
    private Map<String,Image> sprites;
    private List<Chest> chests;
    private List<Enemy> enemies;
    
    /*
    MAP TILES:
    - path = 0
        - path-corner (defaullt is bottom-right)
        - path-fourway
        - path-threeway (default is bottom)
    - enemy = 1
    - chest = 2
    - grass = 3 (random)
    - rocks = 3 (random)
    - trees = 3 (random)
    - building = 4
    */
    
    public GameMap(List<Chest> chests, List<Enemy> enemies) {
        // 640x480
        this.TILE_SIZE = 32;
        this.MAP_WIDTH = 20;
        this.MAP_HEIGHT = 15;
        
        // Interactables
        this.chests = chests;
        this.enemies = enemies;
        
        // Setup methods
        loadImages("map");
        loadImages("sprites");
        createMap();
    }
    
    // Removes an enemy after they die
    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
    }
    
    private void createMap() {
        /**
         * Javafx is structured [col][row] (x and y are switched)
         * You need to switch them in order for a 2D array's output
         * to match the screen dimensions the right orientation if
         * it's a rectangle.
         * 
         * 0 1 2 3 4 5 6 7 8 9
         * 0 1 2 3 4 5 6 7 8 9
         * 0 1 2 3 4 5 6 7 8 9
         * 0 1 2 3 4 5 6 7 8 9
         * 
         * So [0][1] would return 1; however, that would
         * translate to x = 0, y = 1, which is 0 in the array
         */
        mapData = new int[MAP_HEIGHT][MAP_WIDTH];
        Path mapPath = Paths.get("Maps/map-tiles.txt");
        System.out.println("Retrieving map data from: \u001B[36m" + mapPath.toString() + "\u001B[0m");
        
        // Assigns each tile's image value, reader auto-closes
        try (BufferedReader br = Files.newBufferedReader(mapPath)) {
            String line;
            for (int row = 0; row < MAP_HEIGHT; row++) {
                // Checks that there is a line to read
                if ((line = br.readLine()) != null) {
                    // Creates a String array of the data
                    int index = 0;
                    String[] data = line.split(" ");
                    
                    // Goes through each number and adds it
                    for (int col = 0; col < MAP_WIDTH; col++) {
                        mapData[row][col] = Integer.parseInt(data[index]);
                        
                        // Checks and updates chest count
                        if (mapData[row][col] == 2) {
                            Chest.updateCount();
                        }
                        
                        index++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\u001B[92mSuccessfully retrieved map data\u001B[0m");
        
        // Sets visual map based on mapData
        System.out.println("Initializing new game map");
        MAP = new GridPane();
        for (int row = 0; row < MAP_WIDTH; row++) {
            for (int col = 0; col < MAP_HEIGHT; col++) {
                ImageView tile = null;
                
                // Path for ending or starting
                if ((col == 0 && row == 18) || 
                    (col == 14 && row == 1)) {
                    tile = new ImageView(mapTiles.get("path"));
                    tile.getStyleClass().add("image-view");
                    tile.setFocusTraversable(false);
                    MAP.add(tile, row, col);
                    continue;
                }
                
                // Switches image based on the stored value
                switch (mapData[col][row]) {
                    case 1:
                        addEnemy(col, row);
                    case 0:
                        tile = determinePath(col, row);
                        break;
                    case 2:
                        // Creates a chest object
                        Chest chest = new Chest(TILE_SIZE, mapTiles.get("chest"), row, col);
                        chests.add(chest);
                        Rectangle spot = new Rectangle(TILE_SIZE, TILE_SIZE, Color.BLACK);
                        MAP.add(spot, row, col);
                        break;
                    case 3:
                        // Randomly picks grass or rock tile
                        int t = (int) (Math.random() * 10);
                        tile = new ImageView((t < 2) ? mapTiles.get("rocks") 
                            : (t < 5) ? mapTiles.get("grass")
                            : mapTiles.get("tree"));
                        
                        // Determines rotation (only for grass and rock)
                        if (t < 5) { 
                            int r = (int) (Math.random() * 4);
                            tile.setRotate(r * 90);
                        }
                        break;
                    case 4:
                        tile = new ImageView(mapTiles.get("building"));
                        break;
                    default:
                        break;
                }
                
                // Adds tile to map
                if (tile != null) {
                    tile.getStyleClass().add("image-view");
                    tile.setFocusTraversable(false);
                    MAP.add(tile, row, col);
                }
            }
        }
        System.out.println("\u001B[92mSuccessfully created game map\u001B[0m");
    }
    
    // Determines which path image to use
    private ImageView determinePath(int col, int row) {
        // Signals whether that direction is walkable
        Map<String,Boolean> walkable = new HashMap<String,Boolean>();
        walkable.put("north", false);
        walkable.put("east", false);
        walkable.put("south", false);
        walkable.put("west", false);
        
        // Checks which directions are walkable
        if (col - 1 >= 0 
            && (mapData[col - 1][row] == 0 
            || mapData[col - 1][row] == 1)) {
            walkable.replace("north", true);
        }
        
        if (row + 1 < MAP_WIDTH 
            && (mapData[col][row + 1] == 0 
            || mapData[col][row + 1] == 1)) {
            walkable.replace("east", true);
        }
        
        if (col + 1 < MAP_HEIGHT 
            && (mapData[col + 1][row] == 0 
            || mapData[col + 1][row] == 1)) {
            walkable.replace("south", true);
        }
        
        if (row - 1 >= 0 
            && (mapData[col][row - 1] == 0 
            || mapData[col][row - 1] == 1)) {
            walkable.replace("west", true);
        }
        
        // Counts how many directions are walkable (should be at least 1)
        int openPaths = 0;
        for (boolean value : walkable.values()) {
            if (value) {
                openPaths++;
            }
        }
        
        // Narrows path options
        int r = 0;
        ImageView tile = null;
        switch (openPaths) {
            case 1:
                // End paths (for enemies)
                tile = new ImageView(mapTiles.get("path-end"));
                for (Map.Entry<String,Boolean> entry : walkable.entrySet()) {
                    // Finds the beginning
                    if (entry.getValue()) {
                        switch (entry.getKey()) {
                            case "north":
                                r = 2;
                                break;
                            case "east":
                                r = 3;
                                break;
                            case "west":
                                r = 1;
                                break;
                            default:
                                r = 0;
                                break;
                        }
                    }
                }
                break;
            case 2:
                // Checks whether straight or corner
                if (isCorner(walkable)) {
                    tile = new ImageView(mapTiles.get("path-corner"));
                    
                    // Determines how far to rotate
                    boolean rOne = walkable.get("south") && walkable.get("west");
                    boolean rTwo = walkable.get("west") && walkable.get("north");
                    boolean rThree = walkable.get("north") && walkable.get("east");
                    
                    r = rOne ? 1 :
                        rTwo ? 2 :
                        rThree ? 3 : 0;
                } else {
                    tile = new ImageView(mapTiles.get("path"));
                    
                    // Checks if it's horizontal
                    boolean isHoriz = walkable.get("east") && walkable.get("west");
                    r = isHoriz ? 1 : 0;
                }
                break;
            case 3:
                tile = new ImageView(mapTiles.get("path-threeway"));
                
                // Finds which direction to rotate
                boolean rOne = walkable.get("south") 
                    && walkable.get("west") 
                    && walkable.get("north");
                boolean rTwo = walkable.get("west") 
                    && walkable.get("north") 
                    && walkable.get("east");
                boolean rThree = walkable.get("north") 
                    && walkable.get("east") 
                    && walkable.get("south");
                    
                r = rOne ? 1 :
                    rTwo ? 2 :
                    rThree ? 3 : 0;
                break;
            case 4:
                // Fourway is direction-independent
                tile = new ImageView(mapTiles.get("path-fourway"));
                break;
            default:
                break;
        }
        
        // Rotates and returns image
        if (tile != null) {
            tile.setRotate(r * 90);
        }
        
        return tile;
    }
    
    // Determines whether a tile is a corner or straight
    private boolean isCorner(Map<String,Boolean> walkable) {
        // All possible combinations for a straight path
        boolean verticalPath = walkable.get("north") && walkable.get("south");
        boolean horizontalPath = walkable.get("east") && walkable.get("west");
        
        // Returns false if any of them are true
        return ((verticalPath || horizontalPath) ? false : true);
    }
    
    // Adds an enemy to the map
    private void addEnemy(int x, int y) {
        // Possible enemies
        String[] types = {
            "bandit",
            "goblin",
            "knight",
            "ogre",
            "shade",
            "skeleton"
        };
        int t = (int) (Math.random() * types.length);
        
        // Creates a randomized enemy
        Enemy enemy = new Enemy(TILE_SIZE, sprites.get(types[t]), y, x, types[t]);
        enemies.add(enemy);
    }
    
    // Loads map tiles and entity sprites
    private void loadImages(String type) {
        Path dir = null;
        if (type.equals("map")) {
            // Loads all map tiles
            mapTiles = new HashMap<String,Image>();
            dir = Paths.get("Maps/Images");
        } else {
            // Loads all enemy sprites
            sprites = new HashMap<String,Image>();
            dir = Paths.get("Sprites/Enemies");
        }
        System.out.printf("Loading %s from: \u001B[36m%s\u001B[0m\n", 
            (type.equals("map") ? "map tiles" : "sprites"),
            dir.toString());
            
        // Creates a stream
        try (Stream<Path> stream = Files.walk(dir)) {
            List<Path> files = stream
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith("png"))
                .collect(Collectors.toList());
            files.forEach(p -> processImage(p, (type.equals("map") ? "map" : "sprite")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("\u001B[92mSuccessfully loaded %s\u001B[0m\n", (type.equals("map") ? "map tiles" : "sprites"));
    }
    
    // For processing and sorting images
    private void processImage(Path path, String type) {
        // Cuts name to just a descriptor
        String name = path.getFileName().toString();
        name = name.substring(0, name.indexOf(".png"));
        
        // Adds to hash map
        try {
            if (type.equals("map")) {
                // Adds to map hash
                Image image = new Image(Files.newInputStream(path), TILE_SIZE, TILE_SIZE, true, true);
                mapTiles.put(name, image);
            } else {
                // Adds to sprite hash
                Image image = new Image(Files.newInputStream(path), (TILE_SIZE * 0.875), (TILE_SIZE * 0.875), true, true);
                sprites.put(name, image);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Getters
    public int getTileSize() { return TILE_SIZE; }
    public int getWidth() { return MAP_WIDTH; }
    public int getHeight() { return MAP_HEIGHT; }
    public double getSceneWidth() { return TILE_SIZE * MAP_WIDTH; }
    public double getSceneHeight() { return TILE_SIZE * MAP_HEIGHT; }
    public GridPane get() { return MAP; }
    public int[][] getMapData() { return mapData; }
}