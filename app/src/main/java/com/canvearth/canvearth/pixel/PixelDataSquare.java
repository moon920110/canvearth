package com.canvearth.canvearth.pixel;

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
            private PixelData cursor = leftTopPixelData;
            @Override
            public boolean hasNext() {
                return cursor != null;
            }

            @Override
            public PixelData next() {
                PixelData returnCursor = cursor;
                if (cursor.equals(rightBottomPixelData)) {
                    cursor = null;
                } else {
                    cursor = returnCursor.clone();
                    if (returnCursor.x == rightBottomPixelData.x) {
                        returnCursor.y++;
                        returnCursor.x = leftTopPixelData.x;
                    } else {
                        returnCursor.x++;
                    }
                }
                return returnCursor;
            }
        };
    }
}
