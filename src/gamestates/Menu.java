package gamestates;

import entities.PlayerCharacter;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import mainn.Game;
import ui.MenuButton;
import utilz.LoadSave;

public class Menu extends State implements Statemethods {

    private MenuButton[] buttons = new MenuButton[4]; // Reverted to 4 buttons
    private BufferedImage backgroundImg, backgroundImgPink;
    private int menuX, menuY, menuWidth, menuHeight;

    private Font customGameTitleFont;
    private Font customInstructionFont;

    public Menu(Game game) {
        super(game);
        loadButtons();
        loadBackground();
        loadCustomFonts();
        backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
    }

    private void loadBackground() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
        menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (25 * Game.SCALE);
    }

    private void loadButtons() {
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 2, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 2, (int) (200 * Game.SCALE), 1, Gamestate.REGISTER);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 2, (int) (270 * Game.SCALE), 3, Gamestate.LEADERBOARD); // Assuming index 3 for Leaderboard
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 2, (int) (340 * Game.SCALE), 2, Gamestate.QUIT); // Assuming index 2 for Quit
    }

    private void loadCustomFonts() {
        Font baseFont = LoadSave.GetFont(LoadSave.CUSTOM_FONT_JERSEY);

        if (baseFont != null) {
            customGameTitleFont = baseFont.deriveFont(Font.BOLD, (float)(36 * Game.SCALE));
            customInstructionFont = baseFont.deriveFont(Font.PLAIN, (float)(16 * Game.SCALE));
        } else {
            System.err.println("Custom font 'Jersey15-Regular.ttf' not loaded. Falling back to Arial.");
            customGameTitleFont = new Font("Arial", Font.BOLD, (int)(36 * Game.SCALE));
            customInstructionFont = new Font("Arial", Font.PLAIN, (int)(16 * Game.SCALE));
        }
    }

    @Override
    public void update() {
        for (MenuButton mb : buttons)
            mb.update();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImgPink, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);

        g.setFont(customGameTitleFont);
        g.setColor(new Color(255, 255, 255));

        String gameTitle = "Jump up, Superstar!";
        FontMetrics fm = g.getFontMetrics(customGameTitleFont);
        int titleWidth = fm.stringWidth(gameTitle);
        int titleX = (Game.GAME_WIDTH - titleWidth) / 2;
        int titleY = (int) (85 * Game.SCALE);
        g.drawString(gameTitle, titleX, titleY);

        for (MenuButton mb : buttons)
            mb.draw(g);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                mb.setMousePressed(true);
            }
        }
    }

  @Override
    public void mouseReleased(MouseEvent e) {
        for (MenuButton mb : buttons) { // Assuming 'buttons' is an array/list of your menu buttons
            if (isIn(e, mb)) { // 'isIn' is likely a helper method to check if mouse is over button

                if (mb.isMousePressed()) {
                    // This is where we add the new line.
                    // We check if the button being released is the "PLAYING" button.
                    if (mb.getState() == Gamestate.PLAYING) {
                        // --- ADD THIS LINE HERE ---
                        game.getPlaying().setPlayerCharacter(PlayerCharacter.FROG);
                        // -------------------------
                    }
                    mb.applyGamestate(); // This line sets the actual game state (e.g., Gamestate.PLAYING)
                }

                // This line sets the level song. It's fine where it is,
                // as it will execute if the button's state is PLAYING (which it will be after applyGamestate for that button).
                if (mb.getState() == Gamestate.PLAYING)
                    game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());

                break; // Exit loop after handling the button
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        for (MenuButton mb : buttons)
            mb.resetBools();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for (MenuButton mb : buttons)
            mb.setMouseOver(false);

        for (MenuButton mb : buttons)
            if (isIn(e, mb)) {
                mb.setMouseOver(true);
                break;
            }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }
}