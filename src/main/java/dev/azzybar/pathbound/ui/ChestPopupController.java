// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.ui;
import dev.azzybar.pathbound.internal.Player;
import dev.azzybar.pathbound.internal.Chest;
import dev.azzybar.pathbound.internal.Item;
import dev.azzybar.pathbound.internal.Armor;
import dev.azzybar.pathbound.internal.Weapon;

// Connecting to and using chest-popup.fxml
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

public final class ChestPopupController {
    public static final String FXML_PATH = "/fxml/chest-popup.fxml";

    // Gets passed the specific chest and player
    private Player player;
    private Chest chest;
    private Item item;

    // JavaFX nodes declared in the FXML file
    @FXML private VBox popup;
    @FXML private Button take, swap, close;
    private Label[] lines;

    // Configures popup for specific chest
    public void show(Chest chest) {
        this.chest = chest;
        this.item = chest.getItem();

        // Keeps tracking for swapping equiment
        boolean itemConflict = false;

        // Set up popup before displaying
        if (chest.wasOpened()) {
            System.out.println("Chest was already opened");
            lines[0].setText("You already opened this chest");
            lines[0].setVisible(true);
            close.setVisible(true);
        } else {
            // Finds which subclass the item is
            if (item instanceof Armor) {
                // Displays armor's stats
                Armor chestArmor = (Armor) item;
                String[] stats = chestArmor.getStats();
                for (int i = 0; i < stats.length; i++) {
                    lines[i].setText(stats[i]);
                    lines[i].setVisible(true);
                }

                // Checks what the player already has
                Armor[] armor = player.getArmor();
                if (armor[chestArmor.getPiece()] != null) {
                    // Prints to the console
                    String piece = (chestArmor.getPiece() == 0) ? "head"
                        : (chestArmor.getPiece() == 1) ? "torso"
                        : (chestArmor.getPiece() == 2) ? "arm" : "leg";
                    System.out.println("Already wearing " + piece + " armor");

                    lines[stats.length].setText("You are already wearing " + piece + " armor");
                    lines[stats.length].setVisible(true);
                    lines[stats.length + 1].setText("Do you wish to switch?");
                    lines[stats.length + 1].setVisible(true);
                    itemConflict = true;
                }
            } else {
                // Displays weapon's stats
                Weapon chestWeapon = (Weapon) item;
                String[] stats = chestWeapon.getStats();
                for (int i = 0; i < stats.length; i++) {
                    lines[i].setText(stats[i]);
                    lines[i].setVisible(true);
                }

                // Checks what the player already has
                Weapon weapon = player.getWeapon();
                if (weapon != null) {
                    System.out.println("Already have a weapon");

                    lines[stats.length].setText("You already have a " + weapon.getName());
                    lines[stats.length].setVisible(true);
                    lines[stats.length + 1].setText("Do you wish to switch?");
                    lines[stats.length + 1].setVisible(true);
                    itemConflict = true;
                }
            }

            // Adds the appropriate button
            if (itemConflict) {
                swap.setVisible(true);
            } else {
                take.setVisible(true);
            }
        }
        popup.setVisible(true);
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        // Determine which button was pressed
        Button source = (Button) event.getSource();
        if (chest != null && (source == take || source == swap)) {
            chest.open(player);
        }

        // Resets visibility and hides popup
        for (Label line : lines) {
            line.setVisible(false);
        }
        take.setVisible(false);
        swap.setVisible(false);
        close.setVisible(false);
        popup.setVisible(false);
        chest = null;
        item = null;
    }

    @FXML
    public void initialize() {
        // Bind managed property to visibility
        popup.managedProperty().bind(popup.visibleProperty());
        popup.getChildren().stream()
             .forEach(node -> {
                if (node instanceof HBox box) {
                    box.getChildren().stream()
                       .forEach(button -> button.managedProperty().bind(button.visibleProperty()));
                } else {
                    node.managedProperty().bind(node.visibleProperty());
                }
             });

        // Gets all labels and hides them
        lines = popup.getChildren().stream()
                     .filter(Label.class::isInstance)
                     .map(Label.class::cast)
                     .toArray(Label[]::new);

        for (Label line : lines) {
            line.setVisible(false);
        }
    }

    // Prevents player movement when visible
    public boolean isShowing() { return popup.isVisible(); }

    // Sets player to avoid passing as argument
    public void setPlayer(Player player) { this.player = player; }
}
