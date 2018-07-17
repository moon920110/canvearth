package com.canvearth.canvearth.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class PhotoModel {
    private Context m_context;
    private OnStatusListener m_onStatusListener;
    private MediaObserver m_mediaObserver;
    private List<Photo> m_photosList;

    public PhotoModel(Context context, OnStatusListener onStatusListener)
    {
        m_context = context;
        m_onStatusListener = onStatusListener;

        m_mediaObserver = new MediaObserver(context);

        if (isLoaded())
        {
            m_onStatusListener.onLoadSuccess(this);
        }
        else
        {
            refresh();
        }
    }

    public void handleResume()
    {
        if (m_mediaObserver.isDirty())
        {
            refresh();
        }
    }

    public void handleDestroy()
    {
        m_mediaObserver.unbind();

        m_context = null;
        m_onStatusListener = null;
    }

    @SuppressLint("StaticFieldLeak")
    public void refresh()
    {
        m_mediaObserver.resetDirty();

        if (m_onStatusListener != null)
        {
            m_onStatusListener.onLoadStart(this);
        }

        final AsyncTask<Void, Void, Object> asyncTask = new AsyncTask<Void, Void, Object>()
        {
            @Override
            protected Object doInBackground(Void... params)
            {
                try
                {
                    return MediaResolver.getPhotos(m_context);
                }
                catch (Exception e)
                {
                    return e;
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void onPostExecute(Object o)
            {
                if (m_onStatusListener != null)
                {
                    if (o instanceof Exception)
                    {
                        final Exception e = (Exception) o;
                        m_onStatusListener.onLoadFailure(PhotoModel.this, e);
                    }
                    else
                    {
                        m_photosList = (List<Photo>) o;
                        m_onStatusListener.onLoadSuccess(PhotoModel.this);
                    }
                }
            }
        };

        asyncTask.execute();
    }

    public boolean isLoaded()
    {
        return m_photosList != null;
    }

    public List<Photo> getPhotosList()
    {
        return m_photosList;
    }

    public interface OnStatusListener
    {
        void onLoadStart(PhotoModel photosModel);

        void onLoadSuccess(PhotoModel photosModel);

        void onLoadFailure(PhotoModel photosModel, Exception e);
    }
}
