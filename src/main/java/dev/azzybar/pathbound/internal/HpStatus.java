// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
public class HpStatus {
    // Hp display for player during battles
    private final Player player;
    private final HBox health;
    private final Label display;
    private final Rectangle bar;
    private int oldHp;
    
    // Constructor
    public HpStatus(Player player) {
        // Sets informaiton
        this.player = player;
        this.display = new Label("HP (" + player.getHp() + "/" + player.getMaxHp() + ")");
        this.bar = new Rectangle(player.getMaxHp() * 5, 22, Color.WHITE);
        this.oldHp = player.getMaxHp();
        
        // Sets height the same
        display.getStyleClass().add("hp-display");
        bar.heightProperty().bind(display.heightProperty());
        
        // Creates health box
        this.health = new HBox(15);
        health.getChildren().addAll(display, bar);
    }
    
    // Updates health information
    public void update() {
        // Calculates the damage
        int dmg = oldHp - player.getHp();
        
        display.setText("HP (" + player.getHp() + "/" + player.getMaxHp() + ")");
        bar.setWidth(bar.getWidth() - (dmg * 5));
        
        // Sets oldHp to current
        oldHp = player.getHp();
    }
    
    // Getters
    public HBox get() { return health; }
}
