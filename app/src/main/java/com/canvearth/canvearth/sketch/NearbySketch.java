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
     * An array of sample (dummy) items.
     */
    public static final List<Sketch> ITEMS = new ArrayList<Sketch>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Sketch> ITEM_MAP = new HashMap<String, Sketch>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Sketch item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Sketch createDummyItem(int position) {
        return new Sketch(String.valueOf(position), new Photo(R.drawable.earth), "dummy_sketch");
    }

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
