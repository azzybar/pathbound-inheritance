// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;
import dev.azzybar.pathbound.internal.Images;
import dev.azzybar.pathbound.internal.GameMap;
import dev.azzybar.pathbound.internal.BattleEncounter;
import dev.azzybar.pathbound.internal.Player;
import dev.azzybar.pathbound.internal.Enemy;
import dev.azzybar.pathbound.internal.Chest;
import dev.azzybar.pathbound.internal.Armor;
import dev.azzybar.pathbound.internal.Weapon;

import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
final class Game {
    private final Pane viewport;
    private final GameMap map;
    private final Player player;

    Game() throws IOException {
        setupGame();
        this.map = new GameMap();

        // Creates player and sets up viewport
        Path playerPath = Config.APP_DIR.resolve("sprites", "player", "player.png");
        try (InputStream is = Files.newInputStream(playerPath)) {
            Objects.requireNonNull(is, "Image InputStream cannot be null");
            this.player = new Player(Images.load(is), 1, 14);
        }
        this.viewport = new StackPane(
            this.map.getView(),
            this.player.getSprite().getContainer());
        this.viewport.setMouseTransparent(true);

        // Adds enemies
        for (Enemy enemy : this.map.getEnemies()) {
            this.viewport.getChildren()
                .add(enemy.getSprite().getContainer());
        }

        // Adds chests
        for (Chest chest : this.map.getChests()) {
            this.viewport.getChildren()
                .add(chest.getSprite().getContainer());
        }

        // Adds chest pane
        this.viewport.getChildren().add(Chest.getPopup());
    }

    // Handles player movement
    void handleKeyPress(KeyEvent event, Stage stage) {
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
                if (y < Config.HEIGHT - 1) {
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
                if (x < Config.WIDTH - 1) {
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
    private void setupGame() throws IOException {
        // File path for fonts directory
        Font font;
        String fontPath = "/styles/fonts/EagleLake-Regular.ttf";
        System.out.println("Loading font \u001B[36m" + fontPath + "\u001B[0m");
        try (InputStream is = Game.class.getResourceAsStream(fontPath)) {
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
    }

    // Getters
    Pane getViewport() { return viewport; }
}
