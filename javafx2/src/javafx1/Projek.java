package javafx1;
import javafx1.AbstractCharacter.Move;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javafx.stage.Stage;
interface FloorListener{
    void floorChanged();
}

enum FloorTile{
    FLOOR,
    UNBREAKABLEBLOCK,
    BREAKABLEBLOCK
}

class AbstractCharacter{
    private final static int SIZE = 30;
    private int x;
    private int y;
    private int pixelsPerStep;

    protected AbstractCharacter(int x, int y, int pixelsPerStep) {
	this.x = x;
	this.y = y;
	this.pixelsPerStep = pixelsPerStep;
    }
    

    public enum Move
    {
	DOWN(0, 1),
	UP(0, -1), 
	RIGHT(1, 0),
	LEFT(-1, 0);

	private final int deltaX;
	private final int deltaY;
	Move(final int deltaX, final int deltaY) {
	    this.deltaX = deltaX;
	    this.deltaY = deltaY;
	}
    }

    public void move(Move move) {
	y += move.deltaY * pixelsPerStep;
	x += move.deltaX * pixelsPerStep;
    }

    public void moveBack(Move currentDirection) {
	if (currentDirection == Move.DOWN) {
	    move(Move.UP);
	} else if (currentDirection == Move.UP) {
	    move(Move.DOWN);
	} else if (currentDirection == Move.LEFT) {
	    move(Move.RIGHT);
	} else if (currentDirection == Move.RIGHT) {
	    move(Move.LEFT);
	}
    }

    public int getSize() {
	return SIZE;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    public int getColIndex() {
	return Floor.pixelToSquare(x);
    }

    public int getRowIndex() {
	return Floor.pixelToSquare(y);
    }
}

class AbstractPowerup{
    private final static int POWERUP_SIZE = 30;
    private final int x;
    private final int y;
    private String name = null;

    public AbstractPowerup(int x, int y) {
	this.x = x;
	this.y = y;
    }

    public void addToPlayer(Player player) {
    }

    public int getPowerupSize() {
	return POWERUP_SIZE;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    public String getName() {
	return name;
    }
}

class Bomb{
    private final static int BOMBSIZE = 30;
    private final static int STARTCOUNTDOWN = 100;
    private int timeToExplosion = STARTCOUNTDOWN;
    private final int rowIndex;
    private final int colIndex;
    private int explosionRadius;
    private boolean playerLeft;

    public Bomb(final int rowIndex, final int colIndex, int explosionRadius) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.explosionRadius = explosionRadius;
        playerLeft = false;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public static int getBOMBSIZE() {
        return BOMBSIZE;
    }

    public int getTimeToExplosion() {
        return timeToExplosion;
    }

    public void setTimeToExplosion(final int timeToExplosion) {
        this.timeToExplosion = timeToExplosion;
    }

    public int getExplosionRadius() {
        return explosionRadius;
    }

    public boolean isPlayerLeft() {
        return playerLeft;
    }

    public void setPlayerLeft(final boolean playerLeft) {
        this.playerLeft = playerLeft;
    }
}

class Explosion{
    private int rowIndex;
    private int colIndex;
    private int duration = 5;

    public Explosion(int rowIndex, int colIndex)
    {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }
}

final class Engine{
    private static final int TIME_STEP = 30;
    private static int width = 11;
    private static int height = 11;
    private static int nrOfEnemies = 1;
    private static javax.swing.Timer clockTimer = null;

    public Engine() {}

    public static void main() {
        Player.setMap(1);
	startGame();
    }

    public static void startGame() {
	Floor floor = new Floor(width, height, nrOfEnemies);
	BombermanFrame frame = new BombermanFrame("Bomberman", floor);
	frame.setLocationRelativeTo(null);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	floor.addFloorListener(frame.getBombermanComponent());

	Action doOneStep = new AbstractAction()
	{
	    public void actionPerformed(java.awt.event.ActionEvent e) {
		tick(frame, floor);
	    }
	};
	clockTimer = new javax.swing.Timer(TIME_STEP, doOneStep);
	clockTimer.setCoalesce(true);
	clockTimer.start();
    }
    
    public static javax.swing.Timer getTimer( ){
        return clockTimer;
    }
    
    private static void gameOver(BombermanFrame frame, Floor floor) {
	clockTimer.stop();
	frame.dispose();
        Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Your Score: " + Player.getScore());
        alert.setContentText("Do you want to restart the game?");

        ButtonType restartButton = new ButtonType("Restart");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(restartButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == restartButton) {
            Player.setScoreToZero();
            startGame();
        }
    });
    }

