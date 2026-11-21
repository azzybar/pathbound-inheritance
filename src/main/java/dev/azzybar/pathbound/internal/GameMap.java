// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Project;

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
import java.util.stream.Stream;
import java.util.stream.Collectors;

// For map images and setting up lists
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class GameMap {
    // Information regarding map and screen size
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 20;
    public static final int HEIGHT = 15;
    private final GridPane map;
    private final int[][] mapData;
    
    // Map tiles and entity sprites
    private final Map<String,Image> mapTiles;
    private final Map<String,Image> sprites;
    private final List<Chest> chests;
    private final List<Enemy> enemies;
    
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
    
    public GameMap() {
        // Map data
        this.map = new GridPane();
        this.mapData = new int[HEIGHT][WIDTH];

        // Images for tiles and sprites
        this.mapTiles = new HashMap<String,Image>();
        this.sprites = new HashMap<String,Image>();

        // Interactables
        this.chests = new ArrayList<Chest>();
        this.enemies = new ArrayList<Enemy>();
        
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
        Path mapPath = Project.APP_DIR.resolve("maps", "map-tiles.txt");
        System.out.println("Retrieving map data from: \u001B[36m" + mapPath + "\u001B[0m");
        
        // Assigns each tile's image value, reader auto-closes
        try (BufferedReader br = Files.newBufferedReader(mapPath)) {
            String line;
            for (int row = 0; row < HEIGHT; row++) {
                // Checks that there is a line to read
                if ((line = br.readLine()) != null) {
                    // Creates a String array of the data
                    int index = 0;
                    String[] data = line.split(" ");
                    
                    // Goes through each number and adds it
                    for (int col = 0; col < WIDTH; col++) {
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
        for (int row = 0; row < WIDTH; row++) {
            for (int col = 0; col < HEIGHT; col++) {
                ImageView tile = new ImageView();
                tile.setSmooth(false);
                
                // Path for ending or starting
                if ((col == 0 && row == 18) || 
                    (col == 14 && row == 1)) {
                    tile.setImage(mapTiles.get("path"));
                    tile.getStyleClass().add("image-view");
                    tile.setFocusTraversable(false);
                    map.add(tile, row, col);
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
                        Chest chest = new Chest(mapTiles.get("chest"), row, col);
                        chests.add(chest);
                        Rectangle spot = new Rectangle(TILE_SIZE, TILE_SIZE, Color.BLACK);
                        map.add(spot, row, col);
                        break;
                    case 3:
                        // Randomly picks grass or rock tile
                        int t = (int) (Math.random() * 10);
                        tile.setImage((t < 2) ? mapTiles.get("rocks")
                            : (t < 5) ? mapTiles.get("grass")
                            : mapTiles.get("tree"));
                        
                        // Determines rotation (only for grass and rock)
                        if (t < 5) { 
                            int r = (int) (Math.random() * 4);
                            tile.setRotate(r * 90);
                        }
                        break;
                    case 4:
                        tile.setImage(mapTiles.get("building"));
                        break;
                    default:
                        break;
                }
                
                // Adds tile to map
                if (tile.getImage() != null && !tile.getImage().isError()) {
                    tile.getStyleClass().add("image-view");
                    tile.setFocusTraversable(false);
                    map.add(tile, row, col);
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
        
        if (row + 1 < WIDTH
            && (mapData[col][row + 1] == 0 
            || mapData[col][row + 1] == 1)) {
            walkable.replace("east", true);
        }
        
        if (col + 1 < HEIGHT
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
        ImageView tile = new ImageView();
        tile.setSmooth(false);
        switch (openPaths) {
            case 1:
                // End paths (for enemies)
                tile.setImage(mapTiles.get("path-end"));
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
                    tile.setImage(mapTiles.get("path-corner"));
                    
                    // Determines how far to rotate
                    boolean rOne = walkable.get("south") && walkable.get("west");
                    boolean rTwo = walkable.get("west") && walkable.get("north");
                    boolean rThree = walkable.get("north") && walkable.get("east");
                    
                    r = rOne ? 1 :
                        rTwo ? 2 :
                        rThree ? 3 : 0;
                } else {
                    tile.setImage(mapTiles.get("path"));
                    
                    // Checks if it's horizontal
                    boolean isHoriz = walkable.get("east") && walkable.get("west");
                    r = isHoriz ? 1 : 0;
                }
                break;
            case 3:
                tile.setImage(mapTiles.get("path-threeway"));
                
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
                tile.setImage(mapTiles.get("path-fourway"));
                break;
            default:
                break;
        }
        
        // Rotates and returns image
        if (tile.getImage() != null && !tile.getImage().isError()) {
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
        Enemy enemy = new Enemy(sprites.get(types[t]), y, x, types[t]);
        enemies.add(enemy);
    }
    
    // Loads map tiles and entity sprites
    private void loadImages(String type) {
        Path dir = null;
        if (type.equals("map")) {
            // Loads all map tiles
            dir = Project.APP_DIR.resolve("maps", "images");
        } else {
            // Loads all enemy sprites
            dir = Project.APP_DIR.resolve("sprites", "enemies");
        }
        System.out.printf("Loading %s from: \u001B[36m%s\u001B[0m\n", 
            (type.equals("map") ? "map tiles" : "sprites"),
            dir);
            
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
    public GridPane getView() { return map; }
    public int[][] getMapData() { return mapData; }
    public List<Chest> getChests() { return chests; }
    public List<Enemy> getEnemies() { return enemies; }
}
