// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;
import dev.azzybar.pathbound.internal.GameMap;
import dev.azzybar.pathbound.internal.Images;
import dev.azzybar.pathbound.internal.Chest;
import dev.azzybar.pathbound.internal.Enemy;
import dev.azzybar.pathbound.internal.Player;
import dev.azzybar.pathbound.internal.BattleEncounter;
import dev.azzybar.pathbound.internal.Armor;
import dev.azzybar.pathbound.internal.Weapon;

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

// Used for getting app dir and loading assets
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

// For enemy list
import java.util.List;
import java.util.ArrayList;

// Listing module version if set
import java.util.Optional;
import java.lang.module.ModuleDescriptor;

public class Project extends Application {
    public static final Path APP_DIR = resolveAppDir();
    public static final String CSS_PATH = Project.class.getResource("/styles/stylesheet.css").toExternalForm();

    // Native screen resolution (640x480)
    public static final double NATIVE_WIDTH = GameMap.TILE_SIZE * GameMap.WIDTH;
    public static final double NATIVE_HEIGHT = GameMap.TILE_SIZE * GameMap.HEIGHT;

    // Game map and all interactable elements
    private GameMap map;
    private Player player;
    
    @Override
    public void start(Stage stage) {
        setupGame();
        
        // Creates player and sets up root
        Path playerPath = APP_DIR.resolve("sprites", "player", "player.png");
        try (InputStream is = Files.newInputStream(playerPath)) {
            player = new Player(Images.load(is), 1, 14);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StackPane root = new StackPane(
            map.getView(),
            player.getSprite().getContainer());
        root.setMouseTransparent(true);
            
        // Adds enemies
        for (Enemy enemy : map.getEnemies()) {
            root.getChildren()
                .add(enemy.getSprite().getContainer());
        }

        // Adds chests
        for (Chest chest : map.getChests()) {
            root.getChildren()
                .add(chest.getSprite().getContainer());
        }
        
        // Adds chest pane
        root.getChildren().add(Chest.getPopup());
        
        // Creates scene
        Scene scene = new Scene(
            root, 
            NATIVE_WIDTH,
            NATIVE_HEIGHT,
            Color.BLACK);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add(CSS_PATH);
        scene.setOnKeyPressed(event -> handleKeyPress(event, stage));
        
        // Sets stage and title
        String title = "Pathbound: Inheritance";
        ModuleDescriptor descriptor = Project.class.getModule().getDescriptor();
        if (descriptor != null) {
            Optional<ModuleDescriptor.Version> moduleVer = descriptor.version();
            if (moduleVer.isPresent()) {
                title += " (v" + moduleVer.get() + ")";
            }
        }
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(false);
        String iconPath = "/styles/icons/icon.png";
        try (InputStream is = Project.class.getResourceAsStream(iconPath)) {
            Objects.requireNonNull(is, "Image InputStream cannot be null");
            stage.getIcons().add(Images.load(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                if (y < GameMap.HEIGHT - 1) {
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
                if (x < GameMap.WIDTH - 1) {
                    x++;
                }
                break;
            default:
                break;
        }
        
        // Checks if map tile blocks path
        int[][] mapData = map.getMapData();
        if (mapData[y][x] > 2) {
            return;
        }
        
        // Checks if space held an enemy or chest
        if (mapData[y][x] == 1) {
            // Goes through the enemy list
            for (Enemy enemy : map.getEnemies()) {
                // Checks whether the enemy's position matches the current position
                int enemyX = enemy.getSprite().getX();
                int enemyY = enemy.getSprite().getY();
                
                // Checks if enemy was defeated already
                if ((enemyX == x && enemyY == y) && !enemy.wasKilled()) {
                    new BattleEncounter(player, enemy, stage, map).start();
                }
            }
        } else if (mapData[y][x] == 2) {
            // Goes through the chests list
            for (Chest chest : map.getChests()) {
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
        Font font;
        String fontPath = "/styles/fonts/EagleLake-Regular.ttf";
        System.out.println("Loading font \u001B[36m" + fontPath + "\u001B[0m");
        try (InputStream is = Project.class.getResourceAsStream(fontPath)) {
            if (is != null) {
                font = Font.loadFont(is, 10);
                System.out.println("\u001B[92mSuccessfully loaded " + font.getName() + "\u001B[0m");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Loads items (armor and weapons) and map
        Armor.addArmor();
        Weapon.addWeapons();
        map = new GameMap();
    }
    
    public static void main(String[] args){
        launch(args);
    }

    private static Path resolveAppDir() {
        // If launched after jpackage
        String appDir = System.getProperty("app.dir");
        if (appDir != null) {
            return Path.of(appDir).toAbsolutePath();
        }

        // If launched after creating jar file
        try {
            Path codeLocation = Path.of(Project.class.getProtectionDomain()
                                               .getCodeSource()
                                               .getLocation()
                                               .toURI());
            if (Files.isRegularFile(codeLocation)) {
                return codeLocation.getParent();
            }
        } catch (URISyntaxException e) {
        }

        // Running from the compiled class files
        return Path.of("").toAbsolutePath();
    }
}
