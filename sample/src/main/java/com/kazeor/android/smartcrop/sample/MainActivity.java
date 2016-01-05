package com.kazeor.android.smartcrop.sample;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.kazeor.android.smartcrop.SmartCrop;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_FILE_SELECTION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestFileSelection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FILE_SELECTION && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                crop(data);
            }
        }
    }

    private void requestFileSelection() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, REQUEST_CODE_FILE_SELECTION);
    }

    private void crop(final Intent data) {
        Observable
                .create(new Observable.OnSubscribe<Uri>() {
                    @Override
                    public void call(Subscriber<? super Uri> subscriber) {
                        if (data.getData() != null) {
                            subscriber.onNext(data.getData());
                        } else {
                            ClipData clipData = data.getClipData();
                            int num = clipData.getItemCount();
                            for (int i = 0; i < num; i++) {
                                subscriber.onNext(clipData.getItemAt(i).getUri());
                            }
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.computation())
                .map(new Func1<Uri, CropInfo>() {
                    @Override
                    public CropInfo call(Uri documentUri) {
                        String id = DocumentsContract.getDocumentId(documentUri).split(":")[1];
                        Uri uri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));

                        // load bitmap
                        Bitmap bitmap = BitmapUtils.createScaledBitmap(getContentResolver(), uri,
                                BitmapUtils.SIZE_VGA);

                        // crop
                        SmartCrop.CropResult cropResult = null;
                        if (bitmap != null) {
                            SmartCrop smartcrop = new SmartCrop.Builder()
                                    .setDebugFlag(true)
                                    .build();
                            cropResult = smartcrop.crop(bitmap, 1);
                        }

                        CropInfo cropInfo = new CropInfo();
                        cropInfo.mediaId = Long.parseLong(id);
                        cropInfo.cropResult = cropResult;
                        return cropInfo;
                    }
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<CropInfo>>() {
                    @Override
                    public void call(List<CropInfo> results) {
                        ArrayList<CropInfo> array = new ArrayList<>(results);
                        ListView list = (ListView) findViewById(R.id.list);
                        list.setAdapter(new CropInfoAdapter(getApplicationContext(), array));
                    }
                });

    }

}
