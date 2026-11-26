// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import java.util.List;
import java.util.ArrayList;
public class Chest {
    // Keeps tracks of what chests have what
    private static int numOfChests = 0;
    private static int[] itemLimit = new int[2];
    private static int[] itemCount = new int[2];
    
    // Specific to armor pieces
    private static int[] armorPieces = new int[4];
    
    // Holds list of armor and weapons
    private static List<Armor> armorList = new ArrayList<Armor>();
    private static List<Weapon> weaponsList = new ArrayList<Weapon>();
    
    // Sprite
    private Sprite sprite;
    
    // Information regarding contents
    private Item item;
    private boolean wasOpened;
    
    // Constructor
    public Chest(Image img, int x, int y) {
        this.sprite = new Sprite(img, x, y);
        this.wasOpened = false;
        
        // Decides which loot to hold
        int loot = (int) (Math.random() * 2);
        while (itemCount[0] == itemLimit[0] || itemCount[1] == itemLimit[1]) {
            // Regenerates if item limit reached
            if ((loot == 0 && itemCount[0] == itemLimit[0]) ||
                (loot == 1 && itemCount[1] == itemLimit[1])) {
                loot = (int) (Math.random() * 2);
            } else {
                break;
            }
        }
        
        // Randomizes item
        randomItem(loot);
    }
    
    // Returns the item to player
    public void open(Player player) {
        // Decides item's subclass
        if (item instanceof Armor) {
            // Checks if the specific piece is null
            Armor chestArmor = (Armor) item;
            Armor[] armor = player.getArmor();
            if (armor[chestArmor.getPiece()] == null) {
                System.out.println("Found " + chestArmor);
            } else {
                System.out.println("Replaced " + armor[chestArmor.getPiece()] + " with " + chestArmor);
            }
            
            player.take(chestArmor);
        } else {
            // Checks if player has a sword or not
            Weapon chestWeapon = (Weapon) item;
            if (player.getWeapon() == null) {
                System.out.println("Found " + chestWeapon);
            } else {
                System.out.println("Replaced " + player.getWeapon().getName() + " with " + chestWeapon.getName());
            }
            
            player.take(chestWeapon);
        }
        
        close();
    }
    
    // Signals chest has been looted
    private void close() {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        sprite.getView().setEffect(grayscale);
        wasOpened = true;
    }
    
    // Randomizes armor (simple recursive function)
    private void randomItem(int loot) {
        if (loot == 0) {
            item = Armor.randomArmor();
            Armor armor = (Armor) item;
            
            // Checks if armor aready exists
            for (Armor a : armorList) {
                if (item.equals(a)) {
                    randomItem(loot);
                    return;
                }
            }
            
            // Ensures that a fulls is possible or that
            // there are never armor of the same type when
            // less than 4 chests for armor are available
            for (int i = 0; i < armorPieces.length; i++) {
                // Checks if at least one piece is needed
                if (armorPieces[i] == 0) {
                    // Checks if the current item is that piece
                    if (i == armor.getPiece()) {
                        break;
                    } else {
                        // Randomizes until it gets one
                        randomItem(loot);
                        return;
                    }
                }
            }
            
            // Adds to list
            armorList.add(armor);
            armorPieces[armor.getPiece()]++;
            itemCount[0]++;
        } else {
            item = Weapon.randomWeapon();
            Weapon weapon = (Weapon) item;
            
            // Checks if weapon aready exists
            for (Weapon w : weaponsList) {
                if (item.equals(w)) {
                    randomItem(loot);
                    return;
                }
            }
            
            // Adds to list
            weaponsList.add(weapon);
            itemCount[1]++;
        }
    }
    
    // Getters
    public Sprite getSprite() { return sprite; }
    public Item getItem() { return item; }
    public boolean wasOpened() { return wasOpened; }
    
    // Updates chest count (done before chests are created)
    public static void updateCount() {
        // Updates count and splits it in half
        numOfChests++;
        
        // Sets item limits
        itemLimit[0] = numOfChests / 2;
        itemLimit[1] = numOfChests - itemLimit[0];
    }
}
