import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.event.ActionEvent;
/**
 * This is mean to be an associaton class, a regular class
 * that holds information regarding the relationship between
 * two or more classes. Here it is meant to hold information
 * specific to the player and current enemy. It also acts as
 * the battle manager for handling turn order.
 */
public class BattleEncounter {
    // Information regarding main stage and holds a
    // reference to the main map to switched back to after
    // battle ends.
    private final Stage STAGE;
    private final Scene MAP_SCENE;
    private final GameMap MAP;
    
    private Label battleText;
    private Button fight;
    private Button guard;
    private Button item;
    private Button pray;
    
    private final Player player;;
    
    private final Enemy enemy;
    private Sprite enemySprite;
    
    // 0 = Player's turn, 1 = Enemy's turn
    private int turnOrder;
    
    // Constructor
    public BattleEncounter(Player player, Enemy enemy, Stage stage, GameMap map) {
        // For testing and developing purposes
        System.out.println("Starting battle encounter with " + enemy.getName() + "...");
        enemy.printStats();
        
        // Determines who goes first
        this.turnOrder = (player.getSPD() >= enemy.getSPD()) ? 0 : 1;
        
        // Action buttons
        this.fight = new Button("FIGHT");
        this.guard = new Button("GUARD");
        this.item = new Button("ITEM");
        this.pray = new Button("PRAY");
        
        // Disables buttons
        fight.setDisable(true);
        guard.setDisable(true);
        item.setDisable(true);
        pray.setDisable(true);
        
        // Sets up hp status and message
        this.player = player;
        this.battleText = new Label("");
        
        // Creates a deep copy of enemy sprite
        this.enemy = enemy;
        this.enemySprite = new Sprite(enemy.getName(), enemy.getSprite());
        
        // Creates battle scene and stores main scene
        this.STAGE = stage;
        this.MAP_SCENE = STAGE.getScene();
        this.MAP = map;
        switchScene();
    }
    
    public void start() {
        // Starts battle
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(event -> {
            updateGUI();
            if (turnOrder == 0) {
                handlePlayerTurn();
            } else {
                handleEnemyTurn();
            } 
        });
        pause.play();
    }
    
    private void handlePlayerTurn() {
        // Checks entities' health
        if (player.getHp() <= 0 || enemy.getHp() <= 0) {
            end();
            return;
        }
        
        // Sets onAction events
        fight.setOnAction(this::handleAction);
    }
    
