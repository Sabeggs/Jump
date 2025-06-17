package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import gamestates.Gamestate;
import mainn.GamePanel;

public class KeyboardInputs implements KeyListener {

    private GamePanel gamePanel;

    public KeyboardInputs(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (Gamestate.state) {
            case MENU -> gamePanel.getGame().getMenu().keyReleased(e);
            case PLAYING -> gamePanel.getGame().getPlaying().keyReleased(e);
            case OPTIONS -> gamePanel.getGame().getGameOptions().keyReleased(e);
            case REGISTER -> gamePanel.getGame().getRegister().keyReleased(e);
            case LEADERBOARD -> gamePanel.getGame().getLeaderboard().keyReleased(e); 
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (Gamestate.state) {
            case MENU -> gamePanel.getGame().getMenu().keyPressed(e);
            case PLAYING -> gamePanel.getGame().getPlaying().keyPressed(e);
            case OPTIONS -> gamePanel.getGame().getGameOptions().keyPressed(e);
            case REGISTER -> gamePanel.getGame().getRegister().keyPressed(e);
            case LEADERBOARD -> gamePanel.getGame().getLeaderboard().keyPressed(e);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (Gamestate.state) {
            case REGISTER -> gamePanel.getGame().getRegister().keyTyped(e);
            case LEADERBOARD -> gamePanel.getGame().getLeaderboard().keyTyped(e);
        }
    }
}