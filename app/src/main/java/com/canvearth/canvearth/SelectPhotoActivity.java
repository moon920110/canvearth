package com.canvearth.canvearth;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Contacts;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.canvearth.canvearth.client.Photo;
import com.canvearth.canvearth.client.PhotoAdapter;
import com.canvearth.canvearth.client.PhotoModel;
import com.canvearth.canvearth.client.SketchPlacerFragment;
import com.canvearth.canvearth.client.UI;
import com.canvearth.canvearth.client.VariableGridLayoutManager;
import com.canvearth.canvearth.databinding.ActivitySelectPhotoBinding;
import com.canvearth.canvearth.utils.DatabaseUtils;

import java.util.List;

public class SelectPhotoActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivitySelectPhotoBinding binding = null;
    private VariableGridLayoutManager layoutManager = null;
    private int itemHeight = -1;
    private List<Photo> photoList = null;
    private PhotoModel photoModel = null;
    private PhotoAdapter photoAdapter = new PhotoAdapter() {
        @Override
        public int getItemCount() {
            if (photoList == null) {
                return 0;
            } else {
                return photoList.size();
            }
        }

        @Override
        public Photo getItem(int position) {
            if (photoList == null) {
                return null;
            } else {
                return photoList.get(position);
            }
        }

        @Override
        public void onClickPhoto(View view, int position, Photo photo) {
            Intent intent = new Intent();
            intent.putExtra("photo", photo);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private PhotoModel.OnStatusListener statusListener = new PhotoModel.OnStatusListener() {
        @Override
        public void onLoadStart(PhotoModel photoModel) {
        }

        @Override
        public void onLoadSuccess(PhotoModel photoModel) {
            photoList = photoModel.getPhotosList();
            photoAdapter.notifyDataSetChanged();
            binding.refresh.setRefreshing(false);
        }

        @Override
        public void onLoadFailure(PhotoModel photoModel, Exception e) {
            binding.refresh.setRefreshing(false);
            Log.e("PhotoModel Listener", e.getMessage());
        }
    };

    public static Intent createIntent(Context context) {
        return new Intent(context, SelectPhotoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UI.init(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_photo);
        binding.setHandler(this);

        binding.refresh.setColorSchemeResources(R.color.refresh);
        binding.refresh.setOnRefreshListener(this);

        photoModel = new PhotoModel(this, statusListener);

        int minItemWidth = Math.round(UI.getDisplayWidth() / 4.5f);
        layoutManager = new VariableGridLayoutManager(this, minItemWidth);
        binding.list.setLayoutManager(layoutManager);
        binding.list.addItemDecoration(itemDecoration);
        binding.list.setAdapter(photoAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (photoModel != null) {
            photoModel.handleResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photoModel != null) {
            photoModel.handleDestroy();
            photoModel = null;
        }
    }

    private RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration()
    {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            final int spanCount = layoutManager.getSpanCount();

            if (itemHeight == -1)
            {
                itemHeight = parent.getWidth() / spanCount;
            }

            final int halfSpace = UI.dp2px_r(2f);

            final GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = itemHeight;

            if (layoutParams.getSpanIndex() > 0)
            {
                outRect.left = halfSpace;
            }

            if (layoutParams.getSpanIndex() < spanCount - 1)
            {
                outRect.right = halfSpace;
            }

            final int itemCount = photoAdapter.getItemCount();
            final int itemPosition = parent.getChildAdapterPosition(view);

            if (itemPosition >= spanCount)
            {
                outRect.top = halfSpace;
            }

            if (itemCount > 0 && itemPosition < (itemCount - 1) / spanCount * spanCount)
            {
                outRect.bottom = halfSpace;
            }
            else
            {
                outRect.bottom = UI.dp2px_r(16f) + UI.dp2px_r(16f);
            }
        }
    };

    public void onClickBack() {
        onBackPressed();
    }

    @Override
    public void onRefresh() {
        if (photoModel != null) {
            photoModel.refresh();
        }
    }
}
