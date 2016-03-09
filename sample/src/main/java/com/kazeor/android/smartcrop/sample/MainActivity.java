package com.kazeor.android.smartcrop.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.kazeor.android.smartcrop.CropResult;
import com.kazeor.android.smartcrop.Frame;
import com.kazeor.android.smartcrop.SmartCrop;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_FILE_SELECTION = 1;

    private static final int MODE_DEBUG = 0;
    private static final int MODE_CROP = 1;

    private ListView listView = null;
    private MenuItem cropMenu = null;
    private MenuItem modeMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new CropInfoAdapter(getApplicationContext(), new ArrayList<CropInfo>()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        cropMenu = menu.findItem(R.id.action_select_aspect);
        modeMenu = menu.findItem(R.id.action_select_mode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                requestFileSelection();
                return true;
            case R.id.label_crop_square:
                updateAspect(CropInfo.CROP_ASPECT.SQUARE);
                return true;
            case R.id.label_crop_landscape:
                updateAspect(CropInfo.CROP_ASPECT.LANDSCAPE);
                return true;
            case R.id.label_crop_portrait:
                updateAspect(CropInfo.CROP_ASPECT.PORTRAIT);
                return true;
            case R.id.label_mode_debug:
                updateMode(MODE_DEBUG);
                return true;
            case R.id.label_mode_crop:
                updateMode(MODE_CROP);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FILE_SELECTION && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                crop(data);
            }
        }
    }

    private void updateAspect(CropInfo.CROP_ASPECT aspect) {
        if (cropMenu == null || listView == null) {
            return;
        }

        int iconId;
        switch (aspect) {
            default:
            case SQUARE:
                iconId = R.drawable.ic_crop_square_light;
                break;
            case LANDSCAPE:
                iconId = R.drawable.ic_crop_landscape_light;
                break;
            case PORTRAIT:
                iconId = R.drawable.ic_crop_portrait_light;
                break;
        }

        cropMenu.setIcon(iconId);
        ((CropInfoAdapter) listView.getAdapter()).setCropAspect(aspect);
        listView.invalidateViews();
    }

    private void updateMode(int mode) {
        if (modeMenu == null) {
            return;
        }

        int iconId;
        switch (mode) {
            default:
            case MODE_DEBUG:
                iconId = R.drawable.ic_adb_white_24dp;
                break;
            case MODE_CROP:
                iconId = R.drawable.ic_crop_white_24dp;
                break;
        }

        modeMenu.setIcon(iconId);
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
        final ProgressDialog progressDialog = ProgressDialog
                .show(MainActivity.this, null, getString(R.string.message_crop_progress));

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
                .map(documentUri -> {
                    String id = DocumentsContract.getDocumentId(documentUri).split(":")[1];
                    Uri uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
                    ContentResolver contentResolver = getContentResolver();

                    // Orientation
                    Frame.Orientation orientation = Frame.Orientation.DEGREE_0;
                    String[] projection = {MediaStore.Images.Media.ORIENTATION};
                    Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                        switch (cursor.getInt(index)) {
                            default:
                            case 0:
                                break;
                            case 90:
                                orientation = Frame.Orientation.DEGREE_90;
                                break;
                            case 180:
                                orientation = Frame.Orientation.DEGREE_180;
                                break;
                            case 270:
                                orientation = Frame.Orientation.DEGREE_270;
                                break;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }

                    // load bitmap
                    Bitmap bitmap = BitmapUtils.createScaledBitmap(contentResolver, uri,
                            BitmapUtils.SIZE_VGA);

                    // crop
                    CropResult cropResult1by1 = null;
                    CropResult cropResult16by9 = null;
                    CropResult cropResult9by16 = null;
                    if (bitmap != null) {
                        SmartCrop smartcrop = new SmartCrop.Builder()
                                .shouldOutputScoreMap()
                                .build();
                        Frame frame = new Frame.Builder()
                                .bitmap(bitmap)
                                .orientation(orientation)
                                .build();
                        cropResult1by1 = smartcrop.crop(frame, 1f);

                        Frame frameWithMap = new Frame.Builder(frame)
                                .scoreMap(cropResult1by1.scoreMap())
                                .build();
                        cropResult16by9 = smartcrop.crop(frameWithMap, 16f / 9f);
                        cropResult9by16 = smartcrop.crop(frameWithMap, 9f / 16f);
                    }

                    CropInfo cropInfo = new CropInfo();
                    cropInfo.mediaId = Long.parseLong(id);
                    cropInfo.orientation = orientation;
                    cropInfo.cropResultSquare = cropResult1by1;
                    cropInfo.cropResultLandscape = cropResult16by9;
                    cropInfo.cropResultPortrait = cropResult9by16;
                    return cropInfo;
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    if (listView != null) {
                        ((CropInfoAdapter) listView.getAdapter()).addAll(results);
                    }
                    progressDialog.dismiss();
                });

    }

}
