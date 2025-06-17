package utilz;

import entities.PlayerCharacter;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import mainn.Game; // Import Game to use Game.SCALE
import static utilz.Constants.UI.*;

import static utilz.Constants.PlayerConstants.*; // Import PlayerConstants for action integers

public class LoadSave {

    // frog stuff
    public static final String FROG_IDLE = "/res/Frog_Idle.png";
    public static final String FROG_RUN = "/res/Frog_Run.png";
    public static final String FROG_JUMP = "/res/Frog_Jump.png";
    public static final String FROG_FALL = "/res/Frog_Fall.png";
    public static final String FROG_HIT = "/res/Frog_Hit.png";
    // public static final String FROG_DEAD = "/res/Frog_Dead.png";


    public static final String LEVEL_ATLAS = "/res/outside_sprites.png";
    public static final String MENU_BUTTONS = "/res/buttonsA.png";
    public static final String MENU_BACKGROUND = "/res/menu_background.png";
    public static final String PAUSE_BACKGROUND = "/res/pause_menu.png";
    public static final String SOUND_BUTTONS = "/res/sound_button.png";
    public static final String URM_BUTTONS = "/res/urm_buttons.png";
    public static final String VOLUME_BUTTONS = "/res/volume_buttons.png";
    public static final String MENU_BACKGROUND_IMG = "/res/background_menu.png";
    public static final String PLAYING_BG_IMG = "/res/playing_bg_img.png";
    public static final String BIG_CLOUDS = "/res/big_clouds.png";
    public static final String SMALL_CLOUDS = "/res/small_clouds.png";
    public static final String CRABBY_SPRITE = "/res/crabby_sprite.png";
    public static final String COMPLETED_IMG = "/res/completed_sprite.png";
    public static final String POTION_ATLAS = "/res/potions_sprites.png";
    public static final String CONTAINER_ATLAS = "/res/objects_sprites.png";
    public static final String TRAP_ATLAS = "/res/trap_atlas.png";
    public static final String CANNON_ATLAS = "/res/cannon_atlas.png";
    public static final String CANNON_BALL = "/res/ball.png";
    public static final String DEATH_SCREEN = "/res/death_screen.png";
    public static final String OPTIONS_MENU = "/res/options_background.png";
    public static final String PINKSTAR_ATLAS = "/res/pinkstar_atlas.png";
    public static final String QUESTION_ATLAS = "/res/question_atlas.png";
    public static final String EXCLAMATION_ATLAS = "/res/exclamation_atlas.png";
    public static final String SHARK_ATLAS = "/res/shark_atlas.png";
    public static final String CREDITS = "/res/credits_list.png";
    public static final String GRASS_ATLAS = "/res/grass_atlas.png";
    public static final String TREE_ONE_ATLAS = "/res/tree_one_atlas.png";
    public static final String TREE_TWO_ATLAS = "/res/tree_two_atlas.png";
    public static final String GAME_COMPLETED = "/res/game_completed.png";
    public static final String RAIN_PARTICLE = "/res/rain_particle.png";
    public static final String WATER_TOP = "/res/water_atlas_animation.png";
    public static final String WATER_BOTTOM = "/res/water.png";
    public static final String SHIP = "/res/ship.png";
    public static final String REGISTER_INPUT_BG = "/res/register_input_bg.png";
    public static final String LIFE_ICON = "/res/heart_icon.png";
    public static final String LEVEL_PATH_PREFIX = "/res/lvls/";
    public static final String CUSTOM_FONT_JERSEY = "/fonts/Jersey15-Regular.ttf";
    public static final String CUSTOM_FONT_ARCADE_CLASSIC = "/fonts/arcadeclassic.ttf";
    public static final String STAR_ANIMATION = "/res/star.png";
    public static final String REGISTER_MAIN_BACKGROUND = "/res/register_background.png";

    
    public static final String REGISTER_SUBMIT_BUTTON = "/res/submit_button.png";
    public static final String REGISTER_BACK_BUTTON = "/res/back_button.png";
    public static final String REGISTER_SUBMIT_BUTTON_PRESSED = "/res/submit_pressed.png"; // Path to your pressed submit button image
    public static final String REGISTER_BACK_BUTTON_PRESSED = "/res/back_pressed.png";

