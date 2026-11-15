package dev.azzybar.pathbound.internal;

import javafx.scene.image.Image;
import javafx.scene.effect.ColorAdjust;
public class Enemy extends Entity {
    // Holds name (type), weapon, and defeat status
    private final String name;
    private Weapon weapon;
    private boolean wasKilled;
    
    // Constructor
    public Enemy(int tileSize, Image img, int x, int y, String name) {
        super(tileSize, img, x, y);
        this.name = name;
        
        // Randomizes stats (with upper and lower limits)
        double DEF = Math.max(Math.random() * 0.55, 0.1);
        double SPD = Math.max(Math.random() * 0.85, 0.2);
        double EVA = Math.max(Math.random() * 0.35, 0.1);
        
        // Overrides default stats
        setDEF(DEF);
        setSPD(SPD);
        setEVA(EVA);
        
        this.weapon = Weapon.randomWeapon();
        this.wasKilled = false;
    }
    
    // For battle encounters
    public int takeTurn() {
        // Damage is a number from 1 to DMG
        int maxDMG = weapon.getDMG();
        int dmg = (int) (Math.random() * maxDMG) + 1;
        return dmg;
    }
    
    // Grays image and sets wasKilled to true
    public void kill() {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        getSprite().get().setEffect(grayscale);
        wasKilled = true;
    }
    
    // To the console during battles
    public void printStats() {
        System.out.printf("Weapon: %s (1d%d)\n", weapon.getName(), weapon.getDMG());
        System.out.printf("DEF: %.1f%%\n", (getDEF() * 100));
        System.out.printf("SPD: %.1f%%\n", (getSPD() * 100));
        System.out.printf("EVA: %.1f%%\n", (getEVA() * 100));
    }
    
    // Getters
    public String getName() { return name; }
    public boolean wasKilled() { return wasKilled; }
}
