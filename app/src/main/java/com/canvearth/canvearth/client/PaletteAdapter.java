package com.canvearth.canvearth.client;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.canvearth.canvearth.R;

public class PaletteAdapter extends BaseAdapter {
    private Context mContext;

    public PaletteAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return paletteColors.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new TextView(mContext);

            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics());
            textView.setLayoutParams(new ViewGroup.LayoutParams(width, width));
            if (position == paletteColors.length - 1) {
                textView.setBackgroundResource(R.drawable.color_transparent);
            } else {
                textView.setBackgroundResource(paletteColors[position]);
            }
        } else {
            textView = (TextView) convertView;
        }

        return textView;
    }

    // references to our images
    public int[] paletteColors = {
            R.color.palette0, R.color.palette1, R.color.palette2, R.color.palette3, R.color.palette4,
            R.color.palette5, R.color.palette6, R.color.palette7, R.color.palette8, R.color.palette9,
            R.color.palette10, R.color.palette11, R.color.palette12, R.color.palette13, R.color.palette14,
            R.color.palette15, R.color.palette16, R.color.palette17, R.color.palette18, R.color.palette19,
            R.color.palette20, R.color.palette21, R.color.palette22, R.color.palette23, R.color.palette24
    };
}