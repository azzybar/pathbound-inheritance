// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.ui;
import dev.azzybar.pathbound.Config;
import dev.azzybar.pathbound.internal.Images;
import dev.azzybar.pathbound.internal.BattleEncounter;
import dev.azzybar.pathbound.internal.Player;
import dev.azzybar.pathbound.internal.Enemy;

// To use battle-view.fxml
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.util.Duration;

public final class BattleViewController {
    public static final String FXML_PATH = "/fxml/battle-view.fxml";

    // Information specific to current encounter
    private BattleEncounter encounter;

    // Battle view that goes on top of overworld
    @FXML private StackPane view;

    // Nodes that make up the view
    @FXML private ImageView enemyView;
    @FXML private Label battleText;
    @FXML private HBox actionMenu;
    @FXML private Button fight, guard, item, pray;

    // Player's health
    @FXML private HBox hpBox;
    @FXML private Label hpDisplay;
    @FXML private Rectangle hpBar;

    public void initBattle(BattleEncounter encounter) {
        this.encounter = encounter;
        encounter.setOnUIUpdate(this::updateGUI);

        // Adds enemy image scaled up
        Enemy enemy = encounter.getEnemy();
        Image scaledEnemy = Images.scale(enemy.getSprite().getImage(), 5);
        enemyView.setImage(scaledEnemy);
        enemyView.setFitWidth(scaledEnemy.getWidth());
        enemyView.setFitHeight(scaledEnemy.getHeight());

        battleText.setText("");
    }

    private void updateGUI(String message, boolean disableButton) {
        // Set buttons' usability
        fight.setDisable(disableButton);
        guard.setDisable(disableButton);
        item.setDisable(disableButton);
        pray.setDisable(disableButton);

        // Update battle text
        battleText.setText(message);

        // Update player health bar
        Player player = encounter.getPlayer();
        hpDisplay.setText("HP (" + player.getHp() + "/" + player.getMaxHp() + ")");
        hpBar.setWidth(player.getHp() * 5);
    }

    @FXML
    private void onFightAction() { encounter.handleAction("fight"); }

    @FXML
    private void onGuardAction() {}

    @FXML
    private void onItemAction() {}

    @FXML
    private void onPrayAction() {}

    @FXML
    public void initialize() {
        System.out.println("Switching to battle scene");

        // Sets size to fit screen
        view.setPrefSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        view.setMinSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);
        view.setMaxSize(Config.NATIVE_WIDTH, Config.NATIVE_HEIGHT);

        hpBar.heightProperty().bind(hpDisplay.heightProperty());

        // Adjust margins/padding for some nodes
        StackPane.setMargin(enemyView, new Insets(0, 0, (Config.TILE_SIZE * 3), 0));
        VBox.setMargin(hpBox, new Insets(Config.TILE_SIZE / 2.0, 0, 0, 50));
        actionMenu.setPadding(new Insets(0, 0, Config.TILE_SIZE, 0));

        // Offset enemy view to keep pixels sharp
        enemyView.translateXProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.ceil(enemyView.getLayoutX()) - enemyView.getLayoutX(),
                enemyView.layoutXProperty()
            )
        );
        enemyView.translateYProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.ceil(enemyView.getLayoutY()) - enemyView.getLayoutY(),
                enemyView.layoutYProperty()
            )
        );

        System.out.println("\u001B[92mSuccessfully switched to battle scene\u001B[0m");
    }
}
