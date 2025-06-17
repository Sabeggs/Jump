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
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 2, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 2, (int) (200 * Game.SCALE), 1, Gamestate.REGISTER);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 2, (int) (270 * Game.SCALE), 3, Gamestate.LEADERBOARD);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 2, (int) (340 * Game.SCALE), 2, Gamestate.QUIT);
    }

    private void loadCustomFonts() {
        Font baseFont = LoadSave.GetFont(LoadSave.CUSTOM_FONT_ARCADE_CLASSIC);

        if (baseFont != null) {
            customGameTitleFont = baseFont.deriveFont(Font.BOLD, (float)(40 * Game.SCALE));
            customInstructionFont = baseFont.deriveFont(Font.PLAIN, (float)(16 * Game.SCALE));
        } else {
            System.err.println("custom font 'jersey15-regular.ttf' not loaded. falling back to arial.");
            customGameTitleFont = new Font("Arial", Font.PLAIN, (int)(36 * Game.SCALE));
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

        // title drawing
        String line1 = "jump up";
        String line2 = "superstar!";

        FontMetrics fm = g.getFontMetrics(customGameTitleFont);

        int line1Width = fm.stringWidth(line1);
        int line1X = (Game.GAME_WIDTH - line1Width) / 2;

        int line2Width = fm.stringWidth(line2);
        int offsetX = (int) (5.5 * Game.SCALE);        
        int line2X = (Game.GAME_WIDTH - line2Width) / 2 + offsetX;

        
        int line1Y = (int) (65 * Game.SCALE);

        int line2Y = line1Y + fm.getHeight() + (int) (-12 * Game.SCALE);

        g.drawString(line1, line1X, line1Y);
        g.drawString(line2, line2X, line2Y);

        for (MenuButton mb : buttons) {
            mb.draw(g);
        }
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
                if (mb.isMousePressed()) {
                    if (mb.getState() == Gamestate.PLAYING) {
                        game.StartNewGame();
                    } else {
                        mb.applyGamestate();
                    }
                }
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
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }
}