package com.kazeor.android.smartcrop.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class CropInfoAdapter extends ArrayAdapter<CropInfo> {

    public CropInfoAdapter(Context context, ArrayList<CropInfo> infoList) {
        super(context, 0, infoList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new Holder();
            holder.imageLeft = (ImageView) convertView.findViewById(R.id.imageLeft);
            holder.imageRight = (ImageView) convertView.findViewById(R.id.imageRight);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        CropInfo cropInfo = getItem(position);
        Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(
                getContext().getContentResolver(),
                cropInfo.mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null);

        holder.imageLeft.setImageBitmap(thumb);
        holder.imageRight.setImageBitmap(cropInfo.cropResult.debugBitmap);
        return convertView;
    }

    class Holder {
        ImageView imageLeft;
        ImageView imageRight;
    }

}
