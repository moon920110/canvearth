package com.canvearth.canvearth.sketch;

import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.Photo;

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
        public final Photo photo;
        public final String name;

        public Sketch(String id, Photo photo, String name) {
            this.id = id;
            this.photo = photo;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
