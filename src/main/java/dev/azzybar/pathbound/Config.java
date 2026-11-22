// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;

// For getting paths
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;
public final class Config {
    // For getting styles and dynamic assets
    public static final Path APP_DIR = resolveAppDir();
    public static final String CSS_PATH = Config.class.getResource("/styles/stylesheet.css").toExternalForm();

    // Information about tile size and map dimensions
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 20;
    public static final int HEIGHT = 15;

    // Native screen resolution (640x480)
    public static final double NATIVE_WIDTH = TILE_SIZE * WIDTH;
    public static final double NATIVE_HEIGHT = TILE_SIZE * HEIGHT;

    // Prevents outside instantiation
    private Config() {}

    // Gets the root path depending on how it's launched
    private static Path resolveAppDir() {
        // If launched after jpackage
        String appDir = System.getProperty("app.dir");
        if (appDir != null) {
            return Path.of(appDir).toAbsolutePath();
        }

        // If launched after creating jar file
        try {
            Path codeLocation = Path.of(Config.class.getProtectionDomain()
                                               .getCodeSource()
                                               .getLocation()
                                               .toURI());
            if (Files.isRegularFile(codeLocation)) {
                return codeLocation.getParent();
            }
        } catch (URISyntaxException e) {
        }

        // Running from the compiled class files
        return Path.of("").toAbsolutePath();
    }
}
