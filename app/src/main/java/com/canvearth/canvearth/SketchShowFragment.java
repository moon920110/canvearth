package com.canvearth.canvearth;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.canvearth.canvearth.databinding.FragmentSketchPlacerBinding;
import com.canvearth.canvearth.databinding.FragmentSketchshowListBinding;
import com.canvearth.canvearth.sketch.Sketch;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSketchShowFragmentInteractionListener}
 * interface.
 */
public class SketchShowFragment extends Fragment {
    private OnSketchShowFragmentInteractionListener mListener;
    private MysketchShowRecyclerViewAdapter mysketchShowRecyclerViewAdapter;
    private FragmentSketchshowListBinding binding = null;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SketchShowFragment() {
    }

    // TODO: Customize parameter initialization
    public static SketchShowFragment newInstance() {
        SketchShowFragment fragment = new SketchShowFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sketchshow_list, container, false);
        binding.setHandler(this);

        Context context = binding.list.getContext();
        RecyclerView recyclerView = binding.list;
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mysketchShowRecyclerViewAdapter);
        return binding.getRoot();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSketchShowFragmentInteractionListener) {
            mListener = (OnSketchShowFragmentInteractionListener) context;
            mysketchShowRecyclerViewAdapter = new MysketchShowRecyclerViewAdapter(mListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSketchShowFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setSketches(List<Sketch> sketches) {
        mysketchShowRecyclerViewAdapter.setSketches(sketches);
    }

    public int addSketch(Sketch sketch) {
        return mysketchShowRecyclerViewAdapter.addSketch(sketch);
    }

    public void changeSketch(int idx, Sketch sketch) {
        mysketchShowRecyclerViewAdapter.changeSketch(idx ,sketch);
    }

    public void onClickHide() {
        getView().setVisibility(View.GONE);
        try {
            ((MapsActivity) getActivity()).detachSelectedShowingSketch();
        } catch (Exception e){
            e.printStackTrace();
        }
        ((MapsActivity)getActivity()).showAllComponents();
    }

    public void onClickAddInterest() {
        getView().setVisibility(View.GONE);
        ((MapsActivity)getActivity()).addSelectedToMyInterest();
    }

    public void removeProgressForAll() {
        binding.sketchShowProgressBarForAll.setVisibility(View.GONE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSketchShowFragmentInteractionListener {
        void onSketchShowFragmentInteraction(Sketch item);
    }
}
