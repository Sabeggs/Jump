package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;

import entities.EnemyManager;
import entities.Player;
import entities.PlayerCharacter;
import levels.LevelManager;
import mainn.Game;
import objects.ObjectManager;
import ui.GameCompletedOverlay;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;
import effects.DialogueEffect;
import effects.Rain;

import static utilz.Constants.Environment.*;
import static utilz.Constants.Dialogue.*;

public class Playing extends State implements Statemethods {

    private Player player;
    private LevelManager levelManager;
    private EnemyManager enemyManager;
    private ObjectManager objectManager;
    private PauseOverlay pauseOverlay;
    private GameOverOverlay gameOverOverlay;
    private GameCompletedOverlay gameCompletedOverlay;
    private LevelCompletedOverlay levelCompletedOverlay;
    private Rain rain;

    private boolean paused = false;

    private long totalGameTimeMillis = 0;
    private long lastTimerUpdateNanos = System.nanoTime();
    
    private int xLvlOffset;
    private int leftBorder = (int) (0.25 * Game.GAME_WIDTH);
    private int rightBorder = (int) (0.75 * Game.GAME_WIDTH);
    private int maxLvlOffsetX;

    private BufferedImage backgroundImg, bigCloud, smallCloud, shipImgs[];
    private BufferedImage[] questionImgs, exclamationImgs;
    private ArrayList<DialogueEffect> dialogEffects = new ArrayList<>();

    private int[] smallCloudsPos;
    private Random rnd = new Random();

    private boolean gameOver;
    private boolean lvlCompleted;
    private boolean gameCompleted = false;
    private boolean playerDying;
    private boolean drawRain;

    // Ship will be decided to drawn here. It's just a cool addition to the game
    // for the first level. Hinting on that the player arrived with the boat.

    // If you would like to have it on more levels, add a value for objects when
    // creating the level from lvlImgs. Just like any other object.

    // Then play around with position values so it looks correct depending on where
    // you want
    // it.

    private boolean drawShip = true;
    private int shipAni, shipTick, shipDir = 1;
    private float shipHeightDelta, shipHeightChange = 0.05f * Game.SCALE;

    public Playing(Game game) {
        super(game);
        initClasses();

        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
        bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
        smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
        smallCloudsPos = new int[8];
        for (int i = 0; i < smallCloudsPos.length; i++)
            smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));

        shipImgs = new BufferedImage[4];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.SHIP);
        for (int i = 0; i < shipImgs.length; i++)
            shipImgs[i] = temp.getSubimage(i * 78, 0, 78, 72);

        loadDialogue();
        calcLvlOffset();
        loadStartLevel();
        setDrawRainBoolean();
    }

    private void loadDialogue() {
        loadDialogueImgs();

        // Load dialogue array with premade objects, that gets activated when needed.
        // This is a simple
        // way of avoiding ConcurrentModificationException error. (Adding to a list that
        // is being looped through.

        for (int i = 0; i < 10; i++)
            dialogEffects.add(new DialogueEffect(0, 0, EXCLAMATION));
        for (int i = 0; i < 10; i++)
            dialogEffects.add(new DialogueEffect(0, 0, QUESTION));

        for (DialogueEffect de : dialogEffects)
            de.deactive();
    }

    private void loadDialogueImgs() {
        BufferedImage qtemp = LoadSave.GetSpriteAtlas(LoadSave.QUESTION_ATLAS);
        questionImgs = new BufferedImage[5];
        for (int i = 0; i < questionImgs.length; i++)
            questionImgs[i] = qtemp.getSubimage(i * 14, 0, 14, 12);

        BufferedImage etemp = LoadSave.GetSpriteAtlas(LoadSave.EXCLAMATION_ATLAS);
        exclamationImgs = new BufferedImage[5];
        for (int i = 0; i < exclamationImgs.length; i++)
            exclamationImgs[i] = etemp.getSubimage(i * 14, 0, 14, 12);
    }

    public void loadNextLevel() {
        levelManager.setLevelIndex(levelManager.getLevelIndex() + 1);
        levelManager.loadNextLevel();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        resetAll();
        drawShip = false;
    }

    private void loadStartLevel() {
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        objectManager.loadObjects(levelManager.getCurrentLevel());
    }

    private void calcLvlOffset() {
        maxLvlOffsetX = levelManager.getCurrentLevel().getLvlOffset();
    }

    private void initClasses() {    
        levelManager = new LevelManager(game);
        enemyManager = new EnemyManager(this);
        objectManager = new ObjectManager(this);


        pauseOverlay = new PauseOverlay(this);
        gameOverOverlay = new GameOverOverlay(this);
        levelCompletedOverlay = new LevelCompletedOverlay(this);
        gameCompletedOverlay = new GameCompletedOverlay(this);

        rain = new Rain();
    }

    public void setPlayerCharacter(PlayerCharacter pc) {

        player = new Player(pc, this);
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
    }

    @Override
