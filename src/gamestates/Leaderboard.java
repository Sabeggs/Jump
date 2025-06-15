package gamestates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage; // Needed for BufferedImage
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator; // Import for sorting

import mainn.Game;
import utilz.LoadSave; // Assuming LoadSave is in utilz package

public class Leaderboard extends State implements Statemethods {

    // --- Inner Class for Score Entries ---
    // This simple class will hold each row from your CSV
    private static class ScoreEntry {
        String username;
        long timeMillis; // Time in milliseconds

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

    // --- Leaderboard Data ---
    private List<ScoreEntry> leaderboardData;
    // TODO for groupmates: Ensure this CSV_FILE_NAME matches the name used by your
    // score-saving logic (e.g., in Register.java or your game completion logic)
    private static final String CSV_FILE_NAME = "user_scores.csv"; 

    // --- UI Elements for Back Button ---
    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight;
    // TODO for groupmates: This BufferedImage will hold the actual back button image.
    // It's currently set to null for placeholder drawing.
    private BufferedImage backButtonImage;

    public Leaderboard(Game game) {
        super(game);
        leaderboardData = new ArrayList<>(); // Initialize the list
        
        loadImages();      // Load placeholder or actual images
        initUIBounds();    // Set up button positions and sizes
        loadLeaderboardData(); // Read and sort score data from CSV
    }

    private void loadImages() {
        // --- Image Loading for Leaderboard ---
        // TODO for groupmates: EDIT THIS SECTION to load your actual back button image.
        // Uncomment the line below and adjust the constant if your image constant is different.
        // Example:
        // backButtonImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON);
        
        // For now, setting to null so the fallback drawing in 'draw()' is used.
        backButtonImage = null; 
    }

    private void initUIBounds() {
        // Position the back button (similar to Register screen for consistency)
        backButtonWidth = (int)(100 * Game.SCALE);
        backButtonHeight = (int)(30 * Game.SCALE);
        backButtonX = (int)(20 * Game.SCALE); // Top-left corner
        backButtonY = (int)(20 * Game.SCALE);
    }

    // --- Core Logic: Load and Sort Leaderboard Data ---
    private void loadLeaderboardData() {
        leaderboardData.clear(); // Clear existing data before reloading
        File csvFile = new File(CSV_FILE_NAME);

        // TODO for groupmates: Ensure 'user_scores.csv' exists in the root directory
        // of your project or update the CSV_FILE_NAME path accordingly.
        if (!csvFile.exists()) {
            System.out.println("Leaderboard: CSV file not found: " + CSV_FILE_NAME);
            return; // No file, no data to load
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // TODO for groupmates: If your CSV does NOT have a header row, comment out the line below.
            br.readLine(); // Skip header row ("Username,TimeMillis") if it exists

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        String username = parts[0].trim();
                        long timeMillis = Long.parseLong(parts[1].trim());
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

        // Sort the leaderboard: Lowest time is best (ascending order)
        // TODO for groupmates: If higher time should be better, reverse the comparison logic here.
        Collections.sort(leaderboardData, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry s1, ScoreEntry s2) {
                // Using Long.compare for robust comparison of long values
                return Long.compare(s1.getTimeMillis(), s2.getTimeMillis());
            }
        });
        
        // Optional: Limit the number of entries shown (e.g., top 10)
        // TODO for groupmates: Uncomment and adjust the limit if you only want to show a specific number of top scores.
        // if (leaderboardData.size() > 10) {
        //     leaderboardData = leaderboardData.subList(0, 10);
        // }
    }

    // Helper to format milliseconds into a more readable MM:SS:ms string
    // TODO for groupmates: Adjust formatting if a different time display is preferred.
    private String formatTime(long timeMillis) {
        long minutes = (timeMillis / 1000) / 60;
        long seconds = (timeMillis / 1000) % 60;
        long milliseconds = timeMillis % 1000;
        return String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
    }

    // --- Statemethods Implementations ---

    @Override
    public void update() {
        // Not much to update on a static leaderboard, unless you add animations or dynamic elements.
        // TODO for groupmates: Add any update logic if the leaderboard needs animations or real-time changes.
    }

