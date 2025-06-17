package objects;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Enemy;
import entities.Player;
import gamestates.Playing;
import levels.Level;
import mainn.Game;
import utilz.LoadSave;
import static utilz.Constants.ObjectConstants.*;
import static utilz.HelpMethods.CanCannonSeePlayer;
import static utilz.HelpMethods.IsProjectileHittingLevel;
import static utilz.Constants.Projectiles.*;

// NEW Imports for Star specific constants
import static utilz.Constants.ObjectConstants.STAR;
import static utilz.Constants.ObjectConstants.STAR_WIDTH;
import static utilz.Constants.ObjectConstants.STAR_HEIGHT;

public class ObjectManager {

    private Playing playing;
    private BufferedImage[][] potionImgs, containerImgs;
    private BufferedImage[] cannonImgs, grassImgs; // cannonImgs is a 1D array in your code
    private BufferedImage[][] treeImgs;
    private BufferedImage spikeImg, cannonBallImg; // spikeImg is a single image in your code

    // NEW: Star images and list (these ARE new fields, as stars are managed by ObjectManager)
    private BufferedImage[] starImgs;
    private ArrayList<Star> stars;

    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers;
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    private Level currentLevel; // Kept as in your original code

    public ObjectManager(Playing playing) {
        this.playing = playing;
        currentLevel = playing.getLevelManager().getCurrentLevel(); // Kept as in your original code
        loadImgs();
    }

    public void checkSpikesTouched(Player p) {
        for (Spike s : currentLevel.getSpikes()) // Accessing spikes from currentLevel
            if (s.getHitbox().intersects(p.getHitbox()))
                p.kill();
    }

    public void checkSpikesTouched(Enemy e) {
        for (Spike s : currentLevel.getSpikes()) // Accessing spikes from currentLevel
            if (s.getHitbox().intersects(e.getHitbox()))
                e.hurt(200);
    }

    public void checkObjectTouched(Rectangle2D.Float hitbox) {
        for (Potion p : potions)
            if (p.isActive()) {
                if (hitbox.intersects(p.getHitbox())) {
                    p.setActive(false);
                    applyEffectToPlayer(p);
                }
            }
    }

    public void applyEffectToPlayer(Potion p) {
        if (p.getObjType() == RED_POTION)
            playing.getPlayer().addLife(RED_POTION_VALUE);
        else
            playing.getPlayer().changePower(BLUE_POTION_VALUE);
    }

    public void checkObjectHit(Rectangle2D.Float attackbox) {
        for (GameContainer gc : containers)
            if (gc.isActive() && !gc.doAnimation) {
                if (gc.getHitbox().intersects(attackbox)) {
                    gc.setAnimation(true);
                    int type = RED_POTION;
                    if (gc.getObjType() == BARREL)
                        type = BLUE_POTION;
                    potions.add(new Potion((int) (gc.getHitbox().x + gc.getHitbox().width / 2), (int) (gc.getHitbox().y - gc.getHitbox().height / 2), type));
                    return;
                }
            }
    }

    // NEW METHOD: Check for player touching a Star
    public void checkStarTouched(Player p) {
        for (Star s : stars) { // Loop through the 'stars' field, as these are managed locally
            if (s.isActive()) {
                if (s.getHitbox().intersects(p.getHitbox())) {
                    s.setActive(false); // Deactivate the star once touched
                    playing.setLevelCompleted(true); // Trigger level completion
                    return; // Assume only one star is needed to complete the level
                }
            }
        }
    }

    public void loadObjects(Level newLevel) {
        currentLevel = newLevel; // Update currentLevel as in your original code
        potions = new ArrayList<>(newLevel.getPotions()); // Correctly copies the list
        containers = new ArrayList<>(newLevel.getContainers()); // Correctly copies the list

        // NEW: Load Star objects - these *are* managed directly by ObjectManager
        stars = new ArrayList<>(newLevel.getStars());

        projectiles.clear(); // Clear existing projectiles
    }

