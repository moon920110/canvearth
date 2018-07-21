package com.canvearth.canvearth;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sketchshow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Uri uri = mValues.get(position).photo.getUri();
        Glide.with(holder.mSketchView).load(uri).into(holder.mSketchView);
        holder.mSketchName.setText(mValues.get(position).name);

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

