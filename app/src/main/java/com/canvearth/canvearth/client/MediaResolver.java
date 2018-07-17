package com.canvearth.canvearth.client;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MediaResolver
{
    //=========================================================================
    // Public Methods
    //=========================================================================

    public static List<Photo> getPhotos(Context context)
    {
        return getPhotos(context, -1);
    }

    public static List<Photo> getPhotos(Context context, int maxSize)
    {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
        {
            throw new IllegalArgumentException("Unable to get the ContentResolver.");
        }

        final List<Photo> photosList = new ArrayList<>();

        Cursor cursor = null;

        try
        {
            cursor = contentResolver.query(CONTENT_URI, PROJECTION, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            if (cursor == null)
            {
                throw new IllegalArgumentException("Unable to query the ContentResolver.");
            }

            final int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            final int mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
            final int dateTakenIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);

            while (cursor.moveToNext() == true)
            {
                final String data = cursor.getString(dataIndex);
                final String mimeType = cursor.getString(mimeTypeIndex);

                if (TextUtils.isEmpty(data) == true || TextUtils.isEmpty(mimeType) == true || mimeType.equals(MIME_TYPE_GIF) == true)
                {
                    continue;
                }

                final long dateTaken = cursor.getLong(dateTakenIndex);

                final Photo photo = new Photo();
                photo.setUri(toUri(data));
                photo.setDateTaken(dateTaken);

                photosList.add(photo);

                if (maxSize != -1 && photosList.size() >= maxSize)
                {
                    break;
                }
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        return photosList;
    }

    //=========================================================================
    // Private Methods
    //=========================================================================

    private static Uri toUri(String path)
    {
        final File file = new File(path);
        return Uri.fromFile(file);
    }

    //=========================================================================
    // Constants
    //=========================================================================

    private static final Uri CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION = new String[] {
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.DATE_TAKEN
    };

    private static final String MIME_TYPE_GIF = "image/gif";
}
