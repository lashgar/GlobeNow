package myapps.globenow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
    private WebView m_webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);
        m_webView = findViewById(R.id.webview);
        m_webView.setWebViewClient(new WebViewClient());
        Intent intent = getIntent();
        String url = intent.getStringExtra("urlToShow");
        // Enable javascript: Should not be any vulnerability concern
        // as the targets are only a limited set of domains: Twitter or Instagram atm
        m_webView.getSettings().setJavaScriptEnabled(true);
        m_webView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (m_webView.canGoBack()) {
                        m_webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
