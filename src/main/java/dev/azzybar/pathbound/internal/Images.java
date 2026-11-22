// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;
import dev.azzybar.pathbound.Config;

// Scaling images during battles
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;

// For loading assets
import java.io.InputStream;

public final class Images {
    // Sprites should be smaller than tile size
    public static final double SPRITE_SIZE = Config.TILE_SIZE * 0.875;

    // Options regarding images and imageviews
    private static final boolean PRESERVE_RATIO = true;
    private static final boolean SMOOTH = false;

    // Prevents other classes from instantiating this
    private Images() {}

    // Handles loading both embedded and dynamic images
    public static Image load(InputStream is) {
        return new Image(is);
    }

    // Creates an ImageView
    public static ImageView createView(Image image, double size) {
        ImageView view = new ImageView(image);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(PRESERVE_RATIO);
        view.setSmooth(SMOOTH);
        return view;
    }

    // Creates views for tiles and sprites
    public static ImageView createTile() {
        return createView(null, Config.TILE_SIZE);
    }

    public static ImageView createSprite(Image image) {
        return createView(image, SPRITE_SIZE);
    }

    // Specifically for scaling images
    public static ImageView createScaledView(Image image, int factor) {
        Image scaled = scale(image, factor);
        return createView(scaled, scaled.getWidth());
    }

    // Upscale 32x32 sprite by an exact integer factor
    public static Image scale(Image image, int factor) {
        if (factor < 1) {
            throw new IllegalArgumentException("Scale factor must be at least 1");
        }
        if (factor == 1) {
            return image;
        }

        // Create a writeable image upscaled by factor
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage scaled = new WritableImage(width * factor, height * factor);

        // Get associated pixel reader and writer
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = scaled.getPixelWriter();

        // Loop through pixels of original image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get current pixel of original image
                int color = reader.getArgb(x, y);

                // Repeat pixel by the factor
                for (int dy = 0; dy < factor; dy++) {
                    for (int dx = 0; dx < factor; dx++) {
                        writer.setArgb((x * factor + dx), (y * factor + dy), color);
                    }
                }
            }
        }
        return scaled;
    }
}
