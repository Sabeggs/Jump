package gamestates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

import mainn.Game;
import utilz.LoadSave;

public class Leaderboard extends State implements Statemethods {

    private static class ScoreEntry {
        String username;
        long timeMillis;

        public ScoreEntry(String username, long timeMillis) {
            this.username = username;
            this.timeMillis = timeMillis;
        }

        public String getUsername() {
            return username;
        }

        public long getTimeMillis() {
            return timeMillis;
        }
    }

    private List<ScoreEntry> leaderboardData;
    private static final String CSV_FILE_NAME = "user_scores.csv";

    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight;
    private BufferedImage backButtonImage;

    public Leaderboard(Game game) {
        super(game);
        leaderboardData = new ArrayList<>();
        loadImages();
        initUIBounds();
        loadLeaderboardData();
    }

    private void loadImages() {
        backButtonImage = null;
    }

    private void initUIBounds() {
        backButtonWidth = (int)(100 * Game.SCALE);
        backButtonHeight = (int)(30 * Game.SCALE);
        backButtonX = (int)(20 * Game.SCALE);
        backButtonY = (int)(20 * Game.SCALE);
    }

    private void loadLeaderboardData() {
        leaderboardData.clear();
        File csvFile = new File(CSV_FILE_NAME);

        if (!csvFile.exists()) {
            System.out.println("Leaderboard: CSV file not found: " + CSV_FILE_NAME);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        String username = parts[0].trim();
                        String timeString = parts[1].trim(); // Get the string part for time

                        // *** ADDED LOGIC: Remove surrounding quotes if they exist ***
                        if (timeString.startsWith("\"") && timeString.endsWith("\"") && timeString.length() > 1) {
                            timeString = timeString.substring(1, timeString.length() - 1);
                        }
                        
                        long timeMillis = Long.parseLong(timeString); // Parse the potentially de-quoted string
                        leaderboardData.add(new ScoreEntry(username, timeMillis));
                    } catch (NumberFormatException e) {
                        System.err.println("Leaderboard: Skipping malformed line (invalid time format): " + line);
                    }
                } else {
                    System.err.println("Leaderboard: Skipping malformed line (incorrect parts count): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Leaderboard: Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }

        Collections.sort(leaderboardData, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry s1, ScoreEntry s2) {
                return Long.compare(s1.getTimeMillis(), s2.getTimeMillis());
            }
        });
    }

    private String formatTime(long timeMillis) {
        long minutes = (timeMillis / 1000) / 60;
        long seconds = (timeMillis / 1000) % 60;
        long milliseconds = timeMillis % 1000;
        return String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(48 * Game.SCALE)));
        String title = "Leaderboard";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int)(Game.GAME_HEIGHT / 2 - 170 * Game.SCALE));

        g.setFont(new Font("Arial", Font.BOLD, (int)(28 * Game.SCALE)));
        int entryX = (int)(Game.GAME_WIDTH / 2 - 150 * Game.SCALE);
        int entryY = (int)(Game.GAME_HEIGHT / 2 - 120 * Game.SCALE);
        int lineHeight = (int)( 35 * Game.SCALE);

        g.setColor(Color.YELLOW);
        g.drawString("", entryX - (int)(50 * Game.SCALE), entryY);
        g.drawString("Username", entryX, entryY);
        g.drawString("Time", entryX + (int)(200 * Game.SCALE), entryY);

        g.setFont(new Font("Arial", Font.PLAIN, (int)(24 * Game.SCALE)));
        g.setColor(Color.WHITE);
        int currentY = entryY + lineHeight;

        for (int i = 0; i < leaderboardData.size(); i++) {
            ScoreEntry entry = leaderboardData.get(i);
            String rank = (i + 1) + ".";
            String username = entry.getUsername();
            String formattedTime = formatTime(entry.getTimeMillis());

            g.drawString(rank, entryX - (int)(50 * Game.SCALE), currentY);
            g.drawString(username, entryX, currentY);
            g.drawString(formattedTime, entryX + (int)(200 * Game.SCALE), currentY);

            currentY += lineHeight;
            
            if (currentY > Game.GAME_HEIGHT - (int)(50 * Game.SCALE)) {
                break;
            }
        }
        
        if (backButtonImage != null) {
            g.drawImage(backButtonImage, backButtonX, backButtonY, backButtonWidth, backButtonHeight, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
            g.setColor(Color.BLACK);
            g.drawRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(16 * Game.SCALE)));
        String backText = "Back";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        g.drawString(backText, backButtonX + backButtonWidth / 2 - backTextWidth / 2, backButtonY + (int)(backButtonHeight * 0.7));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getX() >= backButtonX && e.getX() <= backButtonX + backButtonWidth &&
            e.getY() >= backButtonY && e.getY() <= backButtonY + backButtonHeight) {
            Gamestate.state = Gamestate.MENU;
            loadLeaderboardData();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {    }

    @Override
    public void mouseMoved(MouseEvent e) {    }

    @Override
    public void mouseClicked(MouseEvent e) {    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
            loadLeaderboardData();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {    }

    @Override
    public void keyTyped(KeyEvent e) {    }
}