package com.canvearth.canvearth.client;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

public final class MediaObserver
{
    //=========================================================================
    // Constructors
    //=========================================================================

    public MediaObserver(Context context)
    {
        m_context = context;

        m_observer = new ContentObserver(new Handler())
        {
            @Override
            public void onChange(boolean selfChange)
            {
                super.onChange(selfChange);

                if (checkValid(m_context) == true)
                {
                    m_isDirty = true;
                }
            }
        };

        final ContentResolver contentResolver = m_context.getContentResolver();
        contentResolver.registerContentObserver(CONTENT_URI, false, m_observer);
    }

    //=========================================================================
    // Public Methods
    //=========================================================================

    public void unbind()
    {
        if (m_observer != null && m_context != null)
        {
            final ContentResolver contentResolver = m_context.getContentResolver();
            contentResolver.unregisterContentObserver(m_observer);

            m_observer = null;
        }

        m_context = null;
    }

    public boolean isDirty()
    {
        return m_isDirty;
    }

    public void resetDirty()
    {
        m_isDirty = false;
    }

    //=========================================================================
    // Private Methods
    //=========================================================================

    private boolean checkValid(Context context)
    {
        Cursor cursor = null;

        try
        {
            cursor = context.getContentResolver().query(CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            if (cursor != null && cursor.moveToNext() == true)
            {
                final int dateTakenIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
                final long lastDateTaken = cursor.getLong(dateTakenIndex);

                return Math.abs(System.currentTimeMillis() - lastDateTaken) < 10000;
            }
        }
        catch (Exception e)
        {
            // Nothing
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        return true;
    }

    //=========================================================================
    // Constants
    //=========================================================================

    private static final Uri CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    //=========================================================================
    // Variables
    //=========================================================================

    private Context m_context;
    private ContentObserver m_observer;
    private boolean m_isDirty;
}
