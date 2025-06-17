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


import mainn.Game;

public class Register extends State implements Statemethods {

    private Font customInputFont;
    private Font titleFont;
    
    // game data
    private long finalGameTimeMillis = 0;

    // ui elements for input
    private StringBuilder usernameInput = new StringBuilder();
    private boolean inputActive = true;
    private int maxUsernameLength = 13;
    private boolean shouldTransitionToMenu = false;

    // ui element bounds
    private int inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight;
    private int submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight;
    private int backButtonX, backButtonY, backButtonWidth, backButtonHeight;

    // image loading
    private BufferedImage inputBoxImage;
    private BufferedImage submitButtonNormalImage;
    private BufferedImage submitButtonPressedImage;
    private BufferedImage backButtonNormalImage;
    private BufferedImage backButtonPressedImage;
    private BufferedImage registerBackgroundImg;
    
    // button state tracking
    private boolean isSubmitPressed = false;
    private boolean isBackPressed = false;

    // feedback message
    private String feedbackMessage = "";
    private long messageDisplayEndTime = 0;
    private final long MESSAGE_DURATION = 3000;

    // csv file configuration
    private static final String CSV_FILE_NAME = "user_scores.csv";
    private static final String CSV_HEADER = "Username,TimeMillis\n";

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
                customInputFont = customInputFont.deriveFont(Font.PLAIN, (float) (16 * Game.SCALE));

