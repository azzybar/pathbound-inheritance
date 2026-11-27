// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Config;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import java.util.function.BiConsumer;
/**
 * This is mean to be an associaton class, a regular class
 * that holds information regarding the relationship between
 * two or more classes. Here it is meant to hold information
 * specific to the player and current enemy. It also acts as
 * the battle manager for handling turn order.
 */
public class BattleEncounter {
    // Functions to call on end and update
    private final Runnable onBattleEnd;
    private BiConsumer<String, Boolean> onUIUpdate;

    // Combatants for current encounter
    private final Player player;
    private final Enemy enemy;
    
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

        this.player = player;
        this.enemy = enemy;
    }
    
    public void start() {
        // Starts battle
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(event -> {
            if (turnOrder == 0) {
                onUIUpdate.accept("Your turn.", false);
                handlePlayerTurn();
            } else {
                onUIUpdate.accept("Enemy's turn.", true);
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
    }
    
    // Handles battle action
    public void handleAction(String action) {
        // Changes turn order
        turnOrder = 1;
        
        if (action.equals("fight")) {
            // Player takes turn
            int dmg = player.takeTurn("fight");
            enemy.takeDmg(enemy.getName(), dmg);
            
            // Checks entities' health again
            if (player.getHp() <= 0 || enemy.getHp() <= 0) {
                end();
                return;
            }
        }

        onUIUpdate.accept("", true);
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            // Switches control to enemy
            System.out.println("\u001B[36mSwitching to enemy\u001B[0m");
            onUIUpdate.accept("Enemy's turn.", true);
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
            onUIUpdate.accept("Your turn.", false);
            handlePlayerTurn();
        });
        pause.play();
    }
    
    // Ends battle (regardless of who lost)
    private void end() {
        // Checks who died
        if (enemy.getHp() == 0) {
            System.out.println("Player has defeated " + enemy.getName());
            enemy.kill();
            
            // Resets player's and removes enemy from map
            player.resetHp();
            
            // Switches back to map
            System.out.println("Switching back to map");
        }
        onBattleEnd.run();
    }

    public void setOnUIUpdate(BiConsumer<String, Boolean> onUIUpdate) {
        this.onUIUpdate = onUIUpdate;
    }

    // Getters
    // public StackPane getView() { return view; }
    public Player getPlayer() { return player; }
    public Enemy getEnemy() { return enemy; }
}
