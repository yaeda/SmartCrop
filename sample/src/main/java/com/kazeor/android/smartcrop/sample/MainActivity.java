package com.kazeor.android.smartcrop.sample;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kazeor.android.smartcrop.SmartCrop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_FILE_SELECTION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPick = (Button) findViewById(R.id.btn_pick);
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFileSelection();
            }
        });
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, REQUEST_CODE_FILE_SELECTION);
    }

    private void crop(Intent data) {
        if (data.getData() == null) {
            return;
        }

        Uri documentUri = data.getData();
        String id = DocumentsContract.getDocumentId(documentUri).split(":")[1];
        Uri uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));

        // load bitmap
        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, null);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // crop
        SmartCrop.Result result = null;
        if (bitmap != null) {
            SmartCrop smartcrop = new SmartCrop();
            result = smartcrop.crop(bitmap, 1);
        }

        // show result
        if (result != null && result.topCrop != null) {
            Log.d(TAG, result.topCrop.x + ", " + result.topCrop.y + ", " +
                    result.topCrop.width + ", " + result.topCrop.height);
        }
    }

}
