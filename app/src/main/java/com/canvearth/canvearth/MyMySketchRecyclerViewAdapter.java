package com.canvearth.canvearth;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.canvearth.canvearth.MySketchFragment.OnListFragmentInteractionListener;
import com.canvearth.canvearth.sketch.NearbySketch;

import java.util.ArrayList;
import java.util.List;

public class MyMySketchRecyclerViewAdapter extends RecyclerView.Adapter<MyMySketchRecyclerViewAdapter.ViewHolder> {

    private List<NearbySketch.Sketch> mValues = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;

    public MyMySketchRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    public void setSketches(List<NearbySketch.Sketch> sketches) {
        mValues = sketches;
        notifyDataSetChanged();
    }

    public int addSketch(NearbySketch.Sketch sketch) {
        mValues.add(sketch);
        notifyDataSetChanged();
        return mValues.size() - 1;
    }

    public void changeSketch(int idx, NearbySketch.Sketch sketch) {
        mValues.set(idx, sketch);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sketchshow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mSketchName.setText(mValues.get(position).name);
        Uri uri = mValues.get(position).photo.getUri();
        if (uri != null) {
            Glide.with(holder.mSketchView)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("Glide", e.getMessage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resouorce, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.mView.findViewById(R.id.sketchShowProgressBarForItem).setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.mSketchView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.mItem);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mSketchView;
        public final TextView mSketchName;
        public NearbySketch.Sketch mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSketchView = view.findViewById(R.id.sketch_view);
            mSketchName = view.findViewById(R.id.sketch_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSketchName.getText() + "'";
        }
    }
}

