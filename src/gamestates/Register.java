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
import java.awt.FontFormatException;
import java.io.InputStream;


import mainn.Game; // Assuming mainn is your base package for Game

public class Register extends State implements Statemethods {

    private Font customInputFont;
    private Font titleFont;
    
    // --- Game Data ---
    private long finalGameTimeMillis = 0;

    // --- UI Elements for Input ---
    private StringBuilder usernameInput = new StringBuilder();
    private boolean inputActive = true;
    private int maxUsernameLength = 13;
    private boolean shouldTransitionToMenu = false;

    // --- UI Element Bounds (positions and sizes) ---
    private int inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight;
    private int submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight;
    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight;

    // --- Image Loading ---
    private BufferedImage inputBoxImage;
    private BufferedImage submitButtonNormalImage; // Renamed for clarity
    private BufferedImage submitButtonPressedImage; // NEW: Image for when submit button is pressed
    private BufferedImage backButtonNormalImage;   // Renamed for clarity
    private BufferedImage backButtonPressedImage;   // NEW: Image for when back button is pressed
    private BufferedImage registerBackgroundImg;
    
    // --- Button State Tracking --- // NEW: Flags to track if a button is currently being held down
    private boolean isSubmitPressed = false;
    private boolean isBackPressed = false;

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
        loadFonts();
        initUIBounds();
    }
    
    private void loadFonts() {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/dogicapixel.ttf");
            if (is != null) {
                customInputFont = Font.createFont(Font.TRUETYPE_FONT, is);
                customInputFont = customInputFont.deriveFont(Font.PLAIN, (float) (16 * Game.SCALE)); // Use float for deriveFont size

                is.close(); // Close the input stream
            } else {
                System.err.println("Font file 'dogicapixel.ttf' not found. Make sure it's in your resources folder.");
                customInputFont = new Font("Arial", Font.PLAIN, (int) (24 * Game.SCALE));
            }
            
            InputStream isTitle = getClass().getResourceAsStream("/fonts/Jersey15-Regular.ttf"); // Adjust path if necessary
            if (isTitle != null) {
                titleFont = Font.createFont(Font.TRUETYPE_FONT, isTitle);
                // Derive the font to the desired size and style for the title (e.g., BOLD, 48 * Game.SCALE)
                titleFont = titleFont.deriveFont(Font.BOLD, (float) (36 * Game.SCALE));
                isTitle.close();
            } else {
                System.err.println("Font file 'Jersey15-Regular.ttf' not found. Using default font for title.");
                titleFont = new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE)); // Fallback
            }
            
        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading font 'dogicapixel.ttf': " + e.getMessage());
            e.printStackTrace();
            customInputFont = new Font("Arial", Font.PLAIN, (int) (24 * Game.SCALE));
        }
    }

    private void loadImages() {
        inputBoxImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_INPUT_BG);
        // Load normal state images
        submitButtonNormalImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_SUBMIT_BUTTON); // Original button image
        backButtonNormalImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON);   // Original button image

        // NEW: Load pressed state images (You'll need to create these image files and LoadSave constants)
        submitButtonPressedImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_SUBMIT_BUTTON_PRESSED);
        backButtonPressedImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON_PRESSED);
        registerBackgroundImg = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_MAIN_BACKGROUND);
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
        submitButtonHeight = (int)(submitButtonWidth / 2);
        submitButtonX = Game.GAME_WIDTH / 2 - submitButtonWidth / 2;
        submitButtonY = (int)(inputBoxY + inputBoxHeight + 30 * Game.SCALE);

        // Adjust for a back button (optional, but good for navigation)
        backButtonWidth = (int)(100 * Game.SCALE);
        backButtonHeight = (int)(backButtonWidth / 2);
        backButtonX = (int)(20 * Game.SCALE);
        backButtonY = (int)(20 * Game.SCALE);
    }

    @Override
    public void update() {
        // Hide feedback message after its duration
        if (!feedbackMessage.isEmpty() && System.currentTimeMillis() > messageDisplayEndTime) {
            feedbackMessage = "";
        }
        
        // Check if we should transition to the menu (only after feedback message has faded)
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
        if (registerBackgroundImg != null) {
            float stretchFactorX = 1.2f; // Make it 20% wider than the screen
            float stretchFactorY = 1.55f; // Make it 20% taller than the screen

            // Calculate the new stretched dimensions
            int stretchedWidth = (int) (Game.GAME_WIDTH * stretchFactorX);
            int stretchedHeight = (int) (Game.GAME_HEIGHT * stretchFactorY);

            // Calculate offsets to center the stretched image on the screen.
            // This ensures the middle of your background image aligns with the middle of your game screen.
            int offsetX = (stretchedWidth - Game.GAME_WIDTH) / 2;
            int offsetY = (stretchedHeight - Game.GAME_HEIGHT) / 2;

            // Draw the image. The negative offsets (-offsetX, -offsetY) shift the
            // larger image so its center aligns with the screen's center.
            g.drawImage(registerBackgroundImg, -offsetX, -offsetY, stretchedWidth, stretchedHeight, null);

        } else {
            // Fallback: If image fails to load, draw the original semi-transparent black
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        // --- Draw Title ---
        g.setColor(Color.WHITE);
        // Assuming Game has a getGameFont() or you use a default font
        g.setFont(titleFont);
        String title = "Enter Username";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int) (Game.GAME_HEIGHT / 2 - 150 * Game.SCALE));

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
        // *** MODIFIED: Using the customInputFont for the username text ***
        g.setFont(customInputFont); // Assumes customInputFont is loaded in loadFonts()
        // Adjust Y position to center text vertically in the box
        g.drawString(usernameInput.toString(), inputBoxX + (int) (5 * Game.SCALE), inputBoxY + (int) (inputBoxHeight * 0.7));

        // Draw typing cursor if input field is active
        if (inputActive && System.currentTimeMillis() % 1000 < 500) { // Blinking cursor
            int textWidth = g.getFontMetrics().stringWidth(usernameInput.toString());
            // Position cursor after the text, inside the box
            g.fillRect(inputBoxX + (int) (5 * Game.SCALE) + textWidth, inputBoxY + (int) (inputBoxHeight * 0.25), (int) (2 * Game.SCALE), (int) (inputBoxHeight * 0.5));
        }

        // --- Draw Submit Button (with animation logic) ---
        BufferedImage submitImgToDraw = isSubmitPressed ? submitButtonPressedImage : submitButtonNormalImage;
        if (submitImgToDraw != null) {
            g.drawImage(submitImgToDraw, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight, null);
        } else { // Fallback if image doesn't load, dynamically change color
            g.setColor(isSubmitPressed ? Color.DARK_GRAY : Color.GREEN); // Pressed state is darker
            g.fillRect(submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight);
            g.setColor(Color.BLACK);
            g.drawRect(submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight);
        }

        BufferedImage backImgToDraw = isBackPressed ? backButtonPressedImage : backButtonNormalImage;
        if (backImgToDraw != null) {
            g.drawImage(backImgToDraw, backButtonX, backButtonY, backButtonWidth, backButtonHeight, null);
        } else { // Fallback if image doesn't load, dynamically change color
            g.setColor(isBackPressed ? Color.DARK_GRAY : Color.RED); // Pressed state is darker
            g.fillRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
            g.setColor(Color.BLACK);
            g.drawRect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        }

        // --- Draw Feedback Message ---
        if (!feedbackMessage.isEmpty()) {
            g.setColor(Color.YELLOW); 
            g.setFont(titleFont.deriveFont(Font.BOLD, (float) (20 * Game.SCALE)));            int msgWidth = g.getFontMetrics().stringWidth(feedbackMessage);
            g.drawString(feedbackMessage, Game.GAME_WIDTH / 2 - msgWidth / 2, (int) (submitButtonY + submitButtonHeight + 40 * Game.SCALE));
        }
    }

    // --- Input Handling ---

    @Override
    public void mousePressed(MouseEvent e) {
        // Check if submit button was clicked to set its pressed state
        if (isInBounds(e, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight)) {
            isSubmitPressed = true;
        }
        // Check if back button was clicked to set its pressed state
        else if (isInBounds(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
            isBackPressed = true;
        }
        // If clicking outside buttons, but inside input box, activate input
        else if (isInBounds(e, inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight)) {
            inputActive = true;
        } else {
            // If clicked anywhere else, deactivate input
            inputActive = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // If submit button was pressed and released over it
        if (isSubmitPressed) {
            if (isInBounds(e, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight)) {
                // Action: Submit
                handleSubmit();
            }
        }
        // If back button was pressed and released over it
        if (isBackPressed) {
            if (isInBounds(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
                // Action: Go back to main menu
                Gamestate.state = Gamestate.MENU;
                inputActive = false; // Deactivate input field
                usernameInput.setLength(0); // Clear username input
                feedbackMessage = ""; // Clear any messages
            }
        }
        // ALWAYS reset pressed states after mouse release, regardless of whether an action was triggered
        isSubmitPressed = false;
        isBackPressed = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) { } // Keep this as is if you don't use it

    @Override
    public void mouseClicked(MouseEvent e) {
        // You can leave this empty, as press/release handles the primary button interaction
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
                    displayFeedback("Max length reached!", Color.RED); // Use helper for consistency
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

    // --- NEW: Helper method to check if mouse event is within given bounds ---
    private boolean isInBounds(MouseEvent e, int x, int y, int width, int height) {
        return e.getX() >= x && e.getX() <= x + width &&
               e.getY() >= y && e.getY() <= y + height;
    }
}