    private void loadImgs() {
        // --- YOUR ORIGINAL POTION IMAGES LOADING ---
        BufferedImage potionSprite = LoadSave.GetSpriteAtlas(LoadSave.POTION_ATLAS);
        potionImgs = new BufferedImage[2][7];
        for (int j = 0; j < potionImgs.length; j++)
            for (int i = 0; i < potionImgs[j].length; i++)
                potionImgs[j][i] = potionSprite.getSubimage(12 * i, 16 * j, 12, 16);

        // --- YOUR ORIGINAL CONTAINER IMAGES LOADING ---
        BufferedImage containerSprite = LoadSave.GetSpriteAtlas(LoadSave.CONTAINER_ATLAS);
        containerImgs = new BufferedImage[2][8];
        for (int j = 0; j < containerImgs.length; j++)
            for (int i = 0; i < containerImgs[j].length; i++)
                containerImgs[j][i] = containerSprite.getSubimage(40 * i, 30 * j, 40, 30);

        // --- YOUR ORIGINAL SPIKE IMAGE LOADING ---
        spikeImg = LoadSave.GetSpriteAtlas(LoadSave.TRAP_ATLAS);

        // --- YOUR ORIGINAL CANNON IMAGES LOADING ---
        cannonImgs = new BufferedImage[7]; // This is a 1D array as in your code
        BufferedImage tempCannon = LoadSave.GetSpriteAtlas(LoadSave.CANNON_ATLAS);
        for (int i = 0; i < cannonImgs.length; i++)
            cannonImgs[i] = tempCannon.getSubimage(i * 40, 0, 40, 26);

        cannonBallImg = LoadSave.GetSpriteAtlas(LoadSave.CANNON_BALL);

        // --- YOUR ORIGINAL TREE IMAGES LOADING ---
        treeImgs = new BufferedImage[2][4];
        BufferedImage treeOneImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_ONE_ATLAS);
        for (int i = 0; i < 4; i++)
            treeImgs[0][i] = treeOneImg.getSubimage(i * 39, 0, 39, 92);
        BufferedImage treeTwoImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_TWO_ATLAS);
        for (int i = 0; i < 4; i++)
            treeImgs[1][i] = treeTwoImg.getSubimage(i * 62, 0, 62, 54);

        // --- YOUR ORIGINAL GRASS IMAGES LOADING ---
        BufferedImage grassTemp = LoadSave.GetSpriteAtlas(LoadSave.GRASS_ATLAS);
        grassImgs = new BufferedImage[2];
        for (int i = 0; i < grassImgs.length; i++)
            grassImgs[i] = grassTemp.getSubimage(32 * i, 0, 32, 32);

        // --- NEW: Load Star Images ---
        BufferedImage starAtlas = LoadSave.GetSpriteAtlas(LoadSave.STAR_ANIMATION);
        starImgs = new BufferedImage[GetSpriteAmount(STAR)];
        for (int i = 0; i < starImgs.length; i++) {
            // CORRECTED LINE: Use STAR_WIDTH_DEFAULT and STAR_HEIGHT_DEFAULT
            // These are the original, unscaled pixel dimensions of your sprite frames on the atlas.
            starImgs[i] = starAtlas.getSubimage(i * STAR_WIDTH_DEFAULT, 0, STAR_WIDTH_DEFAULT, STAR_HEIGHT_DEFAULT);
        }
        // -----------------------------
    }

    public void update(int[][] lvlData, Player player) {
        updateBackgroundTrees(); // Accessing trees from currentLevel implicitly
        for (Potion p : potions)
            if (p.isActive())
                p.update();

        for (GameContainer gc : containers)
            if (gc.isActive())
                gc.update();

        updateCannons(lvlData, player); // Accessing cannons from currentLevel implicitly
        updateProjectiles(lvlData, player);

        // NEW: Update Stars
        for (Star s : stars) { // Loop through the 'stars' field
            if (s.isActive()) {
                s.update(); // Call Star's update (which calls GameObject's update)
            }
        }

        // NEW: Check for player-star collision after stars have updated
        checkStarTouched(player);
    }

    private void updateBackgroundTrees() {
        for (BackgroundTree bt : currentLevel.getTrees()) // Accessing trees from currentLevel
            bt.update();
    }

    private void updateProjectiles(int[][] lvlData, Player player) {
        for (Projectile p : projectiles)
            if (p.isActive()) {
                p.updatePos();
                if (p.getHitbox().intersects(player.getHitbox())) {
                    player.hit();
                    p.setActive(false);
                } else if (IsProjectileHittingLevel(p, lvlData))
                    p.setActive(false);
            }
    }

    private boolean isPlayerInRange(Cannon c, Player player) {
        int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
        return absValue <= Game.TILES_SIZE * 5;
    }

    private boolean isPlayerInfrontOfCannon(Cannon c, Player player) {
        if (c.getObjType() == CANNON_LEFT) {
            if (c.getHitbox().x > player.getHitbox().x)
                return true;
        } else if (c.getHitbox().x < player.getHitbox().x)
            return true;
        return false;
    }

    private void updateCannons(int[][] lvlData, Player player) {
        for (Cannon c : currentLevel.getCannons()) { // Accessing cannons from currentLevel
            if (!c.doAnimation)
                if (c.getTileY() == player.getTileY())
                    if (isPlayerInRange(c, player))
                        if (isPlayerInfrontOfCannon(c, player))
                            if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY()))
                                c.setAnimation(true);

            c.update();
            if (c.getAniIndex() == 4 && c.getAniTick() == 0)
                shootCannon(c);
        }
    }

    private void shootCannon(Cannon c) {
        int dir = 1;
        if (c.getObjType() == CANNON_LEFT)
            dir = -1;

        projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawPotions(g, xLvlOffset);
        drawContainers(g, xLvlOffset);
        drawTraps(g, xLvlOffset);
        drawCannons(g, xLvlOffset);
        drawProjectiles(g, xLvlOffset);
        drawGrass(g, xLvlOffset);
        drawBackgroundTrees(g, xLvlOffset);

        // NEW: Draw Stars
        drawStars(g, xLvlOffset);
    }

    private void drawGrass(Graphics g, int xLvlOffset) {
        for (Grass grassObj : currentLevel.getGrass()) // Accessing grass from currentLevel
            g.drawImage(grassImgs[grassObj.getType()], grassObj.getX() - xLvlOffset, grassObj.getY(), (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
    }

    public void drawBackgroundTrees(Graphics g, int xLvlOffset) {
        for (BackgroundTree bt : currentLevel.getTrees()) { // Accessing trees from currentLevel
            int type = bt.getType();
            if (type == 9)
                type = 8;
            g.drawImage(treeImgs[type - 7][bt.getAniIndex()], bt.getX() - xLvlOffset + GetTreeOffsetX(bt.getType()), (int) (bt.getY() + GetTreeOffsetY(bt.getType())), GetTreeWidth(bt.getType()),
                GetTreeHeight(bt.getType()), null);
        }
    }

    private void drawProjectiles(Graphics g, int xLvlOffset) {
        for (Projectile p : projectiles)
            if (p.isActive())
                g.drawImage(cannonBallImg, (int) (p.getHitbox().x - xLvlOffset), (int) (p.getHitbox().y), CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT, null);
    }

    private void drawCannons(Graphics g, int xLvlOffset) {
        for (Cannon c : currentLevel.getCannons()) { // Accessing cannons from currentLevel
            int x = (int) (c.getHitbox().x - xLvlOffset);
            int width = CANNON_WIDTH;

            if (c.getObjType() == CANNON_RIGHT) {
                x += width;
                width *= -1;
            }
            g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y), width, CANNON_HEIGHT, null);
        }
    }

    private void drawTraps(Graphics g, int xLvlOffset) {
        for (Spike s : currentLevel.getSpikes()) // Accessing spikes from currentLevel
            g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset()), SPIKE_WIDTH, SPIKE_HEIGHT, null);
    }

    private void drawContainers(Graphics g, int xLvlOffset) {
        for (GameContainer gc : containers)
            if (gc.isActive()) {
                int type = 0;
                if (gc.getObjType() == BARREL)
                    type = 1;
                g.drawImage(containerImgs[type][gc.getAniIndex()], (int) (gc.getHitbox().x - gc.getxDrawOffset() - xLvlOffset), (int) (gc.getHitbox().y - gc.getyDrawOffset()), CONTAINER_WIDTH,
                    CONTAINER_HEIGHT, null);
            }
    }

    private void drawPotions(Graphics g, int xLvlOffset) {
        for (Potion p : potions)
            if (p.isActive()) {
                int type = 0;
                if (p.getObjType() == BLUE_POTION)
                    type = 1;
                g.drawImage(potionImgs[type][p.getAniIndex()], (int) (p.getHitbox().x - p.getxDrawOffset() - xLvlOffset), (int) (p.getHitbox().y - p.getyDrawOffset()), POTION_WIDTH, POTION_HEIGHT,
                    null);
            }
    }

    // NEW METHOD: drawStars
    private void drawStars(Graphics g, int xLvlOffset) {
        for (Star s : stars) { // Loop through the 'stars' field
            if (s.isActive()) {
                g.drawImage(starImgs[s.getAniIndex()],
                        (int) (s.getHitbox().x - s.getxDrawOffset() - xLvlOffset),
                        (int) (s.getHitbox().y - s.getyDrawOffset()),
                        STAR_WIDTH, STAR_HEIGHT, null);
            }
        }
    }

    public void resetAllObjects() {
        loadObjects(playing.getLevelManager().getCurrentLevel()); // Reloads potions, containers, stars

        // Reset individual managed lists
        for (Potion p : potions)
            p.reset();
        for (GameContainer gc : containers)
            gc.reset();
        for (Star s : stars)
            s.reset();

        // Reset objects accessed directly from currentLevel
        for (Cannon c : currentLevel.getCannons())
            c.reset();
        for (Spike s : currentLevel.getSpikes())
            s.reset();
        // Assuming BackgroundTree does NOT need reset, as per your previous request.
        // If it does, you'd re-add: for (BackgroundTree bt : currentLevel.getTrees()) bt.reset();

        projectiles.clear(); // Clear all projectiles
    }
}