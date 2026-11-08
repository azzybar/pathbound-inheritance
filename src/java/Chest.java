import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import java.util.List;
import java.util.ArrayList;
public class Chest {
    // Possible button options
    private static Button take = new Button("Take");
    private static Button swap = new Button("Swap");
    private static Button close = new Button("Close");
    
    // Specific for chest popups
    private static Pane chestPane = new Pane();
    private static VBox itemInfo = new VBox();
    private static HBox chestButtons = new HBox(10);
    private static boolean popupOpen = false;
    
    // Sets up popup pane
    static {
        // Hides the popup chest
        itemInfo = new VBox();
        itemInfo.setVisible(false);
        itemInfo.setId("item-info");
        chestPane = new Pane(itemInfo);   
    }
    
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
    public Chest(int tileSize, Image img, int x, int y) {
        this.sprite = new Sprite(tileSize, img, x, y);
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
    
    // Adjusts margins and positions
    public static void setMargins() {
        itemInfo.relocate(15,15);
        itemInfo.setMargin(chestButtons, new Insets(10, 0, 0, 0));
    }
    
    // When interacting with chests
    public void show(Player player) {
        popupOpen = true;
        
        // Handles button input
        take.setOnAction(event -> handlePopupButton(event, player));
        swap.setOnAction(event -> handlePopupButton(event, player));
        close.setOnAction(event -> handlePopupButton(event, player));
        
        // Chests if player already opened chest
        if (wasOpened()) {
            System.out.println("Chest was already opened");
            itemInfo.getChildren().addAll(
                new Label("You already opened this chest"),
                close);
        } else {
            // Finds which subclass the item is
            if (item instanceof Armor) {
                // Displays armor's stats
                Armor chestArmor = (Armor) item;
                Label[] stats = chestArmor.getStats();
                for (Label stat : stats) {
                    itemInfo.getChildren().add(stat);
                }
                    
                // Checks what the player already has
                Armor[] armor = player.getArmor();
                if (armor[chestArmor.getPiece()] != null) {
                    // Prints to the console
                    String piece = (chestArmor.getPiece() == 0) ? "head"
                        : (chestArmor.getPiece() == 1) ? "torso"
                        : (chestArmor.getPiece() == 2) ? "arm" : "leg";
                    System.out.println("Already wearing " + piece + " armor");
                    
                    itemInfo.getChildren().addAll(
                        new Label("You are already wearing " + piece + " armor"),
                        new Label("Do you wish to switch?"));
                        
                    // Adding buttons to interact
                    chestButtons.getChildren().add(swap);
                } else {
                    // Adding buttons to interact
                    chestButtons.getChildren().add(take);
                }
            } else {
                // Displays weapon's stats
                Weapon chestWeapon = (Weapon) item;
                Label[] stats = chestWeapon.getStats();
                for (Label stat : stats) {
                    itemInfo.getChildren().add(stat);
                }
                
                // Checks what the player already has
                Weapon weapon = player.getWeapon();
                if (weapon != null) {
                    System.out.println("Already have a weapon");
                    itemInfo.getChildren().addAll(
                        new Label("You already have a " + weapon.getName()),
                        new Label("Do you wish to switch?"));
                    
                    // Adding buttons to interact
                    chestButtons.getChildren().add(swap);
                } else {
                    // Adding buttons to interact
                    chestButtons.getChildren().add(take);
                }
            }
        }
        
        // Makes the popup visible
        chestButtons.getChildren().add(close);
        itemInfo.setVisible(true);
        itemInfo.getChildren().add(chestButtons);
    }
    
    // Returns the item to player
    private void open(Player player) {
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
        sprite.get().setEffect(grayscale);
        wasOpened = true;
    }
    
    // Handles input based on the button
    private void handlePopupButton(ActionEvent event, Player player) {
        Button source = (Button) event.getSource();
        
        // "Take" and "Swap" perform the same function
        if (source == take || source == swap) {
            open(player);
        }
        
        // Close button is called regardless
        popupOpen = false;
        itemInfo.setVisible(false);
        itemInfo.getChildren().clear();
        chestButtons.getChildren().clear();
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
    public boolean wasOpened() { return wasOpened; }
    public static Pane getPopup() { return chestPane; }
    public static boolean popupOpen() { return popupOpen; }
    
    // Updates chest count (done before chests are created)
    public static void updateCount() {
        // Updates count and splits it in half
        numOfChests++;
        
        // Sets item limits
        itemLimit[0] = numOfChests / 2;
        itemLimit[1] = numOfChests - itemLimit[0];
    }
}