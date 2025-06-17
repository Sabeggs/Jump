package entities;

import utilz.LoadSave;

import static utilz.Constants.PlayerConstants.*;

import mainn.Game;

public enum PlayerCharacter {

    FROG(11, 12, 1,  1, 7, 1, 
            IDLE, RUNNING, JUMP, FALLING, HIT, DEAD,
            "", 
            DEAD + 1, 0,
            32, 32,
            20, 27,
            6, 5);


    public int spriteA_IDLE, spriteA_RUNNING, spriteA_JUMP, spriteA_FALLING, spriteA_HIT, spriteA_DEAD;
    public int rowIDLE, rowRUNNING, rowJUMP, rowFALLING, rowHIT, rowDEAD;
    public String playerAtlas;
    public int rowA, colA;
    public int spriteW, spriteH;
    public int hitboxW, hitboxH;
    public int xDrawOffset, yDrawOffset;


    /*
     private float xDrawOffset = 21 * Game.SCALE;
     private float yDrawOffset = 4 * Game.SCALE;
     */

    PlayerCharacter(int spriteA_IDLE, int spriteA_RUNNING, int spriteA_JUMP, int spriteA_FALLING, int spriteA_HIT, int spriteA_DEAD, // Removed spriteA_ATTACK parameter
            int rowIDLE, int rowRUNNING, int rowJUMP, int rowFALLING, int rowHIT, int rowDEAD, // Removed rowATTACK parameter
            String playerAtlas, int rowA, int colA, int spriteW, int spriteH,
            int hitboxW, int hitboxH,
            int xDrawOffset, int yDrawOffset) {

        this.spriteA_IDLE = spriteA_IDLE;
        this.spriteA_RUNNING = spriteA_RUNNING;
        this.spriteA_JUMP = spriteA_JUMP;
        this.spriteA_FALLING = spriteA_FALLING;
        this.spriteA_HIT = spriteA_HIT;
        this.spriteA_DEAD = spriteA_DEAD;

        this.rowIDLE = rowIDLE;
        this.rowRUNNING = rowRUNNING;
        this.rowJUMP = rowJUMP;
        this.rowFALLING = rowFALLING;
        this.rowHIT = rowHIT;
        this.rowDEAD = rowDEAD;

        this.playerAtlas = playerAtlas;
        this.rowA = rowA;
        this.colA = colA;
        this.spriteW = spriteW;
        this.spriteH = spriteH;

        this.hitboxW = hitboxW;
        this.hitboxH = hitboxH;

        this.xDrawOffset = (int) (xDrawOffset * Game.SCALE);
        this.yDrawOffset = (int) (yDrawOffset * Game.SCALE);
    }

    public int getSpriteAmount(int player_action) {
        return switch (player_action) {
            case IDLE -> spriteA_IDLE;
            case RUNNING -> spriteA_RUNNING;
            case JUMP -> spriteA_JUMP;
            case FALLING -> spriteA_FALLING;
            case HIT -> spriteA_HIT;
            case DEAD -> spriteA_DEAD;
            default -> 1;
        };
    }

    public int getRowIndex(int player_action) {
        return switch (player_action) {
            case IDLE -> rowIDLE;
            case RUNNING -> rowRUNNING;
            case JUMP -> rowJUMP;
            case FALLING -> rowFALLING;
            case HIT -> rowHIT;
            case DEAD -> rowDEAD;
            default -> 1;
        };
    }

}