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
import java.io.InputStream;
import java.awt.FontFormatException;

import mainn.Game;
import utilz.LoadSave;

public class Leaderboard extends State implements Statemethods {

    
    // --- Background Image ---
    private BufferedImage leaderboardBackgroundImg;

    // --- Back Button Assets and State ---
    private BufferedImage backButtonNormalImage;
    private BufferedImage backButtonPressedImage;
    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight;
    private boolean isBackPressed; // State for the back button (pressed/unpressed)

    // --- Custom Fonts ---
    private Font leaderboardTitleFont;   // For "Leaderboard" title (Jersey15-Regular.ttf)
    private Font leaderboardContentFont;
    
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

    private BufferedImage backButtonImage;

    public Leaderboard(Game game) {
        super(game);
        leaderboardData = new ArrayList<>();
        loadImages();
        loadFonts();
        initUIBounds();
        loadLeaderboardData();
    }

    private void loadImages() {
        leaderboardBackgroundImg = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_MAIN_BACKGROUND);
        backButtonNormalImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON);
        backButtonPressedImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON_PRESSED);
    }
    
    private void loadFonts() {
        try {
            // Load Jersey15-Regular.ttf for the Leaderboard Title
            InputStream isTitle = getClass().getResourceAsStream("/fonts/Jersey15-Regular.ttf"); // Adjust path if necessary
            if (isTitle != null) {
                leaderboardTitleFont = Font.createFont(Font.TRUETYPE_FONT, isTitle);
                leaderboardTitleFont = leaderboardTitleFont.deriveFont(Font.BOLD, (float) (36 * Game.SCALE)); // Example size
                isTitle.close();
            } else {
                System.err.println("Font file 'Jersey15-Regular.ttf' not found for Leaderboard title. Using default font.");
                leaderboardTitleFont = new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE)); // Fallback
            }

            // Load dogicapixel.ttf for the Leaderboard Content (names, scores, placements)
            InputStream isContent = getClass().getResourceAsStream("/fonts/dogicapixel.ttf"); // Adjust path if necessary
            if (isContent != null) {
                leaderboardContentFont = Font.createFont(Font.TRUETYPE_FONT, isContent);
                leaderboardContentFont = leaderboardContentFont.deriveFont(Font.PLAIN, (float) (24 * Game.SCALE)); // Example size and style
                isContent.close();
            } else {
                System.err.println("Font file 'dogicapixel.ttf' not found for Leaderboard content. Using default font.");
                leaderboardContentFont = new Font("Arial", Font.PLAIN, (int) (24 * Game.SCALE)); // Fallback
            }

        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading a custom font for Leaderboard: " + e.getMessage());
            e.printStackTrace();
            // Fallback for all fonts if a general loading error occurs
            leaderboardTitleFont = new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE));
            leaderboardContentFont = new Font("Arial", Font.PLAIN, (int) (24 * Game.SCALE));
        }
    }

    private void initUIBounds() {
        backButtonWidth = (int) (100 * Game.SCALE);
        backButtonHeight = (int) (backButtonWidth / 2);
        backButtonX = (int) (20 * Game.SCALE); // Position top-left
        backButtonY = (int) (20 * Game.SCALE);
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
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        String username = parts[0].trim();
                        String timeString = parts[1].trim();

                        // Remove surrounding quotes if they exist
                        if (timeString.startsWith("\"") && timeString.endsWith("\"") && timeString.length() > 1) {
                            timeString = timeString.substring(1, timeString.length() - 1);
                        }

                        long timeMillis = Long.parseLong(timeString);
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
                boolean s1IsDnf = (s1.getTimeMillis() == 0);
                boolean s2IsDnf = (s2.getTimeMillis() == 0);

                if (s1IsDnf && !s2IsDnf) {
                    return 1; // s1 (DnF) goes AFTER s2 (valid time)
                } else if (!s1IsDnf && s2IsDnf) {
                    return -1; // s1 (valid time) goes BEFORE s2 (DnF)
                } else {
                    return Long.compare(s1.getTimeMillis(), s2.getTimeMillis());
                }
            }
        });
    }

    private String formatTime(long timeMillis) {
        if (timeMillis == 0) {
            return "DnF";
        }
        long minutes = (timeMillis / 1000) / 60;
        long seconds = (timeMillis / 1000) % 60;
        long milliseconds = timeMillis % 100;
        return String.format("%02d:%02d:%d", minutes, seconds, milliseconds);
    }


    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        if (leaderboardBackgroundImg != null) {
            float stretchFactorX = 1.2f; // Make it 20% wider than the screen
            float stretchFactorY = 1.5f; // Make it 20% taller than the screen

            int stretchedWidth = (int) (Game.GAME_WIDTH * stretchFactorX);
            int stretchedHeight = (int) (Game.GAME_HEIGHT * stretchFactorY);

            int offsetX = (stretchedWidth - Game.GAME_WIDTH) / 2;
            int offsetY = (stretchedHeight - Game.GAME_HEIGHT) / 2;

            g.drawImage(leaderboardBackgroundImg, -offsetX, -offsetY, stretchedWidth, stretchedHeight, null);
        } else {
            // Fallback: semi-transparent black if image fails to load
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        // --- Drawing Leaderboard Title (uses Jersey15-Regular.ttf) ---
        g.setColor(Color.WHITE);
        if (leaderboardTitleFont != null) {
            g.setFont(leaderboardTitleFont);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE))); // Fallback
        }
        String title = "Leaderboard";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int) (Game.GAME_HEIGHT / 2 - 150 * Game.SCALE));

        
        // --- Calculate bounds for the transparent gray box ---
        int boxWidth = (int) (400 * Game.SCALE);
        int boxHeight = (int) (250 * Game.SCALE); // Enough space for header + 8 entries + padding
        int boxX = Game.GAME_WIDTH / 2 - boxWidth / 2;
        int boxY = (int) (Game.GAME_HEIGHT / 2 - 100 * Game.SCALE); // Position below title

        // --- Draw Semi-Transparent Gray Box ---
        g.setColor(new Color(100, 100, 100, 180)); // R, G, B, Alpha (0-255)
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE)); // Rounded rectangle for aesthetics

        // --- Set font for headers (bold dogicapixel.ttf) ---
        g.setColor(Color.YELLOW); // Header color
        if (leaderboardContentFont != null) {
            g.setFont(leaderboardContentFont.deriveFont(Font.BOLD, (float) (22 * Game.SCALE))); // Slightly larger and bold for headers
        } else {
            g.setFont(new Font("Arial", Font.BOLD, (int) (22 * Game.SCALE))); // Fallback
        }

        // Header positions relative to the box
        int headerX = boxX + (int) (20 * Game.SCALE);
        int headerY = boxY + (int) (30 * Game.SCALE);
        int usernameColX = headerX + (int) (40 * Game.SCALE); // For username
        int timeColX = headerX + (int) (250 * Game.SCALE); // For time

        g.drawString("", headerX, headerY);
        g.drawString("Username", usernameColX, headerY);
        g.drawString("Time", timeColX, headerY);

        // --- Draw Leaderboard Content (names, scores, placements - uses dogicapixel.ttf) ---
        g.setColor(Color.WHITE); // Content color
        if (leaderboardContentFont != null) {
            g.setFont(leaderboardContentFont); // Use the plain version for entries
        } else {
            g.setFont(new Font("Arial", Font.PLAIN, (int) (20 * Game.SCALE))); // Fallback
        }

        int currentY = headerY + (int) (30 * Game.SCALE); // Starting Y for first entry, below header
        int lineHeight = (int) (28 * Game.SCALE); // Spacing between entries

        for (int i = 0; i < Math.min(leaderboardData.size(), 7); i++) {
            ScoreEntry entry = leaderboardData.get(i);
            String rank = (i + 1) + ".";
            String username = entry.getUsername();
            String formattedTime = formatTime(entry.getTimeMillis());

            g.drawString(rank, headerX, currentY);
            g.drawString(username, usernameColX, currentY);
            g.drawString(formattedTime, timeColX, currentY);

            currentY += lineHeight;
        }
        
        BufferedImage backImgToDraw = isBackPressed ? backButtonPressedImage : backButtonNormalImage;
        if (backImgToDraw != null) {
            g.drawImage(backImgToDraw, backButtonX, backButtonY, backButtonWidth, backButtonHeight, null);
        } else {
            g.setColor(isBackPressed ? Color.DARK_GRAY : Color.RED);
            g.fillRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
            g.setColor(Color.BLACK);
            g.drawRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        }
        g.setColor(Color.WHITE); // Back button text color
        g.setFont(new Font("Arial", Font.BOLD, (int) (16 * Game.SCALE))); // Default font for back button text (assuming no custom font specified for it)
        String backText = "";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        g.drawString(backText, backButtonX + backButtonWidth / 2 - backTextWidth / 2, backButtonY + (int) (backButtonHeight * 0.7));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // --- Same logic as Register ---
        if (isIn(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
            isBackPressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // --- Same logic as Register ---
        if (isBackPressed) {
            if (isIn(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
                Gamestate.state = Gamestate.MENU;
            }
            isBackPressed = false;
        }
    }

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