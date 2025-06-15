package gamestates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.image.BufferedImage; 
import utilz.LoadSave;   


import mainn.Game; // Assuming mainn is your base package for Game

public class Register extends State implements Statemethods {

    // --- Game Data ---
    private long finalGameTimeMillis = 0; // Stores the final time to be saved

    // --- UI Elements for Input ---
    private StringBuilder usernameInput = new StringBuilder();
    private boolean inputActive = true; 
    private int maxUsernameLength = 30; 
    private boolean shouldTransitionToMenu = false;

    // --- UI Element Bounds (positions and sizes) ---
    private int inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight;
    private int submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight;
    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight; // New back button
    
    // --- Image Loading ---
    private BufferedImage inputBoxImage;
    private BufferedImage submitButtonImage;
    private BufferedImage backButtonImage;
    
    // --- Feedback Message for User ---
    private String feedbackMessage = ""; // Displays messages like "Saving...", "Success!", "Error!"
    private long messageDisplayEndTime = 0; // Time when message should disappear
    private final long MESSAGE_DURATION = 3000; // Display message for 3 seconds

    // --- CSV File Configuration ---
    private static final String CSV_FILE_NAME = "user_scores.csv";
    private static final String CSV_HEADER = "Username,TimeMillis\n"; // CSV header row

    public Register(Game game) {
        super(game);
        loadImages();
        initUIBounds();
    }

