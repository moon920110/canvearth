package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import junit.framework.Assert;

import java.util.Iterator;

public class PixelDataSquare {
    private PixelData leftTopPixelData;
    private PixelData rightBottomPixelData;

    public PixelDataSquare(PixelData leftTopPixelData, PixelData rightBottomPixelData) {
        this.leftTopPixelData = leftTopPixelData;
        this.rightBottomPixelData = rightBottomPixelData;
        Assert.assertEquals(leftTopPixelData.zoom, rightBottomPixelData.zoom);
    }

    public int zoom() {
        return leftTopPixelData.zoom;
    }

    public PixelData getLeftTopPixelData() {
        return leftTopPixelData;
    }

    public PixelData getRightBottomPixelData() {
        return rightBottomPixelData;
    }

    public void setLeftTopPixelData(PixelData leftTopPixelData) {
        this.leftTopPixelData = leftTopPixelData;
    }

    public void setRightBottomPixelData(PixelData rightBottomPixelData) {
        this.rightBottomPixelData = rightBottomPixelData;
    }

    public Iterator<PixelData> pixelDataIterator() {
        return new Iterator<PixelData>() {
            private PixelData cursor = leftTopPixelData.clone();
            @Override
            public boolean hasNext() {
                return cursor != null;
            }

            @Override
            public PixelData next() {
                PixelData returnCursor = cursor.clone();
                if (cursor.equals(rightBottomPixelData)) {
                    cursor = null;
                } else {
                    if (cursor.x == rightBottomPixelData.x) {
                        cursor.y++;
                        cursor.x = leftTopPixelData.x;
                    } else {
                        cursor.x++;
                    }
                }
                return returnCursor;
            }
        };
    }

    public FBPixelDataSquare toFB() {
        return new FBPixelDataSquare(new FBPixelData(leftTopPixelData), new FBPixelData(rightBottomPixelData));
    }

    public LatLngBounds getLatLngBounds() {
        LatLng southWest = new LatLng(rightBottomPixelData.getSouth(), leftTopPixelData.getWest());
        LatLng northEast = new LatLng(leftTopPixelData.getNorth(), rightBottomPixelData.getEast());
        return new LatLngBounds(southWest, northEast);
    }
}
