package com.canvearth.canvearth;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.request.RequestOptions;
import com.canvearth.canvearth.client.Photo;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public final class BindingAdapters
{
    @BindingAdapter({ "navigationIcon", "navigationIconColor" })
    public static void bindNavigationIcon(Toolbar toolbar, Drawable iconRes, @ColorInt int colorInt)
    {
        toolbar.setNavigationIcon(iconRes);

        if (toolbar.getNavigationIcon() != null)
        {
            DrawableCompat.setTint(toolbar.getNavigationIcon(), colorInt);
        }
    }

    @BindingAdapter("navigationOnClick")
    public static void bindNavigationOnClick(Toolbar toolbar, View.OnClickListener clickListener)
    {
        toolbar.setNavigationOnClickListener(clickListener);
    }

    @BindingAdapter({ "glide_photo",  "glide_placeholder" })
    public static void bindGlidePhoto(ImageView view, Photo photo, @DrawableRes int placeHolder)
    {
        if (photo == null) return;

        final RequestOptions requestOptions = new RequestOptions()
                .placeholder(placeHolder)
                .signature(createDensitySignature(view));

        Glide.with(view.getContext())
                .load(photo.getUri())
                .apply(requestOptions)
                .into(view);
    }

    private static DensitySignature createDensitySignature(View view)
    {
        return new DensitySignature(view.getResources().getDisplayMetrics().densityDpi);
    }
}

final class DensitySignature implements Key
{
    //=========================================================================
    // Constructors
    //=========================================================================

    DensitySignature(int densityDpi)
    {
        m_densityDpi = densityDpi;
    }

    //=========================================================================
    // Override Methods
    //=========================================================================

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DensitySignature that = (DensitySignature) o;

        return this.m_densityDpi == that.m_densityDpi;
    }

    @Override
    public int hashCode()
    {
        return m_densityDpi;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest)
    {
        messageDigest.update(ByteBuffer.allocate(4).putInt(m_densityDpi).array());
    }

    //=========================================================================
    // Variables
    //=========================================================================

    private int m_densityDpi;
}