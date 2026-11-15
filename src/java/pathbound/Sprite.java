package pathbound;

import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Map;
public class Sprite {
    // Tile size and sprite
    private final int TILE_SIZE;
    private ImageView sprite;
    private Pane container;
    
    // Position (grid and visual)
    private int x;
    private int y;
    private double pixelX;
    private double pixelY;
    
    // Constructor
    public Sprite(int tileSize, Image img, int x, int y) {
        this.TILE_SIZE = tileSize;
        
        // Creates entity sprite
        this.sprite = new ImageView(img);
        this.container = new Pane(sprite);
        
        // Sets default position (centers player sprite)
        this.x = x;
        this.y = y;
        updatePosition(x, y);
        sprite.setTranslateX(pixelX);
        sprite.setTranslateY(pixelY);
    }
    
    // Copy constructor (deep copy, for battles)
    public Sprite(String name, Sprite that) {
        this.TILE_SIZE = that.TILE_SIZE;
        
        // Copies the sprite itself and the container
        String path = Project.APP_DIR.resolve("sprites",
                                              (name.equals("player") ? "player" : "enemies"),
                                              (name + ".png"))
                                     .toUri()
                                     .toString();
        this.sprite = new ImageView(new Image(path, 150, 150, true, true));
        this.container = new Pane(sprite);
        
        // Sets to 0 (not map-dependent)
        this.x = 0;
        this.y = 0;
        this.pixelX = 0;
        this.pixelY = 0;
    }
    
    // Updates visual position
    public void updatePosition(int x, int y) {
        this.pixelX = x * TILE_SIZE;
        this.pixelY = y * TILE_SIZE;
    }
    
    // Getters
    public ImageView get() { return sprite; }
    public Pane getContainer() { return container; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    
    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
