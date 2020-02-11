package com.project.crop_prediction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = "CaptureActivity";

    private static File sdcard = Environment.getExternalStorageDirectory();
    private static String picsFolder = "/Pictures/MiniProject/";

    private Bitmap img = null;
    private ImageView imageView = null;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private Map<String, String> details;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        mode = getIntent().getIntExtra("MODE", 0);
    }

    protected void onStart() {
        super.onStart();
        if (img == null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap img = (Bitmap) data.getExtras().get("data");
        setImage(img);
        saveImage(img);
    }

    private void setImage(Bitmap img) {
        this.img = img;
        imageView.setImageBitmap(img);
    }


    //save image in folder (from stack overflow)

    private void saveImage(Bitmap img) {
        try {
            if (!(new File(sdcard, picsFolder).exists()))
                new File(sdcard, picsFolder).mkdirs();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault());
            String dt = sdf.format(new Date());

            File imageFile = new File(sdcard, picsFolder + "IMG-" + dt + ".png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            img.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Image Saved", Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Unable to Save Image" + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


}
