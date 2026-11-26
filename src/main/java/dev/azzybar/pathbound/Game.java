// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;
import dev.azzybar.pathbound.internal.Images;
import dev.azzybar.pathbound.internal.GameMap;
import dev.azzybar.pathbound.internal.BattleEncounter;
import dev.azzybar.pathbound.internal.Player;
import dev.azzybar.pathbound.internal.Enemy;
import dev.azzybar.pathbound.internal.Armor;
import dev.azzybar.pathbound.internal.Weapon;
import dev.azzybar.pathbound.internal.Chest;
import dev.azzybar.pathbound.ui.ChestPopupController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

import javafx.scene.effect.ColorAdjust;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

final class Game {
    private final Pane viewport;
    private final Pane overworldSet;
    private final Group world;
    private final GameMap map;
    private final Player player;
    private final ChestPopupController chestPopup;

    Game() throws IOException {
        // Add items
        Armor.addArmor();
        Weapon.addWeapons();
        this.map = new GameMap();

        // Overarching for non-battle ui and sprites
        this.overworldSet = new StackPane();
        this.overworldSet.managedProperty().bind(this.overworldSet.visibleProperty());

        // Creates player
        Path playerPath = Config.APP_DIR.resolve("sprites", "player", "player" + Config.IMAGE_EXT);
        try (InputStream is = Files.newInputStream(playerPath)) {
            Objects.requireNonNull(is, "Image InputStream cannot be null");
            this.player = new Player(Images.load(is), 1, 14);
        }

        // Wrap player and map in group and add to overworld
        this.world = new Group(this.map.getView(),
                               this.player.getSprite().getContainer());
        this.overworldSet.getChildren().add(this.world);

        // Adds enemies
        for (Enemy enemy : this.map.getEnemies()) {
            this.world.getChildren()
                .add(enemy.getSprite().getContainer());
        }

        // Adds chests
        for (Chest chest : this.map.getChests()) {
            this.world.getChildren()
                .add(chest.getContainer());
        }

        // Creates and adds the chest popup
        FXMLLoader loader = new FXMLLoader(
                            Game.class.getResource(
                            ChestPopupController.FXML_PATH));
        this.overworldSet.getChildren().add(loader.load());
        this.chestPopup = loader.getController();
        this.chestPopup.setPlayer(player);


        // Wraps everything to be a fixed size
        this.viewport = new StackPane(this.overworldSet);
        this.viewport.setMouseTransparent(true);

        // Sets fixed viewport size
        this.viewport.setPrefSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        this.viewport.setMinSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        this.viewport.setMaxSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
    }

    // Handles player movement
    void handleKeyPress(KeyEvent event) {
        // Checks if player is moving or a popup is open
        if (!world.isVisible() || player.isMoving() || chestPopup.isShowing()) {
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
                    startBattle(enemy);
                }
            }
        } else if (mapData[y][x] == 2) {
            // Goes through the chests list
            for (Chest chest : map.getChests()) {
                // Checks if the position (key) matches
                if (chest.getX() == x && chest.getY() == y) {
                    chestPopup.show(chest);
                }
            }
        } else {
            // Updates moving status and moves player
            player.setIsMoving(true);
            player.move(x, y);
        }
    }

    // Create a battle encounter and switch to it
    private void startBattle(Enemy enemy) {
        world.setVisible(false);
        BattleEncounter battle = new BattleEncounter(player,
                                                     enemy,
                                                     () -> endBattle(enemy));
        viewport.getChildren().add(battle.getView());
        battle.start();
    }

    // Automatically switches back to map only if battle won
    private void endBattle(Enemy enemy) {
        viewport.getChildren().retainAll(overworldSet);
        if (enemy.wasKilled()) {
            map.removeEnemy(enemy);
            world.setVisible(true);
            System.out.println("\u001B[36mSuccessfully switched to map\u001B[0m");
        } else {
            gameOver();
        }
    }

    // Switches to death screen
    private void gameOver() {
        ImageView playerView = Images.createScaledView(player.getSprite().getImage(), 5);
        Text deathText = new Text("You have succumbed to death.");
        deathText.setId("death-text");

        // Creates and adds to viewport
        Pane deathScreen = new StackPane(playerView, deathText);
        deathScreen.setId("death-screen");
        viewport.getChildren().add(deathScreen);

        // Slowly grays the player's sprite
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(0);
        playerView.setEffect(grayscale);

        // Creates timeline
        KeyValue grayValue = new KeyValue(grayscale.saturationProperty(), -1.0);
        KeyFrame grayFrame = new KeyFrame(Duration.seconds(4), grayValue);
        Timeline grayTimeline = new Timeline(grayFrame);

        // Makes the text red and opaque
        FadeTransition fadeText = new FadeTransition(Duration.seconds(5), deathText);
        fadeText.setFromValue(0);
        fadeText.setToValue(1);
        fadeText.setInterpolator(Interpolator.LINEAR);

        // Creates a sequence to play
        SequentialTransition sequence = new SequentialTransition(
            new PauseTransition(Duration.seconds(1)),
            grayTimeline,
            new PauseTransition(Duration.seconds(1)),
            fadeText
        );

        sequence.play();
    }

    // Getters
    Pane getViewport() { return viewport; }
}