                is.close();
            } else {
                System.err.println("font file 'dogicapixel.ttf' not found. make sure it's in your resources folder.");
                customInputFont = new Font("arial", Font.PLAIN, (int) (24 * Game.SCALE));
            }
            
            InputStream isTitle = getClass().getResourceAsStream("/fonts/jersey15-regular.ttf");
            if (isTitle != null) {
                titleFont = Font.createFont(Font.TRUETYPE_FONT, isTitle);
                titleFont = titleFont.deriveFont(Font.BOLD, (float) (36 * Game.SCALE));
                isTitle.close();
            } else {
                System.err.println("font file 'jersey15-regular.ttf' not found. using default font for title.");
                titleFont = new Font("arial", Font.BOLD, (int) (48 * Game.SCALE));
            }
            
        } catch (FontFormatException | IOException e) {
            System.err.println("error loading font 'dogicapixel.ttf': " + e.getMessage());
            e.printStackTrace();
            customInputFont = new Font("arial", Font.PLAIN, (int) (24 * Game.SCALE));
        }
    }

    private void loadImages() {
        inputBoxImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_INPUT_BG);
        submitButtonNormalImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_SUBMIT_BUTTON);
        backButtonNormalImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON);

        submitButtonPressedImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_SUBMIT_BUTTON_PRESSED);
        backButtonPressedImage = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_BACK_BUTTON_PRESSED);
        registerBackgroundImg = LoadSave.GetSpriteAtlas(LoadSave.REGISTER_MAIN_BACKGROUND);
    }

    // initialize the bounds for all ui elements
    private void initUIBounds() {
        inputBoxWidth = (int)(250 * Game.SCALE);
        inputBoxHeight = (int)(40 * Game.SCALE);
        inputBoxX = Game.GAME_WIDTH / 2 - inputBoxWidth / 2;
        inputBoxY = (int)(Game.GAME_HEIGHT / 2 - 50 * Game.SCALE);

        submitButtonWidth = (int)(120 * Game.SCALE);
        submitButtonHeight = (int)(submitButtonWidth / 2);
        submitButtonX = Game.GAME_WIDTH / 2 - submitButtonWidth / 2;
        submitButtonY = (int)(inputBoxY + inputBoxHeight + 30 * Game.SCALE);

        backButtonWidth = (int)(100 * Game.SCALE);
        backButtonHeight = (int)(backButtonWidth / 2);
        backButtonX = (int)(20 * Game.SCALE);
        backButtonY = (int)(20 * Game.SCALE);
    }

    @Override
    public void update() {
        if (!feedbackMessage.isEmpty() && System.currentTimeMillis() > messageDisplayEndTime) {
            feedbackMessage = "";
        }
        
        if (shouldTransitionToMenu && feedbackMessage.isEmpty()) {
            Gamestate.state = Gamestate.MENU;
            shouldTransitionToMenu = false;

            usernameInput.setLength(0);
            inputActive = false;
            feedbackMessage = "";
        }
    }

    @Override
    public void draw(Graphics g) {
        if (registerBackgroundImg != null) {
            float stretchFactorX = 1.2f;
            float stretchFactorY = 1.55f;

            int stretchedWidth = (int) (Game.GAME_WIDTH * stretchFactorX);
            int stretchedHeight = (int) (Game.GAME_HEIGHT * stretchFactorY);

            int offsetX = (stretchedWidth - Game.GAME_WIDTH) / 2;
            int offsetY = (stretchedHeight - Game.GAME_HEIGHT) / 2;

            g.drawImage(registerBackgroundImg, -offsetX, -offsetY, stretchedWidth, stretchedHeight, null);

        } else {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        // draw title
        g.setColor(Color.WHITE);
        g.setFont(titleFont);
        String title = "enter username";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, Game.GAME_WIDTH / 2 - titleWidth / 2, (int) (Game.GAME_HEIGHT / 2 - 150 * Game.SCALE));

        // draw username input box
        if (inputBoxImage != null) {
            g.drawImage(inputBoxImage, inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight, null);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight);
            g.setColor(Color.BLACK);
            g.drawRect(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight);
        }

        g.setColor(Color.DARK_GRAY);
        g.setFont(customInputFont);
        g.drawString(usernameInput.toString(), inputBoxX + (int) (5 * Game.SCALE), inputBoxY + (int) (inputBoxHeight * 0.7));

        if (inputActive && System.currentTimeMillis() % 1000 < 500) {
            int textWidth = g.getFontMetrics().stringWidth(usernameInput.toString());
            g.fillRect(inputBoxX + (int) (5 * Game.SCALE) + textWidth, inputBoxY + (int) (inputBoxHeight * 0.25), (int) (2 * Game.SCALE), (int) (inputBoxHeight * 0.5));
        }

        // draw submit button
        BufferedImage submitImgToDraw = isSubmitPressed ? submitButtonPressedImage : submitButtonNormalImage;
        if (submitImgToDraw != null) {
            g.drawImage(submitImgToDraw, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight, null);
        } else {
            g.setColor(isSubmitPressed ? Color.DARK_GRAY : Color.GREEN);
            g.fillRect(submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight);
            g.setColor(Color.BLACK);
            g.drawRect(submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight);
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

        // draw feedback message
        if (!feedbackMessage.isEmpty()) {
            g.setColor(Color.YELLOW);
            g.setFont(titleFont.deriveFont(Font.BOLD, (float) (20 * Game.SCALE)));
            int msgWidth = g.getFontMetrics().stringWidth(feedbackMessage);
            g.drawString(feedbackMessage, Game.GAME_WIDTH / 2 - msgWidth / 2, (int) (submitButtonY + submitButtonHeight + 40 * Game.SCALE));
        }
    }

    // input handling
    @Override
    public void mousePressed(MouseEvent e) {
        if (isInBounds(e, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight)) {
            isSubmitPressed = true;
        } else if (isInBounds(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
            isBackPressed = true;
        } else if (isInBounds(e, inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight)) {
            inputActive = true;
        } else {
            inputActive = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isSubmitPressed) {
            if (isInBounds(e, submitButtonX, submitButtonY, submitButtonWidth, submitButtonHeight)) {
                handleSubmit();
            }
        }
        if (isBackPressed) {
            if (isInBounds(e, backButtonX, backButtonY, backButtonWidth, backButtonHeight)) {
                Gamestate.state = Gamestate.MENU;
                inputActive = false;
                usernameInput.setLength(0);
                feedbackMessage = "";
            }
        }
        isSubmitPressed = false;
        isBackPressed = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) { }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                handleSubmit();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (inputActive && usernameInput.length() > 0) {
                    usernameInput.deleteCharAt(usernameInput.length() - 1);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                Gamestate.state = Gamestate.MENU;
                inputActive = false;
                usernameInput.setLength(0);
                feedbackMessage = "";
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("keytyped received: '" + e.getKeyChar() + "', inputactive: " + inputActive);

        if (inputActive) {
            char typedChar = e.getKeyChar();
            if (Character.isLetterOrDigit(typedChar) || "._- ".indexOf(typedChar) != -1) {
                if (usernameInput.length() < maxUsernameLength) {
                    usernameInput.append(typedChar);
                } else {
                    displayFeedback("max length reached!", Color.RED);
                }
            }
        }
    }

    // game time setter
    public void setFinalGameTime(long timeMillis) {
        this.finalGameTimeMillis = timeMillis;
    }

    // csv file handling methods
    private void handleSubmit() {
        String username = usernameInput.toString().trim();

        if (username.isEmpty()) {
            displayFeedback("username cannot be empty!", Color.RED);
            return;
        }

        if (saveToCSV(username, finalGameTimeMillis)) {
            displayFeedback("username registered successfully!", Color.GREEN);
            shouldTransitionToMenu = true;
        } else {
            displayFeedback("failed to save username. try again.", Color.RED);
        }
    }

    private void displayFeedback(String message, Color color) {
        this.feedbackMessage = message;
        this.messageDisplayEndTime = System.currentTimeMillis() + MESSAGE_DURATION;
    }


    private boolean saveToCSV(String username, long timeMillis) {
        File csvFile = new File(CSV_FILE_NAME);
        boolean fileExists = csvFile.exists();

        try (FileWriter fw = new FileWriter(csvFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            if (!fileExists) {
                out.print(CSV_HEADER);
            }

            out.printf("%s,%d\n", username, timeMillis);
            return true;
        } catch (IOException e) {
            System.err.println("error writing to csv file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean usernameExists(String username) {
        File csvFile = new File(CSV_FILE_NAME);
        if (!csvFile.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].trim().equalsIgnoreCase(username.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("error reading csv file to check username: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // helper method: check if mouse event is within given bounds
    private boolean isInBounds(MouseEvent e, int x, int y, int width, int height) {
        return e.getX() >= x && e.getX() <= x + width &&
               e.getY() >= y && e.getY() <= y + height;
    }
}