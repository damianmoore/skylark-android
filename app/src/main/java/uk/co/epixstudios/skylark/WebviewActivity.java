package uk.co.epixstudios.skylark;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;


public class WebviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        WebView wv = (WebView) findViewById(R.id.webview);
        wv.setWebViewClient(new WebViewClient());
        wv.clearHistory();
        wv.getSettings().setJavaScriptEnabled(true);

        String url = getIntent().getStringExtra("URL");
        wv.loadUrl(url);
    }
}
