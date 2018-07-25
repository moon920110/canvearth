package com.canvearth.canvearth.client;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.databinding.FragmentSketchPlacerBinding;
import com.canvearth.canvearth.utils.BitmapUtils;

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
            binding.confirmContainer.setVisibility(View.INVISIBLE);
            return view;
        }
        binding.confirmContainer.setVisibility(View.VISIBLE);
        Photo photo = getArguments().getParcelable(KEY_PHOTO);
        binding.setSketchPhoto(photo);
        sketchPlacerView = view.findViewById(R.id.sketch_placer);

        Glide.with(view).load(photo.getUri()).into(sketchPlacerView);
        sketchPlacerView.show();

        binding.cancelButton.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClickConfirmButton() {
        Rect rect = sketchPlacerView.getDrawable().getBounds();
        rect.set((int)sketchPlacerView.matrixValues[Matrix.MTRANS_X], (int)sketchPlacerView.matrixValues[Matrix.MTRANS_Y],
                (int)(rect.width() * sketchPlacerView.matrixValues[Matrix.MSCALE_X]), (int)(rect.height() * sketchPlacerView.matrixValues[Matrix.MSCALE_Y]));

        EditText editText = binding.sketchName;
        String sketchName = editText.getText().toString();
        ((MapsActivity) getActivity()).addSketchConfirm(rect, sketchName, binding.getSketchPhoto());
    }

    public void onClickCancelButton() {
        binding.cancelButton.setVisibility(View.INVISIBLE);
        ((MapsActivity) getActivity()).addSketchCancel();
    }
}
