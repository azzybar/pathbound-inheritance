// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound;
import dev.azzybar.pathbound.internal.Images;
import dev.azzybar.pathbound.internal.Chest;

// Required for setting up the application
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

// For dynamic window scaling via integer scaling
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

// Used for initial loading embedded resources
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Create the game
        Game game = new Game();
        Pane viewport = game.getViewport();

        // Wrap the game root inside a group
        Group scaledView = new Group(viewport);
        Pane root = new StackPane(scaledView);

        // Creates scene starting at x1.5 scale (960x720)
        Scene scene = new Scene(
            root,
            Config.DEFAULT_WIDTH,
            Config.DEFAULT_HEIGHT,
            Color.BLACK);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add(Config.CSS_PATH);
        scene.setOnKeyPressed(game::handleKeyPress);

        // Sets stage and title
        stage.setTitle(Config.TITLE);
        stage.setScene(scene);
        String iconPath = "/styles/icons/icon.png";
        try (InputStream is = Main.class.getResourceAsStream(iconPath)) {
            Objects.requireNonNull(is, "Image InputStream cannot be null");
            stage.getIcons().add(Images.load(is));
        }
        stage.show();

        // Waits to set constraints on stage size and adds bindings
        ChangeListener<Number> borderListener = new ChangeListener<Number>() {
            private boolean initialized = false;
            private double borderWidth;
            private double borderHeight;

            private void finalizeWindow() {
                // Ensures changes only run once
                if (initialized) { return; }
                initialized = true;

                // Prevents window from being smaller than game
                stage.setMinWidth(Config.NATIVE_WIDTH + borderWidth);
                stage.setMinHeight(Config.NATIVE_HEIGHT + borderHeight);

                // Allows window to be scaled while keeping ratio
                NumberBinding windowScale = Bindings.createDoubleBinding(() -> {
                    double scaleX = scene.getWidth() / Config.NATIVE_WIDTH;
                    double scaleY = scene.getHeight() / Config.NATIVE_HEIGHT;
                    return Math.max(1.0, Math.min(scaleX, scaleY));
                }, scene.widthProperty(), scene.heightProperty());
                viewport.scaleXProperty().bind(windowScale);
                viewport.scaleYProperty().bind(windowScale);

                // Removes the listener
                stage.heightProperty().removeListener(this);
                scene.heightProperty().removeListener(this);
            }

            @Override
            public void changed(ObservableValue<? extends Number> obs,
                                Number oldVal,
                                Number newVal) {
                // Gets difference after window decoration added
                borderWidth = stage.getWidth() - scene.getWidth();
                borderHeight = stage.getHeight() - scene.getHeight();

                // Ensure scene was actually created
                if (scene.getHeight() <= 0 || borderWidth < 0 || Double.isNaN(borderWidth)) { return; }

                if (borderHeight > 0) {
                    // Border has been loaded
                    finalizeWindow();
                } else if (borderHeight == 0) {
                    // In case of borderless windows
                    Platform.runLater(() -> {
                        if (initialized) { return; }  // If border did load
                        borderWidth = stage.getWidth() - scene.getWidth();
                        borderHeight = stage.getHeight() - scene.getHeight();
                        finalizeWindow();
                    });
                }
            }
        };

        // Run whenever stage or scene changes
        stage.heightProperty().addListener(borderListener);
        scene.heightProperty().addListener(borderListener);

        // Post-show adjustments
        Chest.setMargins();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
