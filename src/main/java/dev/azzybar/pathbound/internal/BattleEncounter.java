// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Config;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.beans.binding.Bindings;
/**
 * This is mean to be an associaton class, a regular class
 * that holds information regarding the relationship between
 * two or more classes. Here it is meant to hold information
 * specific to the player and current enemy. It also acts as
 * the battle manager for handling turn order.
 */
public class BattleEncounter {
    private final StackPane view;
    private final Runnable onBattleEnd;
    
    private Label battleText;
    private Button fight;
    private Button guard;
    private Button item;
    private Button pray;
    
    private final Player player;
    
    private final Enemy enemy;
    private ImageView enemyView;
    
    // 0 = Player's turn, 1 = Enemy's turn
    private int turnOrder;
    
    // Constructor
    public BattleEncounter(Player player, Enemy enemy, Runnable onBattleEnd) {
        this.onBattleEnd = onBattleEnd;

        // For testing and developing purposes
        System.out.println("Starting battle encounter with " + enemy.getName() + "...");
        enemy.printStats();
        
        // Determines who goes first
        this.turnOrder = (player.getSPD() >= enemy.getSPD()) ? 0 : 1;
        
        // Action buttons
        this.fight = new Button("FIGHT");
        this.guard = new Button("GUARD");
        this.item = new Button("ITEM");
        this.pray = new Button("PRAY");
        
        // Disables buttons
        fight.setDisable(true);
        guard.setDisable(true);
        item.setDisable(true);
        pray.setDisable(true);

        // Sets up hp status and message
        this.player = player;
        this.battleText = new Label("");
        
        // Creates a deep copy of enemy sprite
        this.enemy = enemy;
        this.enemyView = Images.createScaledView(enemy.getSprite().getImage(), 5);
        
        // Creates battle scene
        this.view = new StackPane();
        this.view.setPrefSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        this.view.setMinSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        this.view.setMaxSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        switchScene();
    }
    
    public void start() {
        // Starts battle
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(event -> {
            updateGUI();
            if (turnOrder == 0) {
                handlePlayerTurn();
            } else {
                handleEnemyTurn();
            } 
        });
        pause.play();
    }
    
    private void handlePlayerTurn() {
        // Checks entities' health
        if (player.getHp() <= 0 || enemy.getHp() <= 0) {
            end();
            return;
        }
        
        // Sets onAction events
        fight.setOnAction(this::handleAction);
    }
    
    // Handles battle action
    private void handleAction(ActionEvent event) {
        // Gets source of the event
        Button source = (Button) event.getSource();
        
        // Changes turn order
        turnOrder = 1;
        
        if (source == fight) {
            // Player takes turn
            int dmg = player.takeTurn("fight");
            enemy.takeDmg(enemy.getName(), dmg);
            
            // Checks entities' health again
            if (player.getHp() <= 0 || enemy.getHp() <= 0) {
                end();
                return;
            }
        }
        
        // Disables buttons
        fight.setDisable(true);
        guard.setDisable(true);
        item.setDisable(true);
        pray.setDisable(true);
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            // Switches control to enemy
            System.out.println("\u001B[36mSwitching to enemy\u001B[0m");
            updateGUI();
            handleEnemyTurn();
        });
        pause.play();
    }
    
    private void handleEnemyTurn() {
        // Checks entities' health
        if (player.getHp() <= 0 || enemy.getHp() <= 0) {
            end();
            return;
        }
        
        // Enemy takes turn
        int dmg = enemy.takeTurn();
        player.takeDmg("Player", dmg);
        
        // Checks entities' health again
        if (player.getHp() <= 0 || enemy.getHp() <= 0) {
            end();
            return;
        }
        
        // Adds a pause
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            // Switches control to player
            System.out.println("\u001B[36mSwitching to player\u001B[0m");
            turnOrder = 0;
            updateGUI();
            handlePlayerTurn();
        });
        pause.play();
    }
    
    // Simply for updating battle text and health
    private void updateGUI() {
        if (turnOrder == 0) {
            // Re-enables buttons
            fight.setDisable(false);
            guard.setDisable(false);
            item.setDisable(false);
            pray.setDisable(false);
            
            // battleText.setText("Your turn.");
            battleText.setText("Your turn.");
            player.status().update();
        } else {
            battleText.setText("Enemy's turn.");
        }
    }
    
    // Ends battle (regardless of who lost)
    private void end() {
        // Checks who died
        if (enemy.getHp() == 0) {
            System.out.println("Player has defeated " + enemy.getName());
            enemy.kill();
            
            // Resets player's and removes enemy from map
            player.resetHp();
            player.status().update();
            
            // Switches back to map
            System.out.println("Switching back to map");
        }
        onBattleEnd.run();
    }

    // Creates battle scene and switches to it
    private void switchScene() {
        // Creates battle layout
        System.out.println("Switching to battle scene");
        BorderPane root = new BorderPane();
        view.getChildren().add(root);
        root.setMouseTransparent(true);

        StackPane center = new StackPane(enemyView, battleText);
        center.setAlignment(enemyView, Pos.CENTER);
        center.setAlignment(battleText, Pos.BOTTOM_CENTER);
        root.setCenter(center);

        battleText.setId("battle-text");
        center.setId("center");
        root.setId("battle-pane");

        // Creates battle options
        HBox actionMenu = new HBox(20);
        actionMenu.getChildren().addAll(fight, guard, item, pray);
        actionMenu.setAlignment(Pos.CENTER);

        // Creates Battle GUI
        VBox battleGUI = new VBox(15);
        battleGUI.getChildren().addAll(player.status().get(), actionMenu);
        root.setBottom(battleGUI);

        // Adjusts margins
        center.setMargin(enemyView, new Insets(0, 0, (Config.TILE_SIZE * 3), 0));
        battleGUI.setMargin(player.status().get(), new Insets(Config.TILE_SIZE / 2.0, 0, 0, 50));

        // Offset enemy view to keep pixels sharp
        enemyView.translateXProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.ceil(enemyView.getLayoutX()) - enemyView.getLayoutX(),
                enemyView.layoutXProperty()
            )
        );
        enemyView.translateYProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.ceil(enemyView.getLayoutY()) - enemyView.getLayoutY(),
                enemyView.layoutYProperty()
            )
        );

        // Goes through the action buttons
        for (Node node : actionMenu.getChildren()) {
            if (node instanceof Button) {
                // Adds their css and margins
                Button button = (Button) node;
                button.getStyleClass().add("action-button");
                actionMenu.setMargin(button, new Insets(0, 0, Config.TILE_SIZE, 0));
            }
        }
        System.out.println("\u001B[92mSuccessfully switched to battle scene\u001B[0m");
    }

    // Getters
    public StackPane getView() { return view; }
}
