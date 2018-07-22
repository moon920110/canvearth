package com.canvearth.canvearth.client;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.canvearth.canvearth.MapsActivity;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.InputStream;
import java.net.URL;

public final class Photo implements Parcelable, Comparable<Photo>
{
    //=========================================================================
    // Constructors
    //=========================================================================

    public Photo()
    {
        // Nothing
    }

    public Photo(Uri uri) {
        m_uri = uri;
    }

    //=========================================================================
    // Override Methods
    //=========================================================================

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(m_uri).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Photo == false)
        {
            return false;
        }

        if (obj == this)
        {
            return true;
        }

        final Photo other = (Photo) obj;

        return new EqualsBuilder().append(m_uri, other.m_uri).isEquals();
    }

    @Override
    public int compareTo(@NonNull Photo o)
    {
        return new CompareToBuilder()
                .append(o.m_dateTaken, m_dateTaken)
                .append(o.m_uri, m_uri)
                .toComparison();
    }

    //=========================================================================
    // Public Methods
    //=========================================================================

    public Uri getUri()
    {
        return m_uri;
    }

    public void setUri(Uri uri)
    {
        m_uri = uri;
    }

    public long getDateTaken()
    {
        return m_dateTaken;
    }

    public void setDateTaken(long dateTaken)
    {
        m_dateTaken = dateTaken;
    }

    //=========================================================================
    // Variables
    //=========================================================================

    private Uri m_uri;
    private long m_dateTaken;

    //=========================================================================
    // Parcelable
    //=========================================================================

    public Photo(Parcel in)
    {
        m_uri = in.readParcelable(Uri.class.getClassLoader());
        m_dateTaken = in.readLong();
    }

    public Photo(@DrawableRes int photoRes) {
        m_uri = Uri.parse("android.resource://" + MapsActivity.PACKAGE_NAME + "/" + photoRes);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(m_uri, flags);
        dest.writeLong(m_dateTaken);
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>()
    {
        @Override
        public Photo createFromParcel(Parcel source)
        {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size)
        {
            return new Photo[size];
        }
    };
}