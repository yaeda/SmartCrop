package com.kazeor.android.smartcrop.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kazeor.android.smartcrop.CropResult;
import com.kazeor.android.smartcrop.Frame;
import com.kazeor.android.smartcrop.sample.view.ResultView;

import java.util.ArrayList;

public class CropInfoAdapter extends ArrayAdapter<CropInfo> {

    private CropInfo.CROP_ASPECT aspect = CropInfo.CROP_ASPECT.SQUARE;

    public CropInfoAdapter(Context context, ArrayList<CropInfo> infoList) {
        super(context, 0, infoList);
    }

    public void setCropAspect(CropInfo.CROP_ASPECT aspect) {
        this.aspect = aspect;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new Holder();
            holder.imageLeft = (ResultView) convertView.findViewById(R.id.imageLeft);
            holder.imageRight = (ImageView) convertView.findViewById(R.id.imageRight);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        CropInfo cropInfo = getItem(getCount() - 1 - position);
        Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(
                getContext().getContentResolver(),
                cropInfo.mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null);

        CropResult cropResult;
        Bitmap scoreBitmap;
        switch (aspect) {
            default:
            case SQUARE:
                cropResult = cropInfo.cropResultSquare;
                scoreBitmap = cropInfo.cropResultSquare.scoreMap();
                break;
            case LANDSCAPE:
                cropResult = cropInfo.cropResultLandscape;
                scoreBitmap = cropInfo.cropResultLandscape.scoreMap();
                break;
            case PORTRAIT:
                cropResult = cropInfo.cropResultPortrait;
                scoreBitmap = cropInfo.cropResultPortrait.scoreMap();
                break;
        }

        if (cropInfo.orientation == Frame.Orientation.DEGREE_0) {
            holder.imageLeft.setImageBitmap(thumb);
            holder.imageRight.setImageBitmap(scoreBitmap);
        } else {
            holder.imageLeft.setImageBitmap(
                    BitmapUtils.rotateBitmap(thumb, cropInfo.orientation.getDegree())
            );
            thumb.recycle();
            holder.imageRight.setImageBitmap(
                    BitmapUtils.rotateBitmap(scoreBitmap, cropInfo.orientation.getDegree())
            );
        }
        holder.imageLeft.setCropResult(cropResult);
        return convertView;
    }

    class Holder {
        ResultView imageLeft;
        ImageView imageRight;
    }

}
