package com.canvearth.canvearth.client;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.databinding.FragmentSketchPlacerBinding;
import com.canvearth.canvearth.utils.DatabaseUtils;

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
        sketchPlacerView.setPhoto(photo);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClickConfirmButton() {
        ((MapsActivity) getActivity()).addSketchFinish(sketchPlacerView.getBound(), sketchPlacerView.getPhoto());
    }

}
