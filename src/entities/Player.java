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

public class Player extends Entity {

    // class variables
    private BufferedImage[][] animations;
    private boolean moving = false, attacking = false;
    private boolean left, right, jump;
    private int[][] lvlData;

    // jumping / gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    // health system
    private int lives = 3;    
    private final int MAX_LIVES = 3;    
    private boolean dead = false;    

    // status bar ui
    private BufferedImage statusBarImg;

    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);

    private int healthBarXStart = (int) (34 * Game.SCALE);    
    private int healthBarYStart = (int) (14 * Game.SCALE);

    private int powerBarWidth = (int) (104 * Game.SCALE);
    private int powerBarHeight = (int) (2 * Game.SCALE);
    private int powerBarXStart = (int) (44 * Game.SCALE);
    private int powerBarYStart = (int) (34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int powerMaxValue = 200;
    private int powerValue = powerMaxValue;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    private int tileY = 0;

    private boolean powerAttackActive;
    private int powerAttackTick;
    private int powerGrowSpeed = 15;
    private int powerGrowTick;

    private final PlayerCharacter playerCharacter;

    // NEW: Variable for the life icon image
    private BufferedImage lifeIcon;    

    // constructor
    public Player(PlayerCharacter playerCharacter, Playing playing) {
        super(0, 0, (int) (playerCharacter.spriteW * Game.SCALE), (int) (playerCharacter.spriteH * Game.SCALE));
        this.playerCharacter = playerCharacter;
        this.playing = playing;
        this.state = IDLE;
        this.walkSpeed = Game.SCALE * 1.0f;
        animations = LoadSave.loadAnimations(playerCharacter);
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
        lifeIcon = LoadSave.GetSpriteAtlas(LoadSave.LIFE_ICON); // NEW: Load the life icon
        initHitbox(playerCharacter.hitboxW, playerCharacter.hitboxH);
        initAttackBox();
    }

    // spawn position setup
    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    // attack box initialization
    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int) (35 * Game.SCALE), (int) (20 * Game.SCALE));
        resetAttackBox();
    }

    // player update
    public void update() {
        updatePowerBar();

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
            } else if (aniIndex == playerCharacter.getSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();

                // fall if in air
                if (inAir)
                    if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;

            }
            return;
        }

        updateAttackBox();

        if (state == HIT) {
            if (aniIndex <= playerCharacter.getSpriteAmount(state) - 3)
                pushBack(pushBackDir, lvlData, 1.25f);
            updatePushBackDrawOffset();
        } else
            updatePos();

        if (moving) {
            checkPotionTouched();
            checkSpikesTouched();
            checkInsideWater();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
            if (powerAttackActive) {
                powerAttackTick++;
                if (powerAttackTick >= 35) {
                    powerAttackTick = 0;
                    powerAttackActive = false;
                }
            }
        }

        if (attacking || powerAttackActive)
            checkAttack();

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

    // attack logic
    private void checkAttack() {
        if (attackChecked || aniIndex != 1)
            return;
        attackChecked = true;

        if (powerAttackActive)
            attackChecked = false;

        playing.checkEnemyHit(attackBox);
        playing.checkObjectHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    // attack box position right
    private void setAttackBoxOnRightSide() {
        attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 5);
    }

    // attack box position left
    private void setAttackBoxOnLeftSide() {
        attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
    }

    // update attack box position
    private void updateAttackBox() {
        if (right && left) {
            if (flipW == 1) {
                setAttackBoxOnRightSide();
            } else {
                setAttackBoxOnLeftSide();
            }

        } else if (right || (powerAttackActive && flipW == 1))
            setAttackBoxOnRightSide();
        else if (left || (powerAttackActive && flipW == -1))
            setAttackBoxOnLeftSide();

        attackBox.y = hitbox.y + (Game.SCALE * 10);
    }

    // power bar update
    private void updatePowerBar() {
        powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);
        powerGrowTick++;
        if (powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    // player rendering
    public void render(Graphics g, int lvlOffset) {
        int aniStateForDrawing = state;
        if (state == ATTACK || powerAttackActive) {
            if (moving) { 
                aniStateForDrawing = RUNNING;
            } else { 
                aniStateForDrawing = IDLE;
            }
        }
        
        g.drawImage(animations[playerCharacter.getRowIndex(aniStateForDrawing)][aniIndex], (int) (hitbox.x - playerCharacter.xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - playerCharacter.yDrawOffset + (int) (pushDrawOffset)), width * flipW, height, null);
        drawHitbox(g, lvlOffset);
        drawUI(g);
    }

    // ui rendering
    private void drawUI(Graphics g) {
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        // health
        if (lifeIcon != null) {    
            int iconX = healthBarXStart + statusBarX;
            int iconY = healthBarYStart + statusBarY;
            int iconSize = (int)(100 * Game.SCALE);    

            g.drawImage(lifeIcon, iconX, iconY, iconSize, iconSize, null);

            g.setColor(Color.WHITE);    
            g.setFont(new Font("Jersey15-Regular", Font.BOLD, (int)(24 * Game.SCALE)));    

            int textX = iconX + iconSize + (int)(1 * Game.SCALE);    
            int textY = iconY + (int)(iconSize / 2) + (int)(g.getFontMetrics().getAscent() / 2) - (int)(g.getFontMetrics().getDescent() / 2); // Center text vertically with icon

            g.drawString("x" + lives, textX, textY);
        }
    }

    // animation tick update
    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= playerCharacter.getSpriteAmount(state)) {
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
                if (state == HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    if (!IsFloor(hitbox, 0, lvlData))
                        inAir = true;
                }
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

        if (powerAttackActive) {
            state = ATTACK;
            aniIndex = 1;
            aniTick = 0;
            return;
        }

        if (attacking) {
            state = ATTACK;
            if (startAni != ATTACK) {
                aniIndex = 1;
                aniTick = 0;
                return;
            }
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

        if (powerAttackActive) {
            // If power attack is active, force movement in current direction
            if ((!left && !right) || (left && right)) { // If no specific direction pressed, use facing direction
                if (flipW == -1) {
                    xSpeed = -walkSpeed;
                } else {
                    xSpeed = walkSpeed;
                }
            }
            xSpeed *= 3; // Boost speed for power attack
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
            if (powerAttackActive) {
                powerAttackActive = false;
                powerAttackTick = 0;
            }
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

    // power change
    public void changePower(int value) {
        powerValue += value;
        powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
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

    // attacking setter
    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
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
        attacking = false;
        moving = false;
        airSpeed = 0f;
        state = IDLE;
        // health reset
        lives = MAX_LIVES; // reset lives to full
        dead = false; // reset death status
        powerAttackActive = false;
        powerAttackTick = 0;
        powerValue = powerMaxValue;

        hitbox.x = x;
        hitbox.y = y;
        resetAttackBox();

        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    // attack box reset
    private void resetAttackBox() {
        if (flipW == 1)
            setAttackBoxOnRightSide();
        else
            setAttackBoxOnLeftSide();
    }

    // tile y getter
    public int getTileY() {
        return tileY;
    }

    // power attack activation
    public void powerAttack() {
        if (powerAttackActive)
            return;
        if (powerValue >= 60) {
            powerAttackActive = true;
            changePower(-60);
        }
    }

    // player status getters
    public boolean isDead() {
        return dead;
    }

    public int getLives() {
        return lives;
    }
}