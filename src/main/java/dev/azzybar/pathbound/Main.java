// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;
import dev.azzybar.pathbound.internal.Images;
import dev.azzybar.pathbound.internal.Chest;

// Required for setting up the application
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

// Used for initial loading embedded resources
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

// Listing module version if set
import java.util.Optional;
import java.lang.module.ModuleDescriptor;

public final class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Create the game
        Game game = new Game();

        // Wrap the game root inside a group
        Group scaledView = new Group(game.getViewport());
        Pane root = new StackPane(scaledView);

        // Creates scene
        Scene scene = new Scene(
            root,
            Config.NATIVE_WIDTH,
            Config.NATIVE_HEIGHT,
            Color.BLACK);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add(Config.CSS_PATH);
        scene.setOnKeyPressed(game::handleKeyPress);

        // Sets stage and title
        stage.setTitle(Config.TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        String iconPath = "/styles/icons/icon.png";
        try (InputStream is = Main.class.getResourceAsStream(iconPath)) {
            Objects.requireNonNull(is, "Image InputStream cannot be null");
            stage.getIcons().add(Images.load(is));
        }
        stage.show();

        // Post-show adjustments
        Chest.setMargins();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
