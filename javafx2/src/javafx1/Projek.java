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

final class MultiplayerEngine{
    private static final int TIME_STEP = 30;
    private static int width = 30;
    private static int height = 17;
    private static int nrOfEnemies = 1;
    private static javax.swing.Timer clockTimer = null;

    public MultiplayerEngine() {}

    public static void main() {
        Player.setMap(2);
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

    public static javax.swing.Timer getTimer(){
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

public void restartGame() {
    player = null;
    enemyList.clear();
    bombList.clear();
    powerupList.clear();
    explosionList.clear();

    int currentScore = Player.getScore(); 

    player = new Player(currentScore);
    if(Player.getMap() == 1)
    Engine.startGame();
    else MultiplayerEngine.startGame();
    notifyListeners();
}
    public Floor(int width, int height, int nrOfEnemies) {
	this.width = width;
	this.height = height;
	this.tiles = new FloorTile[height][width];
	placeBreakable();
	placeUnbreakableAndGrass();
	spawnEnemies(nrOfEnemies);
    }
    
    
    public Floor(){
    
    }

    public static int pixelToSquare(int pixelCoord){
	return ((pixelCoord + BombermanComponent.getSquareSize()-1) / BombermanComponent.getSquareSize())-1;
    }

    public FloorTile getFloorTile(int rowIndex, int colIndex) {
	return tiles[rowIndex][colIndex];
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public Player getPlayer() {
	return player;
    }

    public Collection<Enemy> getEnemyList() {
	return enemyList;
    }

    public Iterable<Bomb> getBombList() {
	return bombList;
    }

    public int getBombListSize() {
	return bombList.size();
    }

    public Iterable<AbstractPowerup> getPowerupList() {
	return powerupList;
    }

    public Iterable<Explosion> getExplosionCoords() {
	return explosionCoords;
    }

    public boolean getIsGameOver() {
	return isGameOver;
    }

    public void setIsGameOver(boolean value) {
	isGameOver = value;
    }

    public void addToBombList(Bomb bomb) {
	bombList.add(bomb);
    }

    public void createPlayer(BombermanComponent bombermanComponent, Floor floor){
	player = new Player(bombermanComponent, floor,2,0);
    }

    public int squareToPixel(int squareCoord){
	return squareCoord * BombermanComponent.getSquareSize();
    }

    public void moveEnemies() {
	if (enemyList.isEmpty()) {
        if(Player.getMap() == 1)
	    Engine.getTimer().stop();
        else MultiplayerEngine.getTimer().stop();
            Floor tes = new Floor();
            tes.restartGame();
	}
	for (Enemy e: enemyList){
	    Move currentDirection = e.getCurrentDirection();

	    if (currentDirection == Move.DOWN) {
		e.move(Move.DOWN);
	    } else if (currentDirection == Move.UP) {
		e.move(Move.UP);
	    } else if (currentDirection == Move.LEFT) {
		e.move(Move.LEFT);
	    } else {
		e.move(Move.RIGHT);
	    }

	    if (collisionWithBlock(e)) {
		e.changeDirection();
	    }

	    if (collisionWithBombs(e)) {
		e.changeDirection();
	    }

	    if (collisionWithEnemies()) {
		isGameOver = true;
	    }
	}
    }

    public void addFloorListener(FloorListener bl) {
	floorListeners.add(bl);
    }

    public void notifyListeners() {
	for (FloorListener b : floorListeners) {
	    b.floorChanged();
	}
    }

    public void bombCountdown(){
	Collection<Integer> bombIndexesToBeRemoved = new ArrayList<>();
	explosionList.clear();
	int index = 0;
	for (Bomb b: bombList) {
	    b.setTimeToExplosion(b.getTimeToExplosion() - 1);
	    if(b.getTimeToExplosion() == 0){
		bombIndexesToBeRemoved.add(index);
		explosionList.add(b);
	    }
	    index++;
	}
	for (int i: bombIndexesToBeRemoved){bombList.remove(i);}
    }

    public void explosionHandler(){
	Collection<Explosion> explosionsToBeRemoved = new ArrayList<>();
	for (Explosion e:explosionCoords) {
	    e.setDuration(e.getDuration()-1);
	    if(e.getDuration()==0){
		explosionsToBeRemoved.add(e);
	    }
	}
	for (Explosion e: explosionsToBeRemoved){explosionCoords.remove(e);}

	for (Bomb e: explosionList) {
	    int eRow = e.getRowIndex();
	    int eCol = e.getColIndex();
	    boolean northOpen = true;
	    boolean southOpen = true;
	    boolean westOpen = true;
	    boolean eastOpen = true;
	    explosionCoords.add(new Explosion(eRow, eCol));
	    for (int i = 1; i < e.getExplosionRadius()+1; i++) {
		if (eRow - i >= 0 && northOpen) {
		    northOpen = bombCoordinateCheck(eRow-i, eCol, northOpen);
		}
		if (eRow - i <= height && southOpen) {
		    southOpen = bombCoordinateCheck(eRow+i, eCol, southOpen);
		}
		if (eCol - i >= 0 && westOpen) {
		    westOpen = bombCoordinateCheck(eRow, eCol-i, westOpen);
		}
		if (eCol + i <= width && eastOpen) {
		    eastOpen = bombCoordinateCheck(eRow, eCol+i, eastOpen);
		}
	    }
	}
    }

    public void playerInExplosion(){
	for (Explosion tup:explosionCoords) {
	    if(collidingCircles(player, squareToPixel(tup.getColIndex()), squareToPixel(tup.getRowIndex()))){
		isGameOver = true;
	    }
	}
    }

    public void enemyInExplosion(){
	for (Explosion tup:explosionCoords) {
	    Collection<Enemy> enemiesToBeRemoved = new ArrayList<>();
	    for (Enemy e : enemyList) {
		if(collidingCircles(e, squareToPixel(tup.getColIndex()), squareToPixel(tup.getRowIndex()))){
		    enemiesToBeRemoved.add(e);
                    Player.incrementScore(10);
		}
	    }
	    for (Enemy e: enemiesToBeRemoved ) {
		enemyList.remove(e);
	    }
	}
    }

    public void characterInExplosion(){
	playerInExplosion();
	enemyInExplosion();
    }

    private void placeBreakable () {
	for (int i = 0; i < height; i++) {
	    for (int j = 0; j < width; j++) {
		double r = Math.random();
		if (r < CHANCE_FOR_BREAKABLE_BLOCK) {
		    tiles[i][j] = FloorTile.BREAKABLEBLOCK;
		}
	    }
	}
	clearSpawn();
    }

    private void clearSpawn () {
	tiles[1][1] = FloorTile.FLOOR;
	tiles[1][2] = FloorTile.FLOOR;
	tiles[2][1] = FloorTile.FLOOR;
    }

    private void spawnPowerup (int rowIndex, int colIndex) {
	double r = Math.random();
	if (r < CHANCE_FOR_RADIUS_POWERUP) {
	    powerupList.add(new BombRadiusPU(squareToPixel(rowIndex) + BombermanComponent.getSquareMiddle(), squareToPixel(colIndex) + BombermanComponent.getSquareMiddle()));
	} else if (r > CHANCE_FOR_COUNTER_POWERUP) {
	    powerupList.add(new BombCounterPU(squareToPixel(rowIndex) + BombermanComponent.getSquareMiddle(), squareToPixel(colIndex) + BombermanComponent.getSquareMiddle()));
	}
    }

    private void placeUnbreakableAndGrass () {
	for (int i = 0; i < height; i++) {
	    for (int j = 0; j < width; j++) {
		if ((i == 0) || (j == 0) || (i == height - 1) || (j == width - 1) || i % 2 == 0 && j % 2 == 0) {
		    tiles[i][j] = FloorTile.UNBREAKABLEBLOCK;
		} else if (tiles[i][j] != FloorTile.BREAKABLEBLOCK) {
		    tiles[i][j] = FloorTile.FLOOR;
		}
	    }
	}
    }

    private void spawnEnemies (int nrOfEnemies) {
	for (int e = 0; e < nrOfEnemies; e++){
	    while(true) {
		int randRowIndex = 1 + (int) (Math.random() * (height - 2));
		int randColIndex = 1 + (int) (Math.random() * (width - 2));
		if(getFloorTile(randRowIndex, randColIndex) != FloorTile.FLOOR){
		    continue;
		}
		if(randRowIndex==1&&randColIndex==1||randRowIndex==1&&randColIndex==2||randRowIndex==2&&randColIndex==1){
		    continue;
		}
		if((randRowIndex % 2)==0){
		    enemyList.add(new Enemy(squareToPixel(randColIndex) + BombermanComponent.getSquareMiddle(), squareToPixel(randRowIndex) + BombermanComponent.getSquareMiddle(), true));
		}
		else{
		    enemyList.add(new Enemy(squareToPixel(randColIndex) + BombermanComponent.getSquareMiddle(), squareToPixel(randRowIndex) + BombermanComponent.getSquareMiddle(), false));
		}
		break;
	    }
	}
    }



    public boolean collisionWithEnemies(){
	for (Enemy enemy : enemyList) {
	    if(collidingCircles(player, enemy.getX()-BombermanComponent.getSquareMiddle(), enemy.getY()-BombermanComponent.getSquareMiddle())){
		return true;
	    }
	}
	return false;
    }

    public boolean collisionWithBombs(AbstractCharacter abstractCharacter) {
	boolean playerLeftBomb = true;

	for (Bomb bomb : bombList) {
	    if (abstractCharacter instanceof Player) {
		playerLeftBomb = bomb.isPlayerLeft();
	    }
	    if(playerLeftBomb && collidingCircles(abstractCharacter, squareToPixel(bomb.getColIndex()), squareToPixel(bomb.getRowIndex()))){
		return true;
	    }
	}
	return false;
    }


    public boolean collisionWithBlock(AbstractCharacter abstractCharacter){
	for (int i = 0; i < height; i++) {
	    for (int j = 0; j < width; j++) {
		if(getFloorTile(i, j) != FloorTile.FLOOR){
		    boolean isIntersecting = squareCircleInstersect(i, j, abstractCharacter);
		    if (isIntersecting) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    public void collisionWithPowerup() {
	for (AbstractPowerup powerup : powerupList) {
	    if(collidingCircles(player, powerup.getX()-BombermanComponent.getSquareMiddle(), powerup.getY()-BombermanComponent.getSquareMiddle())){
		powerup.addToPlayer(player);
		powerupList.remove(powerup);
		break;
	    }
	}
    }

    public boolean squareHasBomb(int rowIndex, int colIndex){
	for (Bomb b: bombList) {
	    if(b.getRowIndex()==rowIndex && b.getColIndex()==colIndex){
		return true;
	    }
	}
	return false;
    }

    public void checkIfPlayerLeftBomb(){
	for (Bomb bomb: bombList) {
	    if(!bomb.isPlayerLeft()){
		if(!collidingCircles(player, squareToPixel(bomb.getColIndex()), squareToPixel(bomb.getRowIndex()))){
		    bomb.setPlayerLeft(true);
		}
	    }
	}
    }

    private boolean bombCoordinateCheck(int eRow, int eCol, boolean open){
	if(tiles[eRow][eCol] != FloorTile.FLOOR){open = false;}
	if(tiles[eRow][eCol] == FloorTile.BREAKABLEBLOCK){
	    tiles[eRow][eCol] = FloorTile.FLOOR;
	    spawnPowerup(eRow, eCol);
	}
	if(tiles[eRow][eCol] != FloorTile.UNBREAKABLEBLOCK){explosionCoords.add(new Explosion(eRow, eCol));}
	return open;
    }

    private boolean collidingCircles(AbstractCharacter abstractCharacter, int x, int y){
	int a = abstractCharacter.getX() - x - BombermanComponent.getSquareMiddle();
	int b = abstractCharacter.getY() - y - BombermanComponent.getSquareMiddle();
	int a2 = a * a;
	int b2 = b * b;
	double c = Math.sqrt(a2 + b2);
	return(abstractCharacter.getSize() > c);
    }

    private boolean squareCircleInstersect(int row, int col, AbstractCharacter abstractCharacter) {
	int characterX = abstractCharacter.getX();
	int characterY = abstractCharacter.getY();

	int circleRadius = abstractCharacter.getSize() / 2;
	int squareSize = BombermanComponent.getSquareSize();
	int squareCenterX = (col*squareSize)+(squareSize/2);
	int squareCenterY = (row*squareSize)+(squareSize/2);

	int circleDistanceX = Math.abs(characterX - squareCenterX);
	int circleDistanceY = Math.abs(characterY - squareCenterY);

	if (circleDistanceX > (squareSize/2 + circleRadius)) { return false; }
	if (circleDistanceY > (squareSize/2 + circleRadius)) { return false; }

	if (circleDistanceX <= (squareSize/2)) { return true; }
	if (circleDistanceY <= (squareSize/2)) { return true; }

	int cornerDistance = (circleDistanceX - squareSize/2)^2 +
							      (circleDistanceY - squareSize/2)^2;

	return (cornerDistance <= (circleRadius^2));
    }
}

class BombermanComponent extends JComponent implements FloorListener{
    private final static int SQUARE_SIZE = 40;
    private final static int CHARACTER_ADJUSTMENT_FOR_PAINT = 15;
    private final static int SQUARE_MIDDLE = SQUARE_SIZE/2;
    private final static int BOMB_ADJUSTMENT_1 =5;
    private final static int BOMB_ADJUSTMENT_2 =10;
    private final static int PAINT_PARAMETER_13 = 13;
    private final static int PAINT_PARAMETER_15 = 15;
    private final static int PAINT_PARAMETER_17 = 17;
    private final static int PAINT_PARAMETER_18 = 18;
    private final static int PAINT_PARAMETER_19 = 19;
    private final static int PAINT_PARAMETER_20 = 20;
    private final static int PAINT_PARAMETER_24 = 24;
    private final Floor floor;
    private final AbstractMap<FloorTile, Color> colorMap;

    public BombermanComponent(Floor floor) {
	this.floor = floor;

	colorMap = new EnumMap<>(FloorTile.class);
	colorMap.put(FloorTile.FLOOR, Color.GREEN);
	colorMap.put(FloorTile.UNBREAKABLEBLOCK, Color.BLACK);
	colorMap.put(FloorTile.BREAKABLEBLOCK, Color.RED);
    }

    public static int getSquareSize() {
	return SQUARE_SIZE;
    }

    public static int getSquareMiddle() {
	return SQUARE_MIDDLE;
    }

    public Dimension getPreferredSize() {
	super.getPreferredSize();
	return new Dimension(this.floor.getWidth() * SQUARE_SIZE, this.floor.getHeight() * SQUARE_SIZE);
    }

    public void floorChanged() {
	repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	final Graphics2D g2d = (Graphics2D) g;

	for (int rowIndex = 0; rowIndex < floor.getHeight(); rowIndex++) {
	    for (int colIndex = 0; colIndex < floor.getWidth(); colIndex++) {
		g2d.setColor(colorMap.get(this.floor.getFloorTile(rowIndex, colIndex)));
		if(floor.getFloorTile(rowIndex, colIndex)==FloorTile.BREAKABLEBLOCK){
		    paintBreakableBlock(rowIndex, colIndex, g2d);
		}
		else if(floor.getFloorTile(rowIndex, colIndex)==FloorTile.UNBREAKABLEBLOCK){
		    paintUnbreakableBlock(rowIndex, colIndex, g2d);
		}
		else{
		    paintFloor(rowIndex, colIndex, g2d);
		}
	    }
	}
        
        g2d.setColor(Color.WHITE); 
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Score: " + Player.getScore(), 10, 20); 
        
	paintPlayer(floor.getPlayer(), g2d);
        
	for (Enemy e: floor.getEnemyList()) {
	    paintEnemy(e, g2d);
	}

	for (AbstractPowerup p: floor.getPowerupList()) {
	    if (p.getName().equals("BombCounter")) {
		g2d.setColor(Color.BLACK);
	    } else if (p.getName().equals("BombRadius")) {
		g2d.setColor(Color.RED);
	    }
	    g2d.fillOval(p.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT, p.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT, p.getPowerupSize(), p.getPowerupSize());
	}

	for (Bomb b: floor.getBombList()) {
	    g2d.setColor(Color.BLACK);
	    int bombX = floor.squareToPixel(b.getColIndex());
	    int bombY = floor.squareToPixel(b.getRowIndex());
	    g2d.fillOval(bombX + BOMB_ADJUSTMENT_1, bombY + BOMB_ADJUSTMENT_1, Bomb.getBOMBSIZE(), Bomb.getBOMBSIZE());
	    g2d.setColor(Color.WHITE);
	    g2d.fillOval(bombX + BOMB_ADJUSTMENT_2, bombY + BOMB_ADJUSTMENT_1, BOMB_ADJUSTMENT_1, BOMB_ADJUSTMENT_2);
	}

	g2d.setColor(Color.ORANGE);
	for (Explosion tup: floor.getExplosionCoords()) {
	    g2d.fillOval(floor.squareToPixel(tup.getColIndex()) + BOMB_ADJUSTMENT_1, floor.squareToPixel(tup.getRowIndex()) +
										     BOMB_ADJUSTMENT_1, Bomb.getBOMBSIZE(), Bomb.getBOMBSIZE());
	}
    }

    private void paintFloor(int rowIndex, int colIndex, Graphics g2d){
	g2d.setColor(Color.lightGray);
	g2d.fillRect(colIndex * SQUARE_SIZE, rowIndex * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
	g2d.setColor(Color.BLUE);
	g2d.drawLine(colIndex* SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+10, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE+10);
	g2d.drawLine(colIndex* SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE);
	g2d.drawLine(colIndex* SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE+10, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE+10);
	g2d.drawLine(colIndex* SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+SQUARE_SIZE, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_SIZE);

	g2d.drawLine(colIndex* SQUARE_SIZE+10, rowIndex*SQUARE_SIZE+1, colIndex*SQUARE_SIZE+10, rowIndex*SQUARE_SIZE+10);
	g2d.drawLine(colIndex* SQUARE_SIZE+SQUARE_MIDDLE+10, rowIndex*SQUARE_SIZE+1, colIndex*SQUARE_SIZE+SQUARE_MIDDLE+10, rowIndex*SQUARE_SIZE+10);

	g2d.drawLine(colIndex* SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+10, colIndex*SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE);
	g2d.drawLine(colIndex* SQUARE_SIZE+SQUARE_MIDDLE+1, rowIndex*SQUARE_SIZE+10, colIndex*SQUARE_SIZE+SQUARE_MIDDLE+1, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE);

	g2d.drawLine(colIndex* SQUARE_SIZE+10, rowIndex*SQUARE_SIZE+1+SQUARE_MIDDLE, colIndex*SQUARE_SIZE+10, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE+10);
	g2d.drawLine(colIndex* SQUARE_SIZE+SQUARE_MIDDLE+10, rowIndex*SQUARE_SIZE+1+SQUARE_MIDDLE, colIndex*SQUARE_SIZE+SQUARE_MIDDLE+10, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE+10);

	g2d.drawLine(colIndex* SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE+10, colIndex*SQUARE_SIZE+1, rowIndex*SQUARE_SIZE+SQUARE_SIZE);
	g2d.drawLine(colIndex* SQUARE_SIZE+SQUARE_MIDDLE+1, rowIndex*SQUARE_SIZE+SQUARE_MIDDLE+10, colIndex*SQUARE_SIZE+SQUARE_MIDDLE+1, rowIndex*SQUARE_SIZE+SQUARE_SIZE);
    }

    private void paintUnbreakableBlock(int rowIndex, int colIndex, Graphics g2d){
	g2d.fillRect(colIndex * SQUARE_SIZE, rowIndex * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
	g2d.setColor(Color.DARK_GRAY);
	g2d.drawLine(colIndex* SQUARE_SIZE, rowIndex*SQUARE_SIZE, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE);
	g2d.drawLine(colIndex* SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_SIZE, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_SIZE);
	g2d.drawLine(colIndex* SQUARE_SIZE, rowIndex*SQUARE_SIZE, colIndex*SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_SIZE);
	g2d.drawLine(colIndex* SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE, colIndex*SQUARE_SIZE+SQUARE_SIZE, rowIndex*SQUARE_SIZE+SQUARE_SIZE);
    }

private void paintBreakableBlock(int rowIndex, int colIndex, Graphics g2d){
	g2d.setColor(new Color(153, 76, 0)); 
	g2d.fillRect(colIndex * SQUARE_SIZE, rowIndex * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
	
	g2d.setColor(new Color(102, 51, 0)); 
	g2d.drawLine(colIndex * SQUARE_SIZE, rowIndex * SQUARE_SIZE + SQUARE_MIDDLE, colIndex * SQUARE_SIZE + SQUARE_SIZE, rowIndex * SQUARE_SIZE + SQUARE_MIDDLE);
	g2d.drawLine(colIndex * SQUARE_SIZE, rowIndex * SQUARE_SIZE + SQUARE_MIDDLE * 2, colIndex * SQUARE_SIZE + SQUARE_SIZE, rowIndex * SQUARE_SIZE + SQUARE_MIDDLE * 2);
	
	g2d.setColor(new Color(128, 64, 0)); 
	for (int i = 1; i < 8; i++)
        g2d.drawLine(colIndex * SQUARE_SIZE + 6*i, rowIndex * SQUARE_SIZE, colIndex * SQUARE_SIZE + 6*i, rowIndex * SQUARE_SIZE + SQUARE_SIZE);
	
	g2d.setColor(Color.BLACK);
	g2d.drawRect(colIndex * SQUARE_SIZE, rowIndex * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
}


private void paintEnemy(Enemy e, Graphics g2d){
	g2d.setColor(Color.RED);
	g2d.fillOval(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT, e.getSize(), e.getSize());
	
	g2d.setColor(Color.BLACK);
	g2d.drawLine(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+4, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+7, e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+7);
	g2d.drawLine(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_19, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+7, e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_19+6, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+7);
	
	g2d.fillOval(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+4, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+9, 7, 7);
	g2d.fillOval(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_19, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+9, 7, 7);
	
	g2d.fillOval(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+5, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_20, PAINT_PARAMETER_20, 2);
	g2d.setColor(Color.BLACK);
	g2d.drawArc(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+5, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_20, PAINT_PARAMETER_20, 2, 0, -180);
	
	g2d.setColor(Color.RED);
	g2d.fillOval(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+5, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, 5, 5);
	g2d.fillOval(e.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_20, e.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, 5, 5);
}

    private void paintPlayer(Player player, Graphics g2d){

	g2d.setColor(Color.RED);
	g2d.fillOval(player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_15, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT-2, PAINT_PARAMETER_15, PAINT_PARAMETER_15);
	
	g2d.setColor(Color.LIGHT_GRAY);
	g2d.fillOval(player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT, player.getSize(), player.getSize());
	
	Color creamColor = new Color(255, 242, 203);
        g2d.setColor(creamColor);
	g2d.fillOval(player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+3, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+3, player.getSize()-6, player.getSize()-6);
	
	g2d.setColor(Color.BLACK);
	g2d.drawLine(player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_18);
	g2d.drawLine(player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_20, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+10, player.getX()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_20, player.getY()-CHARACTER_ADJUSTMENT_FOR_PAINT+PAINT_PARAMETER_18);
    }
}



class Player extends AbstractCharacter{
    private int playerNum = 2;
    private static int map;
    
    private final static int PLAYER_START_X = 30;
    private final static int PLAYER_START_Y = 60;
    private final static int PLAYER_PIXELS_BY_STEP = 4;
    private int explosionRadius;
    private int bombCount;
    private Floor floor;
    private static int score;
    
    public static int getMap(){
        return map;
    }
    
    public static void setMap(int i){
        map = i;
    }
    public static void incrementScore(int points) {
        score += points;
    }

    public void decrementScore(int points) {
        score -= points;
        if (score < 0) {
            score = 0;
        }
    }

    public static int getScore() {
        return score;
    }
    
    public static void setScoreToZero(){
        score = 0;
    }
 
    public Action up = new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    movePlayer(Move.UP);
	}
    };
 
    public Action right = new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    movePlayer(Move.RIGHT);

	}
    };
   
    public Action down = new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    movePlayer(Move.DOWN);

	}
    };
  
    public Action left = new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    movePlayer(Move.LEFT);

	}
    };

   
    public Action dropBomb = new AbstractAction()
    {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    if(!floor.squareHasBomb(getRowIndex(), getColIndex()) && floor.getBombListSize() < getBombCount()){
		floor.addToBombList(new Bomb(getRowIndex(), getColIndex(), getExplosionRadius()));
	    }
	    floor.notifyListeners();
	}
    };

    public Player(BombermanComponent bombermanComponent, Floor floor, int playerNum, int score){
	super(PLAYER_START_X*playerNum, PLAYER_START_Y, PLAYER_PIXELS_BY_STEP);
	explosionRadius = 1;
	bombCount = 1;
	this.floor = floor;
	setPlayerButtons(bombermanComponent);
        score = 0;
    }
    
    public Player(int scores){
        super(PLAYER_START_X, PLAYER_START_Y, PLAYER_PIXELS_BY_STEP);
        score = scores;
    }

    public void setPlayerButtons(BombermanComponent bombermanComponent){
    
    //jika 2 orang multiplayer
    if(playerNum == 1){
    bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
	bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
	bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moveUp");
	bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
	bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "dropBomb");
    }
    else if(playerNum == 2){
    bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("D"), "moveRight");
    bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("A"), "moveLeft");
    bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("W"), "moveUp");
    bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("S"), "moveDown");
    bombermanComponent.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "dropBomb");
    }
    bombermanComponent.getActionMap().put("moveRight", right);
	bombermanComponent.getActionMap().put("moveLeft", left);
	bombermanComponent.getActionMap().put("moveUp", up);
	bombermanComponent.getActionMap().put("moveDown", down);
	bombermanComponent.getActionMap().put("dropBomb", dropBomb);
    }

    public int getBombCount() {
	return bombCount;
    }

    public void setBombCount(int bombCount) {
	this.bombCount = bombCount;
    }

    public int getExplosionRadius() {
	return explosionRadius;
    }

    public void setExplosionRadius(int explosionRadius) {
	this.explosionRadius = explosionRadius;
    }

    private void movePlayer(Move move) {
	move(move);
	if(floor.collisionWithBlock(this)){
	    moveBack(move);
	}
	if(floor.collisionWithBombs(this)){
	    moveBack(move);
	}
	if(floor.collisionWithEnemies()){
	    floor.setIsGameOver(true);
	}

	floor.checkIfPlayerLeftBomb();
	floor.collisionWithPowerup();
	floor.notifyListeners();
    }

}

class BombRadiusPU extends AbstractPowerup
{

    public BombRadiusPU(int rowIndex, int colIndex) {
	super(colIndex, rowIndex);
    }

    public void addToPlayer(Player player) {
	int currentExplosionRadius = player.getExplosionRadius();
	player.setExplosionRadius(currentExplosionRadius + 1);
        player.incrementScore(10);
    }

    public String getName() {
	final String name = "BombRadius";
	return name;
    }
}

class BombCounterPU extends AbstractPowerup
{

    public BombCounterPU(int rowIndex, int colIndex) {
	super(colIndex, rowIndex);
    }

    public void addToPlayer(Player player) {
	int currentBombCount = player.getBombCount();
	player.setBombCount(currentBombCount + 1);
    }

    public String getName() {
	final String name = "BombCounter";
	return name;
    }
}

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

//            Untuk icon
//            URL iconResource = getClass().getResource("/resources/icon.ico");
//            ImageView iconView = null;
//            if (iconResource != null) {
//                Image icon = new Image(iconResource.toExternalForm());
//                iconView = new ImageView(icon);
//                iconView.setFitWidth(50);
//                iconView.setFitHeight(50);
//            }

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