public void update() {
    // --- Timer Update Logic (keep this as is) ---
    if (!paused && !lvlCompleted && !gameOver && !playerDying) { // Timer runs only when not paused, level completed, game over, or player dying
        long now = System.nanoTime();
        long elapsedTimeNanos = now - lastTimerUpdateNanos;
        totalGameTimeMillis += elapsedTimeNanos / 1_000_000; // Convert nanoseconds to milliseconds
        lastTimerUpdateNanos = now;
    } else {
        // When paused, level completed, game over, or player dying, update lastTimerUpdateNanos to avoid a time jump when resuming
        lastTimerUpdateNanos = System.nanoTime();
    }
    // --- End Timer Update Logic ---

    if (paused) {
        pauseOverlay.update();
    } else if (lvlCompleted) {
        levelCompletedOverlay.update();
    } else if (gameCompleted) {
        // --- THIS IS THE NEW / EDITED PART ---
        gameCompletedOverlay.update(); // Allow the game completed overlay to update (e.g., for animations)

        // Only transition to the Register screen ONCE, right after game completion is detected
        // and if we are not already on the Register screen.
        if (Gamestate.state != Gamestate.REGISTER) {
            // 1. Stop any background music (e.g., level music)
            game.getAudioPlayer().stopSong(); 

            // 2. Get the Register state instance from your main Game class
            //    (This assumes you have 'public Register getRegister()' in Game.java)
            Register registerState = game.getRegister();

            // 3. Pass the final total game time to the Register state
            registerState.setFinalGameTime(totalGameTimeMillis);

            // 4. Change the game's overall state to REGISTER
            Gamestate.state = Gamestate.REGISTER;

            // Optional: You might want to reset the 'gameCompleted' flag here if your logic
            // needs it to prevent re-triggering this specific block, though changing Gamestate.state
            // will largely achieve that for this 'update()' method loop.
            // gameCompleted = false; // Uncomment if your game flow needs this reset here.
        }
        // --- END NEW / EDITED PART ---
    } else if (gameOver) {
        gameOverOverlay.update();
    } else if (playerDying) {
        player.update();
    } else {
        // Normal gameplay update
        updateDialogue();
        if (drawRain)
            rain.update(xLvlOffset);
        levelManager.update();
        objectManager.update(levelManager.getCurrentLevel().getLevelData(), player);
        player.update();
        enemyManager.update(levelManager.getCurrentLevel().getLevelData());
        checkCloseToBorder();
        if (drawShip)
            updateShipAni();
    }
}

    private void updateShipAni() {
        shipTick++;
        if (shipTick >= 35) {
            shipTick = 0;
            shipAni++;
            if (shipAni >= 4)
                shipAni = 0;
        }

        shipHeightDelta += shipHeightChange * shipDir;
        shipHeightDelta = Math.max(Math.min(10 * Game.SCALE, shipHeightDelta), 0);

        if (shipHeightDelta == 0)
            shipDir = 1;
        else if (shipHeightDelta == 10 * Game.SCALE)
            shipDir = -1;

    }

    private void updateDialogue() {
        for (DialogueEffect de : dialogEffects)
            if (de.isActive())
                de.update();
    }

    private void drawDialogue(Graphics g, int xLvlOffset) {
        for (DialogueEffect de : dialogEffects)
            if (de.isActive()) {
                if (de.getType() == QUESTION)
                    g.drawImage(questionImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
                else
                    g.drawImage(exclamationImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
            }
    }

    public void addDialogue(int x, int y, int type) {
        // Not adding a new one, we are recycling. #ThinkGreen lol
        dialogEffects.add(new DialogueEffect(x, y - (int) (Game.SCALE * 15), type));
        for (DialogueEffect de : dialogEffects)
            if (!de.isActive())
                if (de.getType() == type) {
                    de.reset(x, -(int) (Game.SCALE * 15));
                    return;
                }
    }

    private void checkCloseToBorder() {
        int playerX = (int) player.getHitbox().x;
        int diff = playerX - xLvlOffset;

        if (diff > rightBorder)
            xLvlOffset += diff - rightBorder;
        else if (diff < leftBorder)
            xLvlOffset += diff - leftBorder;

        xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
    }

    @Override
public void draw(Graphics g) {
    g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

    drawClouds(g);
    if (drawRain)
        rain.draw(g, xLvlOffset);

    if (drawShip)
        g.drawImage(shipImgs[shipAni], (int) (100 * Game.SCALE) - xLvlOffset, (int) ((288 * Game.SCALE) + shipHeightDelta), (int) (78 * Game.SCALE), (int) (72 * Game.SCALE), null);

    levelManager.draw(g, xLvlOffset);
    objectManager.draw(g, xLvlOffset);
    enemyManager.draw(g, xLvlOffset);
    player.render(g, xLvlOffset);
    objectManager.drawBackgroundTrees(g, xLvlOffset);
    drawDialogue(g, xLvlOffset);

    // --- Draw Timer Logic ---
    drawGameTimer(g);
    // --- End Draw Timer Logic ---

    // Drawing overlays after the timer ensures they cover the game world and timer
    // when active (paused, game over, etc.).
    if (paused) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        pauseOverlay.draw(g);
    } else if (gameOver)
        gameOverOverlay.draw(g);
    else if (lvlCompleted)
        levelCompletedOverlay.draw(g);
    else if (gameCompleted)
        gameCompletedOverlay.draw(g);
}

// --- New Helper Method for Drawing the Timer ---
private void drawGameTimer(Graphics g) {
    // Convert total milliseconds to minutes and seconds for display
    long totalSeconds = totalGameTimeMillis / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    // long milliseconds = totalGameTimeMillis % 1000; // Uncomment if you want to show milliseconds

    // Format the time string (e.g., "00:00")
    String timeString = String.format("%02d:%02d", minutes, seconds);
    // If you want milliseconds: String timeString = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);

    g.setColor(Color.WHITE); // Choose your desired color for the text
    
    // Adjust font size and style as needed, scaling with Game.SCALE for consistency
    // Assuming Game.SCALE is accessible and used for scaling UI elements
    g.setFont(new Font("Arial", Font.BOLD, (int)(24 * Game.SCALE))); 

    // Position the timer on the screen (adjust x and y as per your UI design)
    // Example: Top-right corner, with some padding
    int xPos = (int)(Game.GAME_WIDTH - 150 * Game.SCALE); // Adjust 150 based on text length and desired right margin
    int yPos = (int)(40 * Game.SCALE); // Adjust for top margin

    g.drawString("Time: " + timeString, xPos, yPos);
}
    
    public long getTotalGameTimeMillis() {
        return totalGameTimeMillis;
    }

    private void drawClouds(Graphics g) {
        for (int i = 0; i < 4; i++)
            g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

        for (int i = 0; i < smallCloudsPos.length; i++)
            g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
    }

    public void setGameCompleted() {
        gameCompleted = true;
    }
    public void resetGameCompleted() {
        gameCompleted = false;
    }
    
    public void startNewGameSession() {
        totalGameTimeMillis = 0; 
        lastTimerUpdateNanos = System.nanoTime(); 
        
        levelManager.setLevelIndex(0); 
        levelManager.loadNextLevel();

        resetAll();
    }

    public void resetAll() {
        gameOver = false;
        paused = false;
        lvlCompleted = false;
        playerDying = false;
        drawRain = false;

        setDrawRainBoolean();

        player.resetAll();
        enemyManager.resetAllEnemies();
        objectManager.resetAllObjects();
        dialogEffects.clear();
    }

    private void setDrawRainBoolean() {
        // This method makes it rain 20% of the time you load a level.
        if (rnd.nextFloat() >= 0.9f)
            drawRain = true;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void checkObjectHit(Rectangle2D.Float attackBox) {
        objectManager.checkObjectHit(attackBox);
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        enemyManager.checkEnemyHit(attackBox);
    }

    public void checkPotionTouched(Rectangle2D.Float hitbox) {
        objectManager.checkObjectTouched(hitbox);
    }

    public void checkSpikesTouched(Player p) {
        objectManager.checkSpikesTouched(p);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameOver) {
            if (e.getButton() == MouseEvent.BUTTON1)
                player.setAttacking(true);
            else if (e.getButton() == MouseEvent.BUTTON3)
                player.powerAttack();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver && !gameCompleted && !lvlCompleted)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    player.setLeft(true);
                    break;
                case KeyEvent.VK_D:

                    player.setRight(true);
                    break;
                case KeyEvent.VK_SPACE:
                    player.setJump(true);
                    break;
                case KeyEvent.VK_ESCAPE:
                    paused = !paused;
            }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // No specific typing logic typically needed for the main game (Playing) screen,
        // so leave empty unless you have a specific reason to process typed characters here.
        // This method is required because Statemethods now defines it.
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!gameOver && !gameCompleted && !lvlCompleted)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    player.setLeft(false);
                    break;
                case KeyEvent.VK_D:
                    player.setRight(false);
                    break;
                case KeyEvent.VK_SPACE:
                    player.setJump(false);
                    break;
            }
    }

    public void mouseDragged(MouseEvent e) {
        if (!gameOver && !gameCompleted && !lvlCompleted)
            if (paused)
                pauseOverlay.mouseDragged(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver)
            gameOverOverlay.mousePressed(e);
        else if (paused)
            pauseOverlay.mousePressed(e);
        else if (lvlCompleted)
            levelCompletedOverlay.mousePressed(e);
        else if (gameCompleted)
            gameCompletedOverlay.mousePressed(e);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameOver)
            gameOverOverlay.mouseReleased(e);
        else if (paused)
            pauseOverlay.mouseReleased(e);
        else if (lvlCompleted)
            levelCompletedOverlay.mouseReleased(e);
        else if (gameCompleted)
            gameCompletedOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameOver)
            gameOverOverlay.mouseMoved(e);
        else if (paused)
            pauseOverlay.mouseMoved(e);
        else if (lvlCompleted)
            levelCompletedOverlay.mouseMoved(e);
        else if (gameCompleted)
            gameCompletedOverlay.mouseMoved(e);
    }

    public void setLevelCompleted(boolean levelCompleted) {
        game.getAudioPlayer().lvlCompleted();
        if (levelManager.getLevelIndex() + 1 >= levelManager.getAmountOfLevels()) {
            // No more levels
            gameCompleted = true;
            levelManager.setLevelIndex(0);
            levelManager.loadNextLevel();
            resetAll();
            return;
        }
        this.lvlCompleted = levelCompleted;
    }

    public void setMaxLvlOffset(int lvlOffset) {
        this.maxLvlOffsetX = lvlOffset;
    }

    public void unpauseGame() {
        paused = false;
    }

    public void windowFocusLost() {
        player.resetDirBooleans();
    }

    public Player getPlayer() {
        return player;
    }

    public EnemyManager getEnemyManager() {
        return enemyManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public void setPlayerDying(boolean playerDying) {
        this.playerDying = playerDying;
    }


}