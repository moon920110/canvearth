package com.canvearth.canvearth.client;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.databinding.FragmentSketchPlacerBinding;

public class SketchPlacerFragment extends Fragment {
    private static final String KEY_PHOTO = "KEY_SKETCH_FRAGMENT_PHOTO";
    private FragmentSketchPlacerBinding binding = null;
    private static SketchPlacerView sketchPlacerView = null;

    public SketchPlacerFragment() {

    }

    public static Fragment newInstance(Photo photo) {
        SketchPlacerFragment fragment = new SketchPlacerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_PHOTO, photo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sketch_placer, container, false);
        binding.setHandler(this);

        View view = binding.getRoot();
        if (getArguments() == null) {
            binding.confirmButton.setVisibility(View.GONE);
            return view;
        }
        binding.confirmButton.setVisibility(View.VISIBLE);
        Photo photo = getArguments().getParcelable(KEY_PHOTO);
        binding.setSketchPhoto(photo);
        sketchPlacerView = view.findViewById(R.id.sketch_placer);
        sketchPlacerView.setImageDrawable(photo.getDrawable());
        sketchPlacerView.setVisibility(View.VISIBLE);
        binding.cancelButton.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClickConfirmButton() {
        RectF bounds = sketchPlacerView.getBounds();
        Rect rect = new Rect((int) bounds.left,
                (int) bounds.top,
                (int) bounds.right,
                (int) bounds.bottom);
        ((MapsActivity) getActivity()).addSketchConfirm(rect, binding.getSketchPhoto());
    }

    public void onClickCancelButton() {
        binding.cancelButton.setVisibility(View.INVISIBLE);
        ((MapsActivity) getActivity()).addSketchCancel();
    }
}
