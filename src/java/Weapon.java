/*
Weight Guide:
- Light = 1-3 lbs (10% initiative delay)
- Heavy = 4+ lbs (25% initiative delay)

BLUDGEONS
"battleaxe" - 1d8, 4lb
"club" - 1d4, 2lb
"flail" - 1d8, 2lb
"mace" - 1d6, 4lb
"quarterstaff" - 1d6, 4lb
"warhammer" - 1d8, 5lb

BLADED
"dagger" - 1d4, 1lb
"longsword" - 1d8, 3lb
"rapier" - 1d8, 2lb
"shortsword" - 1d6, 2lb
"scimitar" - 1d6, 3lb

POLEARMS
"spear" - 1d6, 3lb
"glaive" - 1d10, 6lb
"pike" - 1d10, 18lb
"halberd" - 1d10, 6lb
"lance" - 1d10, 6lb
*/
import javafx.scene.control.Label;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
public class Weapon extends Item {
    // Weapon categories
    private static ArrayList<Weapon> bludgeons = new ArrayList<Weapon>();
    private static ArrayList<Weapon> bladed = new ArrayList<Weapon>();
    private static ArrayList<Weapon> polearms = new ArrayList<Weapon>();
    
    // Damage is calculated as a random number from 1 to DMG
    // Weapons affect an entity's initiative (via weight)
    private final int DMG;
    private final double SPD;
    
    // Constructor
    public Weapon(String name, int DMG, double SPD) {
        super(name);
        this.DMG = DMG;
        this.SPD = SPD;
    }
    
    // Copy Constructor
    public Weapon(Weapon that) {
        super(that.getName());
        this.DMG = that.DMG;
        this.SPD = that.SPD;
    }
    
    // Randomizes a weapon for a chest
    public static Weapon randomWeapon() {
        // Picks between the three types
        int type = (int) (Math.random() * 3);
        
        // Determines what weapon to return
        int i = 0;
        if (type == 0) {
            i = (int) (Math.random() * bludgeons.size());
            return (new Weapon(bludgeons.get(i)));
        } else if (type == 1) {
            i = (int) (Math.random() * bladed.size());
            return (new Weapon(bladed.get(i)));
        } else {
            i = (int) (Math.random() * polearms.size());
            return (new Weapon(polearms.get(i)));
        }
    }
    
    // For printing stats in the chest popup
    @Override
    public Label[] getStats() {
        Label[] stats = new Label[3];
        
        // Creates labels to be presented in chest popup
        stats[0] = new Label("You found a " + getName());
        stats[1] = new Label(
            String.format("DMG: 1d%d", DMG));
        stats[2] = new Label(
            String.format("SPD: -%d%%", ((int) (SPD * 100))));
            
        return stats;
    }
    
    // For printing to the console
    @Override
    public String toString() {
        return String.format("%s (1d%d), -%d%% speed", getName(), DMG, ((int) (SPD * 100)));
    }
    
    // Getters
    public int getDMG() { return DMG; }
    public double getSPD() { return SPD; }
    
    // Adds weapons to lists
    public static void addWeapons() {
        // Gets path to directory
        Path weaponsDir = Paths.get("Items/Weapons");
        System.out.println("Adding weapons from: \u001B[36m" + weaponsDir.toString() + "\u001B[0m");
        
        // Goes through Weapons folder
        try (Stream<Path> stream = Files.walk(weaponsDir)) {
            List<Path> files = stream
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith("txt"))
                .collect(Collectors.toList());
            files.forEach(Weapon::configWeapon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\u001B[92mSuccessfully added weapons\u001B[0m");
    }
    
    // Adds the weapon from the file
    private static void configWeapon(Path path) {
        // Goes through each file in the directory
        try (BufferedReader br = Files.newBufferedReader(path)) {
            // Sorts data
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("-");
                String name = data[0];
                int dmg = Integer.parseInt(data[1]);
                int weight = Integer.parseInt(data[2]);
                
                // Creates and sorts weapon into list
                double spd = (weight < 4) ? 0.1 : 0.25;
                Weapon weapon = new Weapon(name, dmg, spd);
                
                // Adds to appropriate list
                if (path.toString().contains("bludgeons")) {
                    bludgeons.add(weapon);
                } else if (path.toString().contains("bladed")) {
                    bladed.add(weapon);
                } else {
                    polearms.add(weapon);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}