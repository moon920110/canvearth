package com.canvearth.canvearth.client;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public final class VariableGridLayoutManager extends GridLayoutManager
{
    private int m_minItemWidth;

    public VariableGridLayoutManager(Context context, int minItemWidth)
    {
        super(context, 1);
        m_minItemWidth = minItemWidth;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        updateSpanCount();
        super.onLayoutChildren(recycler, state);
    }

    private void updateSpanCount()
    {
        final int spanCount = Math.max(getWidth() / m_minItemWidth, 1);
        setSpanCount(spanCount);
    }
}