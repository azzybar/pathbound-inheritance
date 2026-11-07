// Essential for setting up the application and main window
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;

// Used for loading font
import java.nio.file.Path;
import java.nio.file.Paths;

// For enemy list
import java.util.List;
import java.util.ArrayList;

public class Project extends Application {
    // Game map and all interactable elements
    private GameMap MAP;
    private List<Chest> chests;
    private List<Enemy> enemies;
    private Player player;
    
    @Override
    public void start(Stage stage) {
        setupGame();
        
        // Creates player and sets up root
        player = new Player(MAP.getTileSize(), 1, 14);
        StackPane root = new StackPane(
            MAP.get(), 
            player.getSprite().getContainer());
        root.setMouseTransparent(true);
            
        // Adds enemies
        for (Enemy enemy : enemies) {
            root.getChildren()
                .add(enemy.getSprite().getContainer());
        }
        
        // Adds chests
        for (Chest chest : chests) {
            root.getChildren()
                .add(chest.getSprite().getContainer());
        }
        
        // Adds chest pane
        root.getChildren().add(Chest.getPopup());
        
        // Creates scene
        Scene scene = new Scene(
            root, 
            MAP.getSceneWidth(), 
            MAP.getSceneHeight(),
            Color.BLACK);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add("Styles/stylesheet.css");
        scene.setOnKeyPressed(event -> handleKeyPress(event, stage));
        
        // Sets stage and title
        stage.setTitle("RPG Game");
        stage.setScene(scene);
        stage.show();
        
        // Post-show adjustments
        Chest.setMargins();
    }
    
    // Handles player movement
    private void handleKeyPress(KeyEvent event, Stage stage) {
        // Checks if player is moving or a popup is open
        if (player.isMoving() || Chest.popupOpen()) {
            return;
        }
        
        // Gets current player's position
        int x = player.getSprite().getX();
        int y = player.getSprite().getY();
        
        // Checks player doesn't go out of bounds
        switch(event.getCode()) {
            case UP:
            case W:
                if (y > 0) {
                    y--;
                }
                break;
            case DOWN:
            case S:
                if (y < MAP.getHeight() - 1) {
                    y++;
                }
                break;
            case LEFT:
            case A:
                if (x > 0) {
                    x--;
                }
                break;
            case RIGHT:
            case D:
                if (x < MAP.getWidth() - 1) {
                    x++;
                }
                break;
            default:
                break;
        }
        
        // Checks if map tile blocks path
        int[][] mapData = MAP.getMapData();
        if (mapData[y][x] > 2) {
            return;
        }
        
        // Checks if space held an enemy or chest
        if (mapData[y][x] == 1) {
            // Goes through the enemy list
            for (Enemy enemy : enemies) {
                // Checks whether the enemy's position matches the current position
                int enemyX = enemy.getSprite().getX();
                int enemyY = enemy.getSprite().getY();
                
                // Checks if enemy was defeated already
                if ((enemyX == x && enemyY == y) && !enemy.wasKilled()) {
                    new BattleEncounter(player, enemy, stage, MAP).start();
                }
            }
        } else if (mapData[y][x] == 2) {
            // Goes through the chests list
            for (Chest chest : chests) {
                // Gets chest's position
                int chestX = chest.getSprite().getX();
                int chestY = chest.getSprite().getY();
                
                // Checks if the position (key) matches
                if (chestX == x && chestY == y) {
                    chest.show(player);
                }
            }
        } else {
            // Updates moving status and moves player
            player.setIsMoving(true);
            player.move(x, y);
        }
    }
    
    // For loading imported fonts and other items
    private void setupGame() {
        // File path for fonts directory
        Path fontPath = Paths.get("Styles/Fonts/EagleLake-Regular.ttf");
        System.out.println("Loading font \u001B[36m" + fontPath.getFileName() + "\u001B[0m");
        Font font = Font.loadFont(
            Project.class.getClassLoader()
            .getResourceAsStream(fontPath.toString()), 
            10);
        System.out.println("\u001B[92mSuccessfully loaded " + font.getName() + "\u001B[0m");
        
        // Loads items (armor and weapons);
        Armor.addArmor();
        Weapon.addWeapons();
        
        // Sets up main game components
        chests = new ArrayList<Chest>();
        enemies = new ArrayList<Enemy>();
        MAP = new GameMap(chests, enemies);
    }
    
    public static void main(String[] args){
        launch(args);
    }
}