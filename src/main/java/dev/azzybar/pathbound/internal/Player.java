// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.item.Item;
import dev.azzybar.pathbound.item.Armor;
import dev.azzybar.pathbound.item.Weapon;

import javafx.scene.image.Image;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;
public class Player extends Entity {
    // Player's hp and moving status
    private boolean isMoving;
    private final HpStatus status;
    
    // Index order: Head, Torso, Arms, Legs
    private Armor[] armor;
    private Weapon weapon;
    
    // Constructor
    public Player(Image image, int x, int y) {
        super(image, x, y);

        this.isMoving = false;
        this.status = new HpStatus(this);
        this.armor = new Armor[4];
        this.weapon = null;
    }
    
    // For battle encounters
    public int takeTurn(String action) {
        switch (action) {
            case "fight":
                // Returns weapon's dmg or base dmg
                if (weapon == null) {
                    return 5;
                }
                
                int maxDMG = weapon.dmg();
                int dmg = (int) (Math.random() * maxDMG) + 1;
                return dmg;
            default:
                return -1;
        }
    }
    
    // Takes an item from a chest and assigns its type
    public void take(Item item) {
        // Null check
        if (item == null) {
            System.out.println("No item taken from chest");
            return;
        }
        
        // Casts item to its subclass
        if (item instanceof Armor) {
            Armor armor = (Armor) item;
            this.armor[armor.slot().ordinal()] = armor;
        } else {
            Weapon weapon = (Weapon) item;
            this.weapon = weapon;
        }
        
        // Updates stats regardless of the item type
        updateDEF();
        updateSPD();
        updateEVA();
    }
    
    // Moves player's sprite
    public void move(int x, int y) {
        // Updates new visual position for player
        sprite.updatePosition(x, y);
        
        // Creates a linear transition to move player
        TranslateTransition translate = new TranslateTransition(Duration.millis(100), sprite.getContainer());
        translate.setToX(sprite.getPixelX());
        translate.setToY(sprite.getPixelY());
        translate.setInterpolator(Interpolator.LINEAR);
        
        // Updates grid position and sets moving to false
        translate.setOnFinished(event -> {
            sprite.setX(x);
            sprite.setY(y);
            isMoving = false;
        });
        
        translate.play();
    }
    
    // Defense Formula
    // 1 - [(1 - DR1) * (1 - DR2) * (1 - DR3) * (1 - DR4)]
    private void updateDEF() {
        // Tracks counts for specific sets
        int padded = 0;
        int scale = 0;
        int chainmail = 0;
        int splinted = 0;
        int plate = 0;
        
        // Counts how many pieces of armor player is wearing
        int pieces = 0;
        
        // Creates a new defense stat
        double newDEF = 1;
        for (Armor piece : armor) {
            if (piece != null) {
                // Multiples the damage modifiers passing through
                newDEF *= (1 - piece.def());
                pieces++;
                
                // Tracks type of armor
                switch (piece.name()) {
                    case "padded":
                        padded++;
                        break;
                    case "scale":
                        scale++;
                        break;
                    case "chainmail":
                        chainmail++;
                        break;
                    case "splinted":
                        splinted++;
                        break;
                    default:
                        plate++;
                        break;
                }
            }
        }
        
        // Checks whether full set or not
        if (pieces == 4) {
            // Gives a 10% defense bonus for a full set
            newDEF *= (1 - 0.1);
            System.out.println("+10% defense from wearing full armor");
            
            // Checks if user has a specific set
            if (padded == 4) {
                // 15% boost
                newDEF *= (1 - 0.15);
                System.out.println("+15% defense from wearing padded set");
            } else if (scale == 4) {
                // 20% boost
                newDEF *= (1 - 0.2); 
                System.out.println("+20% defense from wearing scale set");
            } else if (chainmail == 4) {
                // 25% boost
                newDEF *= (1 - 0.25); 
                System.out.println("+25% defense from wearing chainmail set");
            } else if (splinted == 4) {
                // 30% boost
                newDEF *= (1 - 0.3);
                System.out.println("+30% defense from wearing splinted set");
            } else if (plate == 4) {
                // 35% boost
                newDEF *= (1 - 0.35);
                System.out.println("+35% defense from wearing plate set");
            }
        }
        
        // Subtracts total damage to get total reduction
        newDEF = 1 - newDEF;
        setDEF(newDEF);
    }
    
    // Speed Formula
    // 1 * (1 - SP1) * (1 - SP2) * (1 - SP3) * (1 - SP4)
    private void updateSPD() {
        double newSPD = 1;
        for (Armor piece : armor) {
            if (piece != null) {
                // Multiples to get remaining speed
                newSPD *= (1 - piece.spd());
            }
        }
        
        // Checks if player has a weapon
        if (weapon != null) {
            newSPD *= (1 - weapon.spd());
        }
        
        // Sets the new speed
        setSPD(newSPD);
    }
    
    // Evasion Formula
    // 0.1 * (1 - EP1) * (1 - EP2) * (1 - EP3) * (1 - EP4)
    private void updateEVA() {
        double newEVA = 0.1;
        for (Armor piece : armor) {
            if (piece != null) {
                newEVA *= (1 - piece.eva());
            }
        }
        
        // Updates new evasion stat
        setEVA(newEVA);
    }
    
    // Getters
    public boolean isMoving() { return isMoving; }
    public HpStatus status() { return status; }
    public Armor[] getArmor() { return armor; }
    public Weapon getWeapon() { return weapon; }
    
    // Setters
    public void setIsMoving(boolean isMoving) { this.isMoving = isMoving; }
}