    private void loadImages() {
    inputBoxImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_INPUT_BG); // Example constant
    submitButtonImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_SUBMIT_BUTTON); // Example constant
    backButtonImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON); // Example constant
    }
    
    // Initialize the bounds for all UI elements
    private void initUIBounds() {
        // Adjust these values based on your screen size (Game.GAME_WIDTH, Game.GAME_HEIGHT)
        // and desired layout for the input box
        inputBoxWidth = (int)(250 * Game.SCALE);
        inputBoxHeight = (int)(40 * Game.SCALE);
        inputBoxX = Game.GAME_WIDTH / 2 - inputBoxWidth / 2;
        inputBoxY = (int)(Game.GAME_HEIGHT / 2 - 50 * Game.SCALE);

        // Adjust for the submit button
        submitButtonWidth = (int)(120 * Game.SCALE);
        submitButtonHeight = (int)(40 * Game.SCALE);
        submitButtonX = Game.GAME_WIDTH / 2 - submitButtonWidth / 2;
        submitButtonY = (int)(inputBoxY + inputBoxHeight + 30 * Game.SCALE);

        // Adjust for a back button (optional, but good for navigation)
        backButtonWidth = (int)(100 * Game.SCALE);
        backButtonHeight = (int)(30 * Game.SCALE);
        backButtonX = (int)(20 * Game.SCALE); // Top-left corner
        backButtonY = (int)(20 * Game.SCALE);
    }

    @Override
    public void update() {
        // Hide feedback message after its duration
        if (!feedbackMessage.isEmpty() && System.currentTimeMillis() > messageDisplayEndTime) {
            feedbackMessage = "";
        }

        // ADD THIS NEW LOGIC: Check if we should transition to the menu
        // This will happen only after the feedback message has faded out (feedbackMessage.isEmpty())
        if (shouldTransitionToMenu && feedbackMessage.isEmpty()) {
            Gamestate.state = Gamestate.MENU; // Change state to main menu
            shouldTransitionToMenu = false; // Reset the flag
            
            // It's good practice to reset the input field and active status
            usernameInput.setLength(0); // Clear the username for next time
            inputActive = false; // Deactivate the input box
            feedbackMessage = ""; // Ensure message is clear
        }
    }

    @Override
    public void draw(Graphics g) {
        // Draw a semi-transparent black background over the whole screen
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        // --- Draw Title ---
        g.setColor(Color.WHITE);
        // Assuming Game has a getGameFont() or you use a default font
        g.setFont(new Font("Arial", Font.BOLD, (int)(48 * Game.SCALE))); 
        String title = "Enter Username";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int)(Game.GAME_HEIGHT / 2 - 150 * Game.SCALE));

        // --- Draw Username Input Box ---
        if (inputBoxImage != null) {
    g.drawImage(inputBoxImage, inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight, null);
        } else { // Fallback if image doesn't load
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight);
            g.setColor(Color.BLACK);
            g.drawRect(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight);
        }

        // Draw the current username text typed by the user
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, (int)(24 * Game.SCALE))); // Font for input text
        // Adjust Y position to center text vertically in the box
        g.drawString(usernameInput.toString(), inputBoxX + (int)(5 * Game.SCALE), inputBoxY + (int)(inputBoxHeight * 0.7));

        // Draw typing cursor if input field is active
        if (inputActive && System.currentTimeMillis() % 1000 < 500) { // Blinking cursor
            int textWidth = g.getFontMetrics().stringWidth(usernameInput.toString());
            // Position cursor after the text, inside the box
            g.fillRect(inputBoxX + (int)(5 * Game.SCALE) + textWidth, inputBoxY + (int)(inputBoxHeight * 0.25), (int)(2 * Game.SCALE), (int)(inputBoxHeight * 0.5));
        }

        // --- Draw Submit Button ---
        if (submitButtonImage != null) {
        g.drawImage(submitButtonImage, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight, null);
            } else { // Fallback
                g.setColor(Color.GREEN);
                g.fillRect(submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight);
                g.setColor(Color.BLACK);
                g.drawRect(submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight);
            }
        
        // Draw submit button text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(20 * Game.SCALE))); // Font for button text
        String submitText = "Enter";
        int submitTextWidth = g.getFontMetrics().stringWidth(submitText);
        g.drawString(submitText, submitButtonX + submitButtonWidth / 2 - submitTextWidth / 2, submitButtonY + (int)(submitButtonHeight * 0.7));

        // --- Draw Back Button ---
        if (backButtonImage != null) {
            g.drawImage(backButtonImage, backButtonX, backButtonY, backButtonWidth, backButtonHeight, null);
        } else { // Fallback
            g.setColor(Color.RED);
            g.fillRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
            g.setColor(Color.BLACK);
            g.drawRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
}
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(16 * Game.SCALE)));
        String backText = "Return";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        g.drawString(backText, backButtonX + backButtonWidth / 2 - backTextWidth / 2, backButtonY + (int)(backButtonHeight * 0.7));

        // --- Draw Feedback Message ---
        if (!feedbackMessage.isEmpty()) {
            g.setColor(Color.YELLOW); // Or red for error, green for success
            g.setFont(new Font("Arial", Font.BOLD, (int)(20 * Game.SCALE)));
            int msgWidth = g.getFontMetrics().stringWidth(feedbackMessage);
            g.drawString(feedbackMessage, Game.GAME_WIDTH / 2 - msgWidth / 2, (int)(submitButtonY + submitButtonHeight + 40 * Game.SCALE));
        }
    }

    // --- Input Handling ---

    @Override
    public void mousePressed(MouseEvent e) {
        // Check if input box was clicked
        if (e.getX() >= backButtonX && e.getX() <= backButtonX + backButtonWidth &&
        e.getY() >= backButtonY && e.getY() <= backButtonY + backButtonHeight) {
        
        Gamestate.state = Gamestate.MENU;
        
        inputActive = false;
        usernameInput.setLength(0);
        feedbackMessage = "";
        
    }
        
        
        
        // Check if submit button was clicked
        if (e.getX() >= submitButtonX && e.getX() <= submitButtonX + submitButtonWidth &&
            e.getY() >= submitButtonY && e.getY() <= submitButtonY + submitButtonHeight) {
            handleSubmit();
        }   

        // Check if back button was clicked
        if (e.getX() >= backButtonX && e.getX() <= backButtonX + backButtonWidth &&
            e.getY() >= backButtonY && e.getY() <= backButtonY + backButtonHeight) {
            Gamestate.state = Gamestate.MENU; // Go back to main menu
            inputActive = false; // Deactivate input field
            usernameInput.setLength(0); // Clear username input
            feedbackMessage = ""; // Clear any messages
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) { } // Keep this as is if you don't use it

    @Override
    public void mouseMoved(MouseEvent e) { } // Keep this as is if you don't use it

    @Override
    public void mouseClicked(MouseEvent e) { // <--- ADD THIS METHOD
        // You can leave this empty if you don't need specific logic for mouse clicks
        // (as opposed to press/release)
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // Keep this for non-character keys like ENTER, BACKSPACE, ESCAPE
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                // Simulate submit button click or process input
                handleSubmit();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (inputActive && usernameInput.length() > 0) {
                    usernameInput.deleteCharAt(usernameInput.length() - 1);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                // For instance, if you want ESC to go back to main menu
                Gamestate.state = Gamestate.MENU;
                inputActive = false; // Deactivate input
                usernameInput.setLength(0); // Clear input
                feedbackMessage = ""; // Clear message
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // You might not need specific logic here unless for release events
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // DEBUG: Check if method is called and inputActive state
        System.out.println("keyTyped received: '" + e.getKeyChar() + "', inputActive: " + inputActive); 

        if (inputActive) {
            char typedChar = e.getKeyChar();
            // Basic validation: allow letters, numbers, and common symbols, but not control characters
            if (Character.isLetterOrDigit(typedChar) || "._- ".indexOf(typedChar) != -1) {
                if (usernameInput.length() < maxUsernameLength) {
                    usernameInput.append(typedChar);
                } else {
                    feedbackMessage = "Max length reached!";
                }
            }
        }
    }

    // --- Game Time Setter ---
    // This method will be called by Playing.java when the game is completed
    public void setFinalGameTime(long timeMillis) {
        this.finalGameTimeMillis = timeMillis;
    }

    // --- CSV File Handling Methods ---

    // Handles the submission logic
    private void handleSubmit() {
        String username = usernameInput.toString().trim(); // Trim whitespace

        if (username.isEmpty()) {
            displayFeedback("Username cannot be empty!", Color.RED);
            return;
        }

        // Attempt to save the data to CSV
        if (saveToCSV(username, finalGameTimeMillis)) {
            displayFeedback("Username registered successfully!", Color.GREEN);
            // ADD THIS LINE: Set the flag to true on success
            shouldTransitionToMenu = true; 
        } else {
            displayFeedback("Failed to save Username. Try again.", Color.RED);
        }
    }

    // Helper to display messages to the user
    private void displayFeedback(String message, Color color) {
        this.feedbackMessage = message;
        this.messageDisplayEndTime = System.currentTimeMillis() + MESSAGE_DURATION;
        // Optionally store the color too, if you want different colors for messages
    }


    // Saves the username and time to the CSV file
    private boolean saveToCSV(String username, long timeMillis) {
        File csvFile = new File(CSV_FILE_NAME);
        boolean fileExists = csvFile.exists();

        try (FileWriter fw = new FileWriter(csvFile, true); // true means append mode
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            // Add header only if the file is new and doesn't exist
            if (!fileExists) {
                out.print(CSV_HEADER);
            }

            // Write the new score
            out.printf("%s,%d\n", username, timeMillis);
            return true; // Success
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            e.printStackTrace();
            return false; // Failure
        }
    }

    // Checks if a username already exists in the CSV file
    private boolean usernameExists(String username) {
        File csvFile = new File(CSV_FILE_NAME);
        if (!csvFile.exists()) {
            return false; // File doesn't exist, so username can't exist
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip header
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                // Split the line by comma
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].trim().equalsIgnoreCase(username.trim())) {
                    return true; // Username found (case-insensitive check)
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file to check username: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Username not found or error occurred
    }
}