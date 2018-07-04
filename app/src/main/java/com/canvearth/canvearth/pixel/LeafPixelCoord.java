package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.Constants;

import java.util.Date;

// TODO Change this data structure
public class LeafPixelCoord extends PixelCoord {
    public String modifiedUserToken;
    public String modifiedTime;
    public Color color;

    public LeafPixelCoord() {
        // Default constructor required for Firebase db
    }

    public LeafPixelCoord(int x, int y, String userToken, Date now, Color color) {
        super(x, y, Constants.LEAF_PIXEL_LEVEL);

        this.modifiedUserToken = userToken;
        this.modifiedTime = now.toString();
        this.color = color;
    }
}
