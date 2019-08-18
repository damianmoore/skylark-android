package uk.co.epixstudios.skylark;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;


public class WebviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        String url = getIntent().getStringExtra("URL");
        WebView wv = (WebView) findViewById(R.id.webview);
        wv.loadUrl(url);
    }
}