    // Handles battle action
    private void handleAction(ActionEvent event) {
        // Gets source of the event
        Button source = (Button) event.getSource();
        
        // Changes turn order
        turnOrder = 1;
        
        if (source == fight) {
            // Player takes turn
            int dmg = player.takeTurn("fight");
            enemy.takeDmg(enemy.getName(), dmg);
            
            // Checks entities' health again
            if (player.getHp() <= 0 || enemy.getHp() <= 0) {
                end();
                return;
            }
        }
        
        // Disables buttons
        fight.setDisable(true);
        guard.setDisable(true);
        item.setDisable(true);
        pray.setDisable(true);
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            // Switches control to enemy
            System.out.println("\u001B[36mSwitching to enemy\u001B[0m");
            updateGUI();
            handleEnemyTurn();
        });
        pause.play();
    }
    
    private void handleEnemyTurn() {
        // Checks entities' health
        if (player.getHp() <= 0 || enemy.getHp() <= 0) {
            end();
            return;
        }
        
        // Enemy takes turn
        int dmg = enemy.takeTurn();
        player.takeDmg("Player", dmg);
        
        // Checks entities' health again
        if (player.getHp() <= 0 || enemy.getHp() <= 0) {
            end();
            return;
        }
        
        // Adds a pause
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            // Switches control to player
            System.out.println("\u001B[36mSwitching to player\u001B[0m");
            turnOrder = 0;
            updateGUI();
            handlePlayerTurn();
        });
        pause.play();
    }
    
    // Simply for updating battle text and health
    private void updateGUI() {
        if (turnOrder == 0) {
            // Re-enables buttons
            fight.setDisable(false);
            guard.setDisable(false);
            item.setDisable(false);
            pray.setDisable(false);
            
            // battleText.setText("Your turn.");
            battleText.setText("Your turn.");
            player.status().update();
        } else {
            battleText.setText("Enemy's turn.");
        }
    }
    
    // Ends battle (regardless of who lost)
    private void end() {
        // Checks who died
        if (enemy.getHp() == 0) {
            System.out.println("Player has defeated " + enemy.getName());
            enemy.kill();
            
            // Resets player's and removes enemy from map
            player.resetHp();
            player.status().update();
            MAP.removeEnemy(enemy);
            
            // Switches back to map
            System.out.println("Switching back to map");
            STAGE.setScene(MAP_SCENE);
            System.out.println("\u001B[36mSuccessfully switched to map\u001B[0m");
        } else {
            // Creates death screen
            ImageView playerSprite = new Sprite("player", player.getSprite()).get();
            Text deathText = new Text("You have succumbed to death.");
            deathText.setId("death-text");
            
            StackPane deathScreen = new StackPane(playerSprite, deathText);
            deathScreen.setId("death-screen");
            
            // Creates dedicated scene to end game on
            Scene scene = new Scene(
                deathScreen, 
                MAP.getSceneWidth(), 
                MAP.getSceneHeight(), 
                Color.BLACK);
            scene.getStylesheets().add("Styles/stylesheet.css");
            STAGE.setScene(scene);
            
            // Plays game over animation
            gameOver(playerSprite, deathText);
        }
    }
    
    private void gameOver(ImageView sprite, Text text) {
        // Slowly grays the player's sprite
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(0);
        sprite.setEffect(grayscale);
        
        // Creates timeline
        KeyValue grayValue = new KeyValue(grayscale.saturationProperty(), -1.0);
        KeyFrame grayFrame = new KeyFrame(Duration.seconds(4), grayValue);
        Timeline grayTimeline = new Timeline(grayFrame);
        
        // Makes the text red and opaque
        FadeTransition fadeText = new FadeTransition(Duration.seconds(5), text);
        fadeText.setFromValue(0);
        fadeText.setToValue(1);
        fadeText.setInterpolator(Interpolator.LINEAR);
        
        // Creates a sequence to play
        SequentialTransition sequence = new SequentialTransition(
            new PauseTransition(Duration.seconds(1)),
            grayTimeline,
            new PauseTransition(Duration.seconds(1)),
            fadeText
        );
        
        sequence.play();
    }
    
    // Creates battle scene and switches to it
    private void switchScene() {
        // Creates battle layout
        System.out.println("Switching to battle scene");
        BorderPane root = new BorderPane();
        root.setMouseTransparent(true);
        
        StackPane center = new StackPane(enemySprite.get(), battleText);
        center.setAlignment(enemySprite.get(), Pos.CENTER);
        center.setAlignment(battleText, Pos.BOTTOM_CENTER);
        root.setCenter(center);
        
        battleText.setId("battle-text");
        center.setId("center");
        root.setId("battle-pane");
        
        // Creates battle options
        HBox actionMenu = new HBox(20);
        actionMenu.getChildren().addAll(fight, guard, item, pray);
        actionMenu.setAlignment(Pos.CENTER);
        
        // Creates Battle GUI
        VBox battleGUI = new VBox(15);
        battleGUI.getChildren().addAll(player.status().get(), actionMenu);
        root.setBottom(battleGUI);
        
        // Creates battle scene
        Scene scene = new Scene(
            root, 
            MAP.getSceneWidth(), 
            MAP.getSceneHeight(), 
            Color.BLACK);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add("Styles/stylesheet.css");
        STAGE.setScene(scene);
        
        // Adjusts margins
        center.setMargin(enemySprite.get(), new Insets(0, 0, (MAP.getTileSize() * 3), 0));
        battleGUI.setMargin(player.status().get(), new Insets(MAP.getTileSize() / 2, 0, 0, 50));
        
        // Goes through the action buttons
        for (Node node : actionMenu.getChildren()) {
            if (node instanceof Button) {
                // Adds their css and margins
                Button button = (Button) node;
                button.getStyleClass().add("action-button");
                actionMenu.setMargin(button, new Insets(0, 0, MAP.getTileSize(), 0));
            }
        }
        System.out.println("\u001B[92mSuccessfully switched to battle scene\u001B[0m");
    }
}