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

    // background image
    private BufferedImage leaderboardBackgroundImg;

    // back button assets and state
    private BufferedImage backButtonNormalImage;
    private BufferedImage backButtonPressedImage;
    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight;
    private boolean isBackPressed;

    // custom fonts
    private Font leaderboardTitleFont;
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
            InputStream isTitle = getClass().getResourceAsStream("/fonts/Jersey15-Regular.ttf");
            if (isTitle != null) {
                leaderboardTitleFont = Font.createFont(Font.TRUETYPE_FONT, isTitle);
                leaderboardTitleFont = leaderboardTitleFont.deriveFont(Font.BOLD, (float) (36 * Game.SCALE));
                isTitle.close();
            } else {
                System.err.println("font file 'jersey15-regular.ttf' not found for leaderboard title. using default font.");
                leaderboardTitleFont = new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE));
            }

            InputStream isContent = getClass().getResourceAsStream("/fonts/dogicapixel.ttf");
            if (isContent != null) {
                leaderboardContentFont = Font.createFont(Font.TRUETYPE_FONT, isContent);
                leaderboardContentFont = leaderboardContentFont.deriveFont(Font.PLAIN, (float) (24 * Game.SCALE));
                isContent.close();
            } else {
                System.err.println("font file 'dogicapixel.ttf' not found for leaderboard content. using default font.");
                leaderboardContentFont = new Font("Arial", Font.PLAIN, (int) (24 * Game.SCALE));
            }

        } catch (FontFormatException | IOException e) {
            System.err.println("error loading a custom font for leaderboard: " + e.getMessage());
            e.printStackTrace();
            leaderboardTitleFont = new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE));
            leaderboardContentFont = new Font("Arial", Font.PLAIN, (int) (24 * Game.SCALE));
        }
    }

    private void initUIBounds() {
        backButtonWidth = (int) (100 * Game.SCALE);
        backButtonHeight = (int) (backButtonWidth / 2);
        backButtonX = (int) (20 * Game.SCALE);
        backButtonY = (int) (20 * Game.SCALE);
    }

    private void loadLeaderboardData() {
        leaderboardData.clear();
        File csvFile = new File(CSV_FILE_NAME);

        if (!csvFile.exists()) {
            System.out.println("leaderboard: csv file not found: " + CSV_FILE_NAME);
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
                        String timeString = parts[1].trim();

                        if (timeString.startsWith("\"") && timeString.endsWith("\"") && timeString.length() > 1) {
                            timeString = timeString.substring(1, timeString.length() - 1);
                        }

                        long timeMillis = Long.parseLong(timeString);
                        leaderboardData.add(new ScoreEntry(username, timeMillis));
                    } catch (NumberFormatException e) {
                        System.err.println("leaderboard: skipping malformed line (invalid time format): " + line);
                    }
                } else {
                    System.err.println("leaderboard: skipping malformed line (incorrect parts count): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("leaderboard: error reading csv file: " + e.getMessage());
            e.printStackTrace();
        }

        Collections.sort(leaderboardData, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry s1, ScoreEntry s2) {
                boolean s1IsDnf = (s1.getTimeMillis() == 0);
                boolean s2IsDnf = (s2.getTimeMillis() == 0);

                if (s1IsDnf && !s2IsDnf) {
                    return 1;
                } else if (!s1IsDnf && s2IsDnf) {
                    return -1;
                } else {
                    return Long.compare(s1.getTimeMillis(), s2.getTimeMillis());
                }
            }
        });
    }

    private String formatTime(long timeMillis) {
        if (timeMillis == 0) {
            return "dnf";
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
            float stretchFactorX = 1.2f;
            float stretchFactorY = 1.5f;

            int stretchedWidth = (int) (Game.GAME_WIDTH * stretchFactorX);
            int stretchedHeight = (int) (Game.GAME_HEIGHT * stretchFactorY);

            int offsetX = (stretchedWidth - Game.GAME_WIDTH) / 2;
            int offsetY = (stretchedHeight - Game.GAME_HEIGHT) / 2;

            g.drawImage(leaderboardBackgroundImg, -offsetX, -offsetY, stretchedWidth, stretchedHeight, null);
        } else {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        // drawing leaderboard title
        g.setColor(Color.WHITE);
        if (leaderboardTitleFont != null) {
            g.setFont(leaderboardTitleFont);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE)));
        }
        String title = "leaderboard";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int) (Game.GAME_HEIGHT / 2 - 150 * Game.SCALE));

        
        // calculate box bounds
        int boxWidth = (int) (400 * Game.SCALE);
        int boxHeight = (int) (250 * Game.SCALE);
        int boxX = Game.GAME_WIDTH / 2 - boxWidth / 2;
        int boxY = (int) (Game.GAME_HEIGHT / 2 - 100 * Game.SCALE);

        // draw semi-transparent box
        g.setColor(new Color(100, 100, 100, 180));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));

        // set font for headers
        g.setColor(Color.YELLOW);
        if (leaderboardContentFont != null) {
            g.setFont(leaderboardContentFont.deriveFont(Font.BOLD, (float) (22 * Game.SCALE)));
        } else {
            g.setFont(new Font("Arial", Font.BOLD, (int) (22 * Game.SCALE)));
        }

        int headerX = boxX + (int) (20 * Game.SCALE);
        int headerY = boxY + (int) (30 * Game.SCALE);
        int usernameColX = headerX + (int) (40 * Game.SCALE);
        int timeColX = headerX + (int) (250 * Game.SCALE);

        g.drawString("", headerX, headerY);
        g.drawString("username", usernameColX, headerY);
        g.drawString("time", timeColX, headerY);

        // draw leaderboard content
        g.setColor(Color.WHITE);
        if (leaderboardContentFont != null) {
            g.setFont(leaderboardContentFont);
        } else {
            g.setFont(new Font("Arial", Font.PLAIN, (int) (20 * Game.SCALE)));
        }

        int currentY = headerY + (int) (30 * Game.SCALE);
        int lineHeight = (int) (28 * Game.SCALE);

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
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int) (16 * Game.SCALE)));
        String backText = "";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        g.drawString(backText, backButtonX + backButtonWidth / 2 - backTextWidth / 2, backButtonY + (int) (backButtonHeight * 0.7));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isIn(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
            isBackPressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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