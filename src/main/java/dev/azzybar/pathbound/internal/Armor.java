// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Config;

/*
Armor Guide:
    Piece:
    - Head = 10% DEF increasee
    - Torso = 15% DEF increase
    - Arms = 10% SPD decrease
    - Legs = 15% EVA decrease
    Weight:
    - Light = 10% initiative delay
    - Medium = 15% initiative delay
    - Heavy = 20% initiative delay

ARMOR
"padded" - 11 AC, 8lb, light
"scale" - 14 AC, 45lb, medium
"chainmail" - 16 AC, 55lb, heavy
"splinted" - 17 AC, 60lb, heavy
"plate" - 18 AC, 65lb, heavy

FORMULA
- DEF: AC
- SPD: Weight
- EVA: sqrt(Weight) / 100
*/
import javafx.scene.control.Label;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
public class Armor extends Item {
    /*
    0, (AC * 1.1), SPD, (Math.sqrt(EVA) / 100)),
    1, (AC * 1.15), SPD, (Math.sqrt(EVA) / 100)),
    2, AC, (SPD * 1.1), (Math.sqrt(EVA) / 100)),
    3, AC, SPD, (Math.sqrt(EVA) / 100) * 1.15)
    */
    
    // Possible armor sets
    private static Armor[] padded = new Armor[4];
    private static Armor[] scale = new Armor[4];
    private static Armor[] chainmail = new Armor[4];
    private static Armor[] splinted = new Armor[4];
    private static Armor[] plate = new Armor[4];
    
    // Indicates what part of the body (array index)
    private final int piece;
    
    /**
     * Stats regarding are immutable for simplicity
     * Represented as percentages
     * 
     * DEF is positive, higher value = higher damage reduction
     * SPD is negative, higher value = lower initiative score
     * EVA is negative, higher value = lower dodge chance
     */
    private final double DEF;
    private final double SPD;
    private final double EVA;
    
    // Constructor
    public Armor(String name, int piece, double DEF, double SPD, double EVA) {
        super(name);
        this.piece = piece;
        this.DEF = DEF;
        this.SPD = SPD;
        this.EVA = EVA;
    }
    
    // Copy Constructor
    public Armor(Armor that) {
        super(that.getName());
        this.piece = that.piece;
        this.DEF = that.DEF;
        this.SPD = that.SPD;
        this.EVA = that.EVA;
    }
    
    // Randomizes a armor for a chest
    public static Armor randomArmor() {
        // Picks between the five sets
        int type = (int) (Math.random() * 5);
        
        // Determines what piece of armor to return
        int armor = 0;
        if (type < 3) {
            armor = (int) (Math.random() * 4);
            return (new Armor(padded[armor]));
        } else if (type < 5) {
            armor = (int) (Math.random() * 4);
            return (new Armor(scale[armor]));   
        } else if (type < 7) {
            armor = (int) (Math.random() * 4);
            return (new Armor(chainmail[armor]));
        } else if (type < 9) {
            armor = (int) (Math.random() * 4);
            return (new Armor(splinted[armor]));
        } else {
            armor = (int) (Math.random() * 4);
            return (new Armor(plate[armor]));
        }
    }
    
    // For printing stats in the chest popup
    @Override
    public Label[] getStats() {
        Label[] stats = new Label[4];
        
        // Creates labels to be presented in chest popup
        stats[0] = new Label((piece <= 1 ? 
            "You found a " : "You found ") + this);
        stats[1] = new Label(
            String.format("DEF: +%.1f%%", (DEF * 100)));
        stats[2] = new Label(
            String.format("SPD: -%.1f%%", (SPD * 100)));
        stats[3] = new Label(
            String.format("EVA: -%.1f%%", (EVA * 100)));
            
        return stats;
    }
    
    // For printing to the console
    @Override
    public String toString() {
        String descriptor = getName() + " ";
        
        // Checks what armor piece it is
        switch (piece) {
            case 0:  // Head
                descriptor += ((getName().equals("padded") 
                    || getName().equals("scale")) ? "helmet"
                    : getName().equals("chainmail") ? "coif" : "sallet");
                break;
            case 1:  // Torso
                descriptor += (getName().equals("padded") ? "gambeson"
                    : getName().equals("scale") ? "brigandine"
                    : getName().equals("chainmail") ? "hauberk" : "cuirass");
                break;
            case 2:  // Arms
                descriptor += (getName().equals("padded") ? "bracers"
                    : getName().equals("scale") ? "pauldrons"
                    : (getName().equals("chainmail") 
                    || getName().equals("plate")) ? "gauntlets" : "vambraces");
                break;
            default:  // Legs
                descriptor += (getName().equals("padded") ? "trousers"
                    : getName().equals("scale") ? "sabatons"
                    : getName().equals("chainmail") ? "chausses" : "greave");
                break;
        }
        
        return descriptor;
    }
    
    // Getters
    public int getPiece() { return piece; }
    public double getDEF() { return DEF; }
    public double getSPD() { return SPD; }
    public double getEVA() { return EVA; }
    
    // Adds armor to lists
    public static void addArmor() throws IOException {
        // Gets path to directory
        Path armorDir = Config.APP_DIR.resolve("items", "armor.txt");
        System.out.println("Adding armor from: \u001B[36m" + armorDir + "\u001B[0m");
        
        // Goes through Weapons folder
        try (Stream<String> stream = Files.lines(armorDir)) {
            stream.forEach(Armor::configArmor);
        }
        System.out.println("\u001B[92mSuccessfully added armor\u001B[0m");
    }
    
    // Adds the armor from the file
    private static void configArmor(String line) {
        // Sorts data
        String[] data = line.split("-");
        String name = data[0];
        double def = Double.parseDouble(data[1]);
        double spd = Double.parseDouble(data[2]);
        int eva = Integer.parseInt(data[3]);
        
        // Creates armor set
        Armor headArmor = new Armor(name, 0, 
            (def * 1.1), 
            spd, 
            (Math.sqrt(eva) / 100));
        
        Armor torsoArmor = new Armor(name, 1, 
            (def * 1.15), 
            spd, 
            (Math.sqrt(eva) / 100));
            
        Armor armArmor = new Armor(name, 2, 
            def, 
            (spd * 1.1), 
            (Math.sqrt(eva) / 100));
        
        Armor legArmor = new Armor(name, 3, 
            def, 
            spd, 
            ((Math.sqrt(eva) / 100) * 1.15));
        
        // Adds to appropriate list
        Armor[] armor = null;
        switch (name) {
            case "padded":
                armor = padded;
                break;
            case "scale":
                armor = scale;
                break;
            case "chainmail":
                armor = chainmail;
                break;
            case "splinted":
                armor = splinted;
                break;
            default:
                armor = plate;
                break;
        }
        
        armor[0] = headArmor;
        armor[1] = torsoArmor;
        armor[2] = armArmor;
        armor[3] = legArmor;
    }
}
