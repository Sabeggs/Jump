package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;
import static utilz.Constants.Directions.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import gamestates.Playing;
import mainn.Game;
import utilz.LoadSave; 
import static utilz.Constants.UI.*; 

public class Player extends Entity {

    // class variables
    private BufferedImage[][] animations;
    private boolean moving = false;
    private boolean left, right, jump;
    private int[][] lvlData;

    // jumping / gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    // health system
    private int lives = 3;
    private final int MAX_LIVES = 3;
    private boolean dead = false;

    // --- NEW Animated Heart fields ---
    private BufferedImage[] heartAnimations; 
    private int heartAnimationTick = 0;
    private int heartAnimationFrame = 0;  
  

    private int flipX = 0;
    private int flipW = 1;

    private Playing playing;

    private int tileY = 0;

    private BufferedImage lifeIcon;

    public Player(PlayerCharacter playerCharacter, Playing playing) {
        super(0, 0, (int) (playerCharacter.spriteW * Game.SCALE), (int) (playerCharacter.spriteH * Game.SCALE));
        this.playing = playing;
        this.state = IDLE;
        this.walkSpeed = Game.SCALE * 1.0f;
        animations = LoadSave.loadAnimations(playerCharacter);
        lifeIcon = LoadSave.GetSpriteAtlas(LoadSave.LIFE_ICON); 
        heartAnimations = LoadSave.GetHeartAnimationSprites();
        initHitbox(playerCharacter.hitboxW, playerCharacter.hitboxH);
    }

    // spawn position setup
    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    // player update
    public void update() {
        // player death check
        if (lives <= 0) {
            if (state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

                // check if player died in air
                if (!IsEntityOnFloor(hitbox, lvlData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if (aniIndex == PlayerCharacter.FROG.getSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();

                if (inAir)
                    if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;

            }
            return;
        }

        updateHeartAnimation();

        if (state == HIT) {
            if (aniIndex <= PlayerCharacter.FROG.getSpriteAmount(state) - 3)
                pushBack(pushBackDir, lvlData, 1.25f);
            updatePushBackDrawOffset();
        } else
            updatePos();

        if (moving) {
            checkPotionTouched();
            checkSpikesTouched();
            checkInsideWater();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
        }


        updateAnimationTick();
        setAnimation();
    }

    // water damage check
    private void checkInsideWater() {
        if (IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData()))
            kill();
    }

    // spike collision check
    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    // potion collision check
    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);
    }

    // player rendering
    public void render(Graphics g, int lvlOffset) {
        g.drawImage(animations[PlayerCharacter.FROG.getRowIndex(state)][aniIndex], (int) (hitbox.x - PlayerCharacter.FROG.xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - PlayerCharacter.FROG.yDrawOffset + (int) (pushDrawOffset)), width * flipW, height, null);//        drawHitbox(g, lvlOffset);
        drawUI(g);
    }

    // ui rendering
    private void drawUI(Graphics g) {
        int heartIconX = (int) (20 * Game.SCALE); // New X position for the heart icon
        int heartIconY = (int) (20 * Game.SCALE); // New Y position for the heart icon

        if (heartAnimations != null && heartAnimations.length > 0 && heartAnimations[heartAnimationFrame] != null) {
            // Draw the heart animation frame
            g.drawImage(heartAnimations[heartAnimationFrame],
                    heartIconX,
                    heartIconY,
                    (int) (HEART_SPRITE_WIDTH * Game.SCALE),
                    (int) (HEART_SPRITE_HEIGHT * Game.SCALE),
                    null);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Jersey15-Regular", Font.BOLD, (int) (24 * Game.SCALE)));

            // Position the text "x" + lives next to the heart
            int textX = heartIconX + (int) (HEART_SPRITE_WIDTH * Game.SCALE) + (int) (5 * Game.SCALE);
            int textY = heartIconY + (int) (HEART_SPRITE_HEIGHT * Game.SCALE / 2) + (int) (g.getFontMetrics().getAscent() / 2) - (int) (g.getFontMetrics().getDescent() / 2);

            g.drawString("x" + lives, textX, textY);
        } else if (lifeIcon != null) { // Fallback if animated heart is not loaded
            int fallbackIconSize = (int) (20 * Game.SCALE); // You might need to adjust this size
            g.drawImage(lifeIcon, heartIconX, heartIconY, fallbackIconSize, fallbackIconSize, null);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Jersey15-Regular", Font.BOLD, (int) (24 * Game.SCALE)));

            // Position the text "x" + lives next to the fallback icon
            int textX = heartIconX + fallbackIconSize + (int) (5 * Game.SCALE);
            int textY = heartIconY + (int) (fallbackIconSize / 2) + (int) (g.getFontMetrics().getAscent() / 2) - (int) (g.getFontMetrics().getDescent() / 2);
            g.drawString("x" + lives, textX, textY);
        }
    }

