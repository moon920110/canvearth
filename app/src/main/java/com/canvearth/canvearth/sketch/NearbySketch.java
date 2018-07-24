package com.canvearth.canvearth.sketch;

import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.Photo;
import com.canvearth.canvearth.pixel.PixelDataSquare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class NearbySketch {
    /**
     * A dummy item representing a piece of content.
     */
    public static class Sketch {
        public final String id;
        public Photo photo;
        public final String name;
        public final PixelDataSquare pixelDataSquare;

        public Sketch(String id, Photo photo, String name, PixelDataSquare pixelDataSquare) {
            this.id = id;
            this.photo = photo;
            this.name = name;
            this.pixelDataSquare = pixelDataSquare;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