    @Override
    public void draw(Graphics g) {
        // Draw a semi-transparent black background
        g.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black (180 out of 255 alpha)
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        // Draw Title
        g.setColor(Color.WHITE);
        // Font size scaled by Game.SCALE for consistency with other UI elements
        g.setFont(new Font("Arial", Font.BOLD, (int)(48 * Game.SCALE)));
        String title = "Leaderboard";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        // Center the title horizontally, positioned slightly above the vertical center
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int)(Game.GAME_HEIGHT / 2 - 170 * Game.SCALE));

        // Draw Leaderboard Headers (Rank, Username, Time)
        g.setFont(new Font("Arial", Font.BOLD, (int)(28 * Game.SCALE)));
        // Adjust these X coordinates to control the spacing and alignment of columns
        int entryX = (int)(Game.GAME_WIDTH / 2 - 150 * Game.SCALE); // Starting X for Username column
        int entryY = (int)(Game.GAME_HEIGHT / 2 - 120 * Game.SCALE); // Starting Y for header line
        int lineHeight = (int)( 35 * Game.SCALE); // Vertical spacing between lines/entries

        //Header
        g.setColor(Color.YELLOW); // Header text color
        g.drawString("", entryX - (int)(50 * Game.SCALE), entryY); // Position for Rank
        g.drawString("Username", entryX, entryY); // Position for Username
        g.drawString("Time", entryX + (int)(200 * Game.SCALE), entryY); // Position for Time

        // Draw Leaderboard Entries
        g.setFont(new Font("Arial", Font.PLAIN, (int)(24 * Game.SCALE))); // Font for score entries
        g.setColor(Color.WHITE); // Color for score entry text
        int currentY = entryY + lineHeight; // Start drawing entries below the header line

        for (int i = 0; i < leaderboardData.size(); i++) {
            ScoreEntry entry = leaderboardData.get(i);
            String rank = (i + 1) + "."; // Rank starts from 1
            String username = entry.getUsername();
            String formattedTime = formatTime(entry.getTimeMillis());

            g.drawString(rank, entryX - (int)(50 * Game.SCALE), currentY);
            g.drawString(username, entryX, currentY);
            g.drawString(formattedTime, entryX + (int)(200 * Game.SCALE), currentY);

            currentY += lineHeight; // Move to the next line for the next entry
            
            // TODO for groupmates: Adjust or remove this check if you need to display more or fewer entries.
            // Stop drawing if we go off-screen to prevent text from drawing outside bounds.
            if (currentY > Game.GAME_HEIGHT - (int)(50 * Game.SCALE)) {
                break; 
            }
        }
        
        // Draw Back Button
        if (backButtonImage != null) {
            // TODO for groupmates: This will draw your actual image if loaded above.
            g.drawImage(backButtonImage, backButtonX, backButtonY, backButtonWidth, backButtonHeight, null);
        } else { 
            // Fallback drawing if backButtonImage is null (due to placeholder setup)
            // TODO for groupmates: Customize these placeholder colors/shape if desired.
            g.setColor(Color.RED); // Draw a red rectangle for the button body
            g.fillRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
            g.setColor(Color.BLACK); // Draw a black border around the button
            g.drawRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        }
        g.setColor(Color.WHITE); // Color for "Back" text
        g.setFont(new Font("Arial", Font.BOLD, (int)(16 * Game.SCALE))); // Font for "Back" text
        String backText = "Back";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        // Center "Back" text within the button
        g.drawString(backText, backButtonX + backButtonWidth / 2 - backTextWidth / 2, backButtonY + (int)(backButtonHeight * 0.7));
    }

    // --- Input Handling Methods ---
    // These methods respond to mouse and keyboard input on the Leaderboard screen.

    @Override
    public void mousePressed(MouseEvent e) {
        // Check if the back button area was clicked
        if (e.getX() >= backButtonX && e.getX() <= backButtonX + backButtonWidth &&
            e.getY() >= backButtonY && e.getY() <= backButtonY + backButtonHeight) {
            Gamestate.state = Gamestate.MENU; // Change game state back to the main menu
            // Reload data here so that the leaderboard is fresh if new scores were added
            // before the next time it's opened.
            loadLeaderboardData(); 
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) { 
        // TODO for groupmates: Add any specific logic needed when mouse button is released over elements.
    }

    @Override
    public void mouseMoved(MouseEvent e) { 
        // TODO for groupmates: Add hover effects or other mouse movement logic if desired.
    }

    @Override
    public void mouseClicked(MouseEvent e) { 
        // TODO for groupmates: Add any specific click logic (different from pressed/released).
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Handle ESC key to go back to the menu
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
            loadLeaderboardData(); // Reload data
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { 
        // TODO for groupmates: Add any specific key release logic if needed.
    }

    @Override
    public void keyTyped(KeyEvent e) { 
        // TODO for groupmates: Add any specific key typed logic (e.g., text input, but unlikely for a leaderboard).
    }
}