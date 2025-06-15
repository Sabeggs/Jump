package gamestates;

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

    private MenuButton[] buttons = new MenuButton[4];
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
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 2, (int) (130 * Game.SCALE), 0, Gamestate.PLAYER_SELECTION);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 2, (int) (200 * Game.SCALE), 1, Gamestate.REGISTER);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 2, (int) (270 * Game.SCALE), 3, Gamestate.LEADERBOARD);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 2, (int) (340 * Game.SCALE), 2, Gamestate.QUIT);
    }
    
    private void loadCustomFonts() {
        Font baseFont = LoadSave.GetFont(LoadSave.CUSTOM_FONT_JERSEY);

        if (baseFont != null) {
            customGameTitleFont = baseFont.deriveFont(Font.BOLD, (float)(36 * Game.SCALE));
            customInstructionFont = baseFont.deriveFont(Font.PLAIN, (float)(16 * Game.SCALE));
        } else {
            // Fallback to Arial if your custom font file isn't found or loaded correctly
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
        // No specific typing logic needed for the Menu screen, so leave empty.
        // This method is required because Statemethods now defines it.
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImgPink, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);

        // --- NEW: Text Overlay ---
        // Set font for the title
        g.setFont(new Font("Jersey15-Regular", Font.BOLD, (int) (36 * Game.SCALE))); // Larger font for title
        g.setColor(new Color(255, 255, 255)); // White color for text

        String gameTitle = "Jump up \n Superstar!";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(gameTitle);
        int titleX = (Game.GAME_WIDTH - titleWidth) / 2;
        int titleY = (int) (85 * Game.SCALE); // Adjust Y position as needed
        g.drawString(gameTitle, titleX, titleY);

//        // Optional: Smaller instruction text
//        g.setFont(new Font("Arial", Font.PLAIN, (int) (16 * Game.SCALE))); // Smaller font for instruction
//        g.setColor(new Color(200, 200, 200)); // Slightly darker white/light gray
//        String instructionText = "A Platformer Adventure"; // Or "Click a button to start!"
//        int instructionWidth = fm.stringWidth(instructionText); // Recalculate for new font
//        int instructionX = (Game.GAME_WIDTH - instructionWidth) / 2;
//        int instructionY = (int) (titleY + fm.getHeight() + (5 * Game.SCALE)); // Below the title
//        g.drawString(instructionText, instructionX, instructionY);
//        // --- END NEW: Text Overlay ---


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
        for (MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                if (mb.isMousePressed())
                    mb.applyGamestate();
                if (mb.getState() == Gamestate.PLAYING)
                    game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
                break;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

}