    private static void tick(BombermanFrame frame, Floor floor) {
	if (floor.getIsGameOver()) {
	    gameOver(frame, floor);
	} else {
	    floor.moveEnemies();
	    floor.bombCountdown();
	    floor.explosionHandler();
	    floor.characterInExplosion();
	    floor.notifyListeners();
	}
    }
}

class BombermanFrame extends JFrame{
    private Floor floor; 
    private BombermanComponent bombermanComponent;

    public BombermanFrame(final String title, Floor floor) throws HeadlessException {
	super(title);
	this.floor = floor;
	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	bombermanComponent = new BombermanComponent(floor);
	floor.createPlayer(bombermanComponent, floor);
	setKeyStrokes();

	this.setLayout(new BorderLayout());
	this.add(bombermanComponent, BorderLayout.CENTER);
	this.pack();
	this.setVisible(true);
    }

    public BombermanComponent getBombermanComponent() {
	return bombermanComponent;
    }

    private boolean askUser(String question) {
	return JOptionPane.showConfirmDialog(null, question, "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void setKeyStrokes() {

	KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	bombermanComponent.getInputMap().put(stroke, "q");
	bombermanComponent.getActionMap().put("q", quit);
    }

    private final Action quit = new AbstractAction()
    {
	public void actionPerformed(java.awt.event.ActionEvent e) {
		dispose();
	    
	}
    };
}



class Enemy extends AbstractCharacter{
    private Move currentDirection;

    public Enemy(int x, int y, boolean vertical) {
        super(x, y, 1);
        currentDirection = randomDirection(vertical);
    }

    public void changeDirection() {
        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        
        if (currentDirection == Move.DOWN) {
            currentDirection = Move.UP;
        } else if (currentDirection == Move.UP) {
            currentDirection = Move.DOWN;
        } else if (currentDirection == Move.LEFT) {
            currentDirection = Move.RIGHT;
        } else {
            currentDirection = Move.LEFT;
        }
    }

    public Move getCurrentDirection() {
        return currentDirection;
    }

    private Move randomDirection(boolean vertical) {
        assert Move.values().length == 4;
        int pick = (int) (Math.random() * (Move.values().length-2));
        if(vertical) {
            return Move.values()[pick];
        }
        else{
            return Move.values()[pick+2];
        }

    }
}


class Floor {
    private final static double CHANCE_FOR_BREAKABLE_BLOCK = 0.4;
    private final static double CHANCE_FOR_RADIUS_POWERUP = 0.2;
    private final static double CHANCE_FOR_COUNTER_POWERUP = 0.8;
    private FloorTile[][] tiles;
    private int width;
    private int height;
    private Collection<FloorListener> floorListeners = new ArrayList<>();
    private Player player = null;
    private Collection<Enemy> enemyList = new ArrayList<>();
    private List<Bomb> bombList= new ArrayList<>();
    private Collection<AbstractPowerup> powerupList = new ArrayList<>();
    private Collection<Bomb> explosionList= new ArrayList<>();
    private Collection<Explosion> explosionCoords= new ArrayList<>();
    private boolean isGameOver = false;



public class Projek extends Application {
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button("Quick");
        Button btn1 = new Button("Big Arena");
        ImageView imageView = null;

        try {
            URL resource = getClass().getResource("/resources/grass.png");
            if (resource != null) {
                Image image = new Image(resource.toExternalForm());
                imageView = new ImageView(image);
                imageView.setFitWidth(600);
                imageView.setFitHeight(600);
                imageView.setPreserveRatio(false);
            } else {
                throw new FileNotFoundException("Resource not found: grass.webp");
            }
            btn.setStyle("-fx-background-color: #8F00FF; -fx-text-fill: white; -fx-font-size: 14px;");
            btn1.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white; -fx-font-size: 14px;");

            VBox vbox = new VBox(20);
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(btn, btn1);

            StackPane root = new StackPane();
            if (imageView != null) {
                root.getChildren().add(imageView);
            }
            root.getChildren().add(vbox);
//            Penempatan icon
//            if (iconView != null) {
//                StackPane.setAlignment(iconView, Pos.TOP_LEFT);
//                root.getChildren().add(iconView);
//            }

            Scene scene = new Scene(root, 600, 600);
            primaryStage.setTitle("Bomber Man");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();

            btn.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-size: 14px;");
            btn1.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white; -fx-font-size: 14px;");

            VBox vbox = new VBox(20);
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(btn, btn1);

            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: black;");
            root.getChildren().add(vbox);

            Scene scene = new Scene(root, 600, 600);
            primaryStage.setTitle("Bomber Man");
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        btn.setOnAction(event -> Engine.main());
        btn1.setOnAction(event -> MultiplayerEngine.main());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
