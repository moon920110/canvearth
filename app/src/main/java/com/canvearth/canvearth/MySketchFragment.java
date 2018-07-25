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

import com.canvearth.canvearth.databinding.FragmentMysketchListBinding;
import com.canvearth.canvearth.sketch.Sketch;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnMySketchFragmentInteractionListener}
 * interface.
 */
public class MySketchFragment extends Fragment {

    private OnMySketchFragmentInteractionListener mListener;
    private MyMySketchRecyclerViewAdapter myMySketchRecyclerViewAdapter;
    private FragmentMysketchListBinding binding = null;

    public MySketchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mysketch_list, container, false);
        binding.setHandler(this);

        Context context = binding.mysketchList.getContext();
        RecyclerView recyclerView = binding.mysketchList;
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(myMySketchRecyclerViewAdapter);
        return binding.getRoot();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMySketchFragmentInteractionListener) {
            mListener = (OnMySketchFragmentInteractionListener) context;
            myMySketchRecyclerViewAdapter = new MyMySketchRecyclerViewAdapter(mListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMySketchFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setSketches(List<Sketch> sketches) {
        myMySketchRecyclerViewAdapter.setSketches(sketches);
    }

    public int addSketch(Sketch sketch) {
        return myMySketchRecyclerViewAdapter.addSketch(sketch);
    }

    public void changeSketch(int idx, Sketch sketch) {
        myMySketchRecyclerViewAdapter.changeSketch(idx ,sketch);
    }

    public void onClickHide() {
        getView().setVisibility(View.INVISIBLE);
        ((MapsActivity)getActivity()).showAllComponents();
    }

    public void removeProgressForAll() {
        binding.sketchShowProgressBarForMyInterest.setVisibility(View.GONE);
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
    public interface OnMySketchFragmentInteractionListener {
        void onMySketchFragmentInteraction(Sketch item);
    }
}