    // animation tick update
    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= PlayerCharacter.FROG.getSpriteAmount(state)) {
                aniIndex = 0;
                if (state == HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    if (!IsFloor(hitbox, 0, lvlData))
                        inAir = true;
                }
            }
        }
    }

    private void updateHeartAnimation() {
        heartAnimationTick++;
        if (heartAnimationTick >= HEART_ANIM_SPEED) { // Uses HEART_ANIM_SPEED from Constants.UI
            heartAnimationTick = 0; // Reset tick
            heartAnimationFrame++;  // Move to the next frame
            if (heartAnimationFrame >= HEART_SPRITE_FRAMES) { // Uses HEART_SPRITE_FRAMES from Constants.UI
                heartAnimationFrame = 0; // Loop back to the first frame
            }
        }
    }
    // animation state setting
    private void setAnimation() {
        int startAni = state;

        if (state == HIT)
            return;

        if (moving)
            state = RUNNING;
        else
            state = IDLE;

        if (inAir) {
            if (airSpeed < 0)
                state = JUMP;
            else
                state = FALLING;
        }
        
        if (startAni != state)
            resetAniTick();
    }

    // animation tick reset
    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    // player position update
    private void updatePos() {
        moving = false; // Reset moving state for the current frame

        if (jump) { // Handles jump initiation (sets initial airSpeed)
            jump();
        }

        float xSpeed = 0; // Calculate desired horizontal speed for this frame
        if (left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        } else if (right && !left) { // Use else if to ensure only one direction is chosen
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }

        // --- Vertical Gravity Application & Initial In-Air Check ---
        // If not currently in air, check if player is actually on solid ground
        if (!inAir && !utilz.HelpMethods.IsEntityOnFloor(hitbox, lvlData)) {
            inAir = true; // Player has fallen off an edge
        }

        // --- HORIZONTAL MOVEMENT & COLLISION RESOLUTION ---
        // Try to move horizontally based on xSpeed. Check for collision only for the X-axis.
        if (utilz.HelpMethods.CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed; // No horizontal collision, apply movement
        } else {
            // Horizontal collision detected: snap to wall and stop horizontal movement
            hitbox.x = utilz.HelpMethods.GetEntityXPosNextToWall(hitbox, xSpeed);
            xSpeed = 0; // <--- CRUCIAL: Stop horizontal velocity after hitting a wall
        }

        // --- VERTICAL MOVEMENT & COLLISION RESOLUTION ---
        if (inAir) {
            if (utilz.HelpMethods.CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed; // No vertical collision, apply movement
                airSpeed += GRAVITY; // Apply gravity
            } else {
                hitbox.y = utilz.HelpMethods.GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0) { // If falling and hit the floor
                    resetInAir(); // This method MUST set inAir=false AND airSpeed=0
                } else { // If jumping and hit a roof
                    airSpeed = fallSpeedAfterCollision; // Start falling down

                }
            }
        }

        if (Math.abs(xSpeed) > 0 || inAir) {
            moving = true;
        } else {
            moving = false; // Character is on ground and not moving horizontally
        }
    }

    // jump action
    private void jump() {
        if (inAir)
            return;
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = true;
        airSpeed = jumpSpeed;
    }

    // in-air state reset
    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    // x position update
    private void updateXPos(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            hitbox.x += xSpeed;
        else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
        }
    }

    public void hit() {
        if (state == HIT) {
            return;
        }

        lives--; // decrease a life/hit point
        newState(HIT); // transition to the hit animation

        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.PLAYER_HIT);

    }


    // player hit with enemy pushback
    public void hit(Enemy e) {
        if (state == HIT) { // already hit, basic invincibility
            return;
        }
        hit(); // call the base hit method
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;

        if (e.getHitbox().x < hitbox.x)
            pushBackDir = RIGHT;
        else
            pushBackDir = LEFT;
    }

    // kill method (instant death)
    public void kill() {
        lives = 0; // set lives to 0 for instant death
    }

    // NEW METHOD FOR GAINING LIVES (used by red potion)
    public void addLife(int value) {
        lives += value;
        if (lives > MAX_LIVES) {
            lives = MAX_LIVES; // Cap lives at MAX_LIVES
        }
    }

    // level data loading
    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    // direction booleans reset
    public void resetDirBooleans() {
        left = false;
        right = false;
    }


    // left getter
    public boolean isLeft() {
        return left;
    }

    // left setter
    public void setLeft(boolean left) {
        this.left = left;
    }

    // right getter
    public boolean isRight() {
        return right;
    }

    // right setter
    public void setRight(boolean right) {
        this.right = right;
    }

    // jump setter
    public void setJump(boolean jump) {
        this.jump = jump;
    }

    // reset all player properties
    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        moving = false;
        airSpeed = 0f;
        state = IDLE;
        lives = MAX_LIVES; // reset lives to full
        dead = false; // reset death status 

        hitbox.x = x;
        hitbox.y = y;

        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    // tile y getter
    public int getTileY() {
        return tileY;
    }

    // player status getters
    public boolean isDead() {
        return dead;
    }

    public int getLives() {
        return lives;
    }
}