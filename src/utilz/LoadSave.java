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

public class LoadSave {

    // --- Image Path Constants ---
    public static final String PLAYER_PIRATE = "/res/player_sprites.png";
    public static final String PLAYER_ORC = "/res/player_orc.png";
    public static final String PLAYER_SOLDIER = "/res/player_soldier.png";
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
    public static final String STATUS_BAR = "/res/health_power_bar.png";
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
    public static final String REGISTER_SUBMIT_BUTTON = "/res/submit_button.png";
    public static final String REGISTER_BACK_BUTTON = "/res/back_button.png";
    public static final String LIFE_ICON = "/res/heart_icon.png";
    public static final String LEVEL_PATH_PREFIX = "/res/lvls/";
    // UPDATED FONT CONSTANT for src/fonts location
    public static final String CUSTOM_FONT_JERSEY = "/fonts/Jersey15-Regular.ttf";


    public static BufferedImage[][] loadAnimations(PlayerCharacter pc) {
        BufferedImage img = GetSpriteAtlas(pc.playerAtlas);
        if (img == null) {
            System.err.println("Failed to load player atlas for: " + pc.playerAtlas);
            return new BufferedImage[0][0];
        }

        BufferedImage[][] animations = new BufferedImage[pc.rowA][pc.colA];
        for (int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * pc.spriteW, j * pc.spriteH, pc.spriteW, pc.spriteH);

        return animations;
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