// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

// This should technically be an abstract class as it's
// meant to be a blueprint for Player and Enemy and both
// need to override the takeTurn() method.
import javafx.scene.image.Image;
public abstract class Entity {
    // Sprite
    protected Sprite sprite;
    
    // Health
    private int maxHp;
    private int hp;
    
    // Each armor piece's own values accumulates to the player's
    // Represented as percentages
    private double DEF;
    private double SPD;
    private double EVA;
    
    // Constructor
    protected Entity(int tileSize, Image img, int x, int y) {
        this.sprite = new Sprite(tileSize, img, x, y);
        
        // Health
        this.maxHp = 20;
        this.hp = maxHp;
        
        // Base defense is 0 (no reduction)
        // Base speed is 1 (attacks first)
        // Base evasion is 0.15 (15% chance)
        this.DEF = 0;
        this.SPD = 1;
        this.EVA = 0.15;
    }
    
    // For battle encounters
    public void takeDmg(String name, int dmg) {
        if (dmg > 0) {
            // Determines when entity dodges
            double chance = Math.random();
            if (EVA > chance) {
                System.out.println(name + " dodged attack.");
            } else {
                int totalDmg = (int) (dmg * (1.0 - DEF));
                
                // Sets a minimum of 1 dmg
                if (totalDmg == 0) {
                    totalDmg = 1;
                }
                hp -= totalDmg;
                
                // Checks to make sure health isn't negative
                if (hp < 0) {
                    hp = 0;
                }
                
                System.out.println(name + " received " + dmg + " damage");
                if (DEF != 0) {
                    System.out.println("Reduced damage to " + totalDmg);
                }
                System.out.println(name + " new health: " + hp + " (-" + totalDmg + ")");
            }
        }
    }
    
    // Getters
    public Sprite getSprite() { return sprite; }
    public int getMaxHp() { return maxHp; }
    public int getHp() { return hp; }
    protected double getDEF() { return DEF; }
    protected double getSPD() { return SPD; }
    protected double getEVA() { return EVA; }
    
    // Setters
    public void resetHp() { this.hp = maxHp; }
    protected void setDEF(double DEF) { this.DEF = DEF; }
    protected void setSPD(double SPD) { this.SPD = SPD; }
    protected void setEVA(double EVA) { this.EVA = EVA; }
}