    public static BufferedImage[][] loadAnimations(PlayerCharacter pc) {
        int numPlayerActions = DEAD + 1; // This will be 7 if DEAD is 6.
        BufferedImage[][] animations = new BufferedImage[numPlayerActions][];

        switch (pc) {

            case FROG:
                int frogSpriteW = pc.spriteW;
                int frogSpriteH = pc.spriteH;
                animations[IDLE] = cutSheet(GetSpriteAtlas(FROG_IDLE), pc.getSpriteAmount(IDLE), frogSpriteW, frogSpriteH);

                animations[RUNNING] = cutSheet(GetSpriteAtlas(FROG_RUN), pc.getSpriteAmount(RUNNING), frogSpriteW, frogSpriteH);
                animations[JUMP] = cutSheet(GetSpriteAtlas(FROG_JUMP), pc.getSpriteAmount(JUMP), frogSpriteW, frogSpriteH);
                animations[FALLING] = cutSheet(GetSpriteAtlas(FROG_FALL), pc.getSpriteAmount(FALLING), frogSpriteW, frogSpriteH);
                animations[HIT] = cutSheet(GetSpriteAtlas(FROG_HIT), pc.getSpriteAmount(HIT), frogSpriteW, frogSpriteH);
                animations[DEAD] = cutSheet(GetSpriteAtlas(FROG_IDLE), pc.getSpriteAmount(DEAD), frogSpriteW, frogSpriteH);

                break;

            default:
                System.err.println("Unknown PlayerCharacter: " + pc);
                return new BufferedImage[0][0];
        }
        return animations;
    }

    // Helper method to cut a single-row spritesheet (or single image)
    private static BufferedImage[] cutSheet(BufferedImage sheet, int numFrames, int spriteWidth, int spriteHeight) {
        if (sheet == null) {
            System.err.println("Error: Sprite sheet is null. Cannot cut frames. Expected " + numFrames + " frames of " + spriteWidth + "x" + spriteHeight + ".");
            return new BufferedImage[0];
        }
        BufferedImage[] frames = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            try {
                frames[i] = sheet.getSubimage(i * spriteWidth, 0, spriteWidth, spriteHeight);
            } catch (java.awt.image.RasterFormatException e) {
                System.err.println("Error cutting subimage from sheet. Check spriteWidth/spriteHeight or numFrames against actual image dimensions. Sheet: " + sheet.getWidth() + "x" + sheet.getHeight() + ", trying to cut frame " + i + " at " + (i * spriteWidth) + ", 0 with size " + spriteWidth + "x" + spriteHeight);
                e.printStackTrace();
                return new BufferedImage[0];
            }
        }
        return frames;
    }


    public static BufferedImage GetSpriteAtlas(String fileName) {
        BufferedImage img = null;
        InputStream is = LoadSave.class.getResourceAsStream(fileName);

        if (is == null) {
            System.err.println("Could not find resource: " + fileName);
            return null;
        }

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading image: " + fileName);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return img;
    }
    
    public static BufferedImage[] GetHeartAnimationSprites() {
        BufferedImage heartSheet = GetSpriteAtlas(HEART_SPRITE_SHEET); // Uses HEART_SPRITE_SHEET from Constants.UI
        BufferedImage[] heartFrames = new BufferedImage[HEART_SPRITE_FRAMES];

        if (heartSheet == null) {
            System.err.println("Failed to load heart sprite sheet: " + HEART_SPRITE_SHEET + ". Check path and file existence!");
            return heartFrames; // Return an empty array if loading fails
        }

        for (int i = 0; i < HEART_SPRITE_FRAMES; i++) {
            heartFrames[i] = heartSheet.getSubimage(
                    i * HEART_SPRITE_WIDTH, 
                    0, 
                    HEART_SPRITE_WIDTH,
                    HEART_SPRITE_HEIGHT 
            );
        }
        return heartFrames;
    }

    public static Font GetFont(String fileName) {
        InputStream is = LoadSave.class.getResourceAsStream(fileName);

        if (is == null) {
            System.err.println("Could not find font resource: " + fileName);
            return null;
        }

        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load font: " + fileName);
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static BufferedImage[] GetAllLevels() {
        ArrayList<BufferedImage> levelImages = new ArrayList<>();
        int levelNum = 1;
        while (true) {
            String levelFileName = LEVEL_PATH_PREFIX + levelNum + ".png";
            InputStream is = LoadSave.class.getResourceAsStream(levelFileName);

            if (is == null) {
                if (levelNum == 1 && levelImages.isEmpty()) {
                    System.err.println("Could not find any level files. Checked path: " + levelFileName);
                }
                break;
            }

            try {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    levelImages.add(img);
                } else {
                    System.err.println("Failed to read image from stream for level: " + levelFileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading level image: " + levelFileName);
                break;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            levelNum++;
        }
        return levelImages.toArray(new BufferedImage[0]);
    }
}