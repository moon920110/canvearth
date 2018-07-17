package com.canvearth.canvearth.client;

import com.canvearth.canvearth.databinding.ItemPhotoBinding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        final Context context = parent.getContext();

        final ItemPhotoBinding b = ItemPhotoBinding.inflate(LayoutInflater.from(context), parent, false);
        b.setHandler(this);

        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        final Photo photo = getItem(position);

        holder.m_b.setPosition(position);
        holder.m_b.setPhoto(photo);
        holder.m_b.executePendingBindings();
    }

    public abstract void onClickPhoto(View view, int position, Photo photo);
    public abstract Photo getItem(int position);

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ViewHolder(ItemPhotoBinding b)
        {
            super(b.getRoot());
            m_b = b;
        }

        private ItemPhotoBinding m_b;
    }
}
