// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;

// For getting paths
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;

// Gets module version is set
import java.util.Optional;
import java.lang.module.ModuleDescriptor;

public final class Config {
    // App name and version for setting stage title
    public static final String NAME = "Pathbound: Inheritance";
    public static final String VERSION = resolveVersion();
    public static final String TITLE = (VERSION == null)
                                       ? NAME
                                       : NAME + " (v" + VERSION + ")";

    // For getting styles, and dynamic and embedded assets
    public static final Path APP_DIR = resolveAppDir();
    public static final String CSS_PATH = Config.class.getResource("/styles/stylesheet.css").toExternalForm();

    // File extensions regarding items and image files
    public static final String ITEM_EXT = ".txt";
    public static final String IMAGE_EXT = ".png";

    // Information about tile size and map dimensions
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 20;
    public static final int HEIGHT = 15;

    // Native screen resolution (640x480)
    public static final double NATIVE_WIDTH = TILE_SIZE * WIDTH;
    public static final double NATIVE_HEIGHT = TILE_SIZE * HEIGHT;

    // Default resolution at window launch (960x720)
    public static final double DEFAULT_SCALE = 1.5;
    public static final double DEFAULT_WIDTH = NATIVE_WIDTH * DEFAULT_SCALE;
    public static final double DEFAULT_HEIGHT = NATIVE_HEIGHT * DEFAULT_SCALE;

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
        } catch (URISyntaxException e) {}

        // Running from the compiled class files
        return Path.of("").toAbsolutePath();
    }

    // Retrieves module version
    private static String resolveVersion() {
        ModuleDescriptor descriptor = Config.class.getModule().getDescriptor();
        if (descriptor != null) {
            Optional<ModuleDescriptor.Version> moduleVer = descriptor.version();
            if (moduleVer.isPresent()) {
                return moduleVer.get().toString();
            }
        }
        return null;
    }
}
