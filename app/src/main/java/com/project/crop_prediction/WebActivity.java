package com.project.crop_prediction;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class WebActivity extends AppCompatActivity {

    private WebView webView;
    private String helpDocURL = "https://firebasestorage.googleapis.com/v0/b/rurathon-cvr-2019.appspot.com/o/docs%2Findex.html?alt=media&token=de3d6e96-6c80-4ddc-a900-ca2a7033be30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        setTitle("Help");

        webView = findViewById(R.id.webView);
        webView.loadUrl(helpDocURL);
    }
}
