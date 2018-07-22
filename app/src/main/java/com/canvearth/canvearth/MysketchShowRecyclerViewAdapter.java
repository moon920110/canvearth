package com.canvearth.canvearth;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.canvearth.canvearth.sketch.NearbySketch.Sketch;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Sketch} and makes a call to the
 * specified {@link SketchShowFragment.OnSketchShowFragmentInteractionListener}.
 */
public class MysketchShowRecyclerViewAdapter extends RecyclerView.Adapter<MysketchShowRecyclerViewAdapter.ViewHolder> {

    private List<Sketch> mValues = new ArrayList<>();
    private final SketchShowFragment.OnSketchShowFragmentInteractionListener mListener;

    public MysketchShowRecyclerViewAdapter(SketchShowFragment.OnSketchShowFragmentInteractionListener listener) {
        mListener = listener;
    }

    public void setSketches(List<Sketch> sketches) {
        mValues = sketches;
        notifyDataSetChanged();
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sketchshow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
//        holder.mSketchView.setImageDrawable(mValues.get(position).photo.getDrawable());
        Uri uri = mValues.get(position).photo.getUri();
        Glide.with(holder.mSketchView).load(uri).into(holder.mSketchView);
        holder.mSketchName.setText(mValues.get(position).name);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSketchShowFragmentInteraction(holder.mItem);
                    holder.mView.getRootView().findViewById(R.id.add_interest_button).setVisibility(View.VISIBLE);
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
        public Sketch mItem;

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
