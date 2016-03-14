package com.example.dongja94.samplecamera;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongja94 on 2016-03-14.
 */
public class ImageAdapter extends BaseAdapter {
    List<Bitmap> items = new ArrayList<Bitmap>();
    public void add(Bitmap bitmap) {
        items.add(bitmap);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(parent.getContext());
            int width = parent.getContext().getResources().getDimensionPixelSize(R.dimen.photo_width);
            int height = parent.getContext().getResources().getDimensionPixelSize(R.dimen.photo_height);
            imageView.setLayoutParams(new Gallery.LayoutParams(width, height));
        } else {
            imageView = (ImageView)convertView;
        }
        imageView.setImageBitmap(items.get(position));
        return imageView;
    }
}
