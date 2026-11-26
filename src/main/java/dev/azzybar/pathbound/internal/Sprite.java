// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Config;

import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Map;
public class Sprite {
    // Tile size and sprite
    private final ImageView sprite;
    private final Pane container;
    
    // Position (grid and visual)
    private int x;
    private int y;
    private double pixelX;
    private double pixelY;
    
    // Constructor
    public Sprite(Image image, int x, int y) {
        // Creates entity sprite
        this.sprite = Images.createSprite(image);
        this.container = new Pane(sprite);
        
        // Sets default position (centers player sprite)
        this.x = x;
        this.y = y;
        updatePosition(x, y);
        this.container.setTranslateX(pixelX);
        this.container.setTranslateY(pixelY);
    }
    
    // Copy constructor (deep copy, for battles)
    public Sprite(Sprite that) {
        // Copies the sprite itself and the container
        this.sprite = Images.createSprite(that.getImage());
        this.container = new Pane(sprite);
        
        // Sets to 0 (not map-dependent)
        this.x = 0;
        this.y = 0;
        this.pixelX = 0;
        this.pixelY = 0;
    }
    
    // Updates visual position
    public void updatePosition(int x, int y) {
        this.pixelX = x * Config.TILE_SIZE;
        this.pixelY = y * Config.TILE_SIZE;
    }
    
    // Getters
    public ImageView getView() { return sprite; }
    public Image getImage() { return sprite.getImage(); }
    public Pane getContainer() { return container; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    
    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
