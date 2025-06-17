package objects;

// Make sure these imports are correct based on your Constants.java and Game.java
import static utilz.Constants.ObjectConstants.STAR_HEIGHT_DEFAULT;
import static utilz.Constants.ObjectConstants.STAR_WIDTH_DEFAULT;

import mainn.Game; // Assuming Game.SCALE is used here indirectly via GameObject's initHitbox

public class Star extends GameObject {

    // --- IMPORTANT: This is the constructor that was missing or incorrect ---
    public Star(int x, int y, int objType) {
        super(x, y, objType); // Calls the GameObject constructor
        // Corrected: initHitbox should use DEFAULT dimensions as GameObject's initHitbox
        // will apply Game.SCALE
        initHitbox(STAR_WIDTH_DEFAULT, STAR_HEIGHT_DEFAULT);
        doAnimation = true; 
        setxDrawOffset(0); // Assuming no offset needed for star image
        setyDrawOffset(0); // Assuming no offset needed for star image
    }

    public void update() {
        // This calls the GameObject's update method, which handles animation
        super.update();
    }
}