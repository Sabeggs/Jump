package objects;

import static utilz.Constants.ObjectConstants.STAR_HEIGHT_DEFAULT;
import static utilz.Constants.ObjectConstants.STAR_WIDTH_DEFAULT;

import mainn.Game;

public class Star extends GameObject {

    public Star(int x, int y, int objType) {
        super(x, y, objType);
        initHitbox(STAR_WIDTH_DEFAULT, STAR_HEIGHT_DEFAULT);
        doAnimation = true;
        setxDrawOffset(0);
        setyDrawOffset(0);
    }

    public void update() {
        super.update();
    }
}