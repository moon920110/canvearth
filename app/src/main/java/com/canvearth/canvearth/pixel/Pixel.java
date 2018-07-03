package com.canvearth.canvearth.pixel;

import uk.me.jstott.jcoord.UTMRef;

public class Pixel {
    protected String pixelId;

    protected UTMRef rootUTMRef;
    protected double easting;
    protected double northing;
    protected double width;
    protected double height;

    protected int level; // leaf Pixel's level is 0.
    protected String[] childrenIds;
    protected String parentId;

    //TODO image member variable needed
}
