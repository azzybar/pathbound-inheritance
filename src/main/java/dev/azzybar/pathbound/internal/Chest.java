// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Config;
import dev.azzybar.pathbound.item.Item;
import dev.azzybar.pathbound.item.Armor;
import dev.azzybar.pathbound.item.Weapon;

// For the chest image and container
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;

// Loading the image to the chest
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Chest {
    private static final Path PATH = Config.APP_DIR.resolve("maps", "images", "chest" + Config.IMAGE_EXT);
    
    // Container holding chest image
    private final ImageView view;
    private final Pane container;
    
    // Information regarding contents and location
    private Item item;
    private final int x;
    private final int y;
    
    // Constructor
    public Chest(Item item, int x, int y) throws IOException {
        this.item = item;

        // Load chest image
        try (InputStream is = Files.newInputStream(PATH)) {
            Objects.requireNonNull(is, "Image InputStream cannot be null");
            Image image = Images.load(is);
            this.view = Images.createView(image, Config.TILE_SIZE);
            this.container = new Pane(this.view);
        }

        // Sets position of chest on map
        this.x = x;
        this.y = y;
        this.container.setTranslateX(this.x * Config.TILE_SIZE);
        this.container.setTranslateY(this.y * Config.TILE_SIZE);
    }
    
    // Returns the item to player
    public void open(Player player) {
        // Decides item's subclass
        if (item instanceof Armor) {
            // Checks if the specific piece is null
            Armor chestArmor = (Armor) item;
            Armor[] armor = player.getArmor();
            if (armor[chestArmor.slot().ordinal()] == null) {
                System.out.println("Found " + chestArmor);
            } else {
                System.out.println("Replaced " + armor[chestArmor.slot().ordinal()] + " with " + chestArmor);
            }
            
            player.take(chestArmor);
        } else {
            // Checks if player has a sword or not
            Weapon chestWeapon = (Weapon) item;
            if (player.getWeapon() == null) {
                System.out.println("Found " + chestWeapon);
            } else {
                System.out.println("Replaced " + player.getWeapon().name() + " with " + chestWeapon.name());
            }
            
            player.take(chestWeapon);
        }
        
        close();
    }
    
    // Signals chest has been looted
    private void close() {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        view.setEffect(grayscale);
        item = null;
    }
    
    // Getters
    public Pane getContainer() { return container; }
    public Item getItem() { return item; }
    public boolean wasOpened() { return item == null; }
    public int getX() { return x; }
    public int getY() { return y; }
}
