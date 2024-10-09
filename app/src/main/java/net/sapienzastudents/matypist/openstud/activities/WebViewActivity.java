package net.sapienzastudents.matypist.openstud.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class WebViewActivity extends BaseDataActivity {

    @BindView(R.id.webview)
    WebView webView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progressBar)
    MaterialProgressBar progressBar;
    WebViewClient client;
    private boolean javascriptInjected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setupWebView(getIntent());
        if (savedInstanceState == null) {
            String url = getIntent().getExtras().getString("url", null);
            if (url != null) webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(Intent intent) {
        Bundle bdl = intent.getExtras();
        String title = bdl.getString("title");
        String subtitle = bdl.getString("subtitle", null);
        boolean clearCookies = bdl.getBoolean("clearCookies", true);
        int type = bdl.getInt("webviewType");
        setTitle(title);
        if (subtitle != null) toolbar.setSubtitle(subtitle);
        client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("intent://") || url.startsWith("tg:resolve")) {
                    try {
                        Context context = view.getContext();
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            view.stopLoading();

                            if (!url.startsWith("tg:resolve")) {
                                PackageManager packageManager = context.getPackageManager();
                                ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

                                if (info != null) {
                                    context.startActivity(intent);
                                } else {
                                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                    if (fallbackUrl == null || fallbackUrl.isEmpty())
                                        fallbackUrl = intent.getStringExtra("link");
                                    if (fallbackUrl != null && !fallbackUrl.isEmpty())
                                        view.loadUrl(fallbackUrl);
                                }
                                return true;
                            } else {
                                intent.setPackage("org.telegram.messenger");
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex1) {
                                    intent.setPackage("org.telegram.messenger.web");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex2) {
                                        Toast.makeText(view.getContext(), context.getResources().getString(R.string.telegram_not_found), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                return true;
                            }
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (progressBar.getVisibility() != View.VISIBLE)
                    progressBar.setVisibility(View.VISIBLE);
                handleLoading(webView, url, type);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.INVISIBLE);
                inject(view, url, type);
            }

        };
        if (clearCookies) InfoManager.clearCookies();
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(client);
    }

    private void handleLoading(WebView view, String url, int type) {
        switch (os.getProvider()) {
            case SAPIENZA:
                if (type == ClientHelper.WebViewType.EMAIL.getValue()) {
                    if (url.startsWith("https://login.studenti.uniroma1.it") && !url.contains("logout")) {
                        view.setVisibility(View.INVISIBLE);
                    } else if (url.contains("logout")) view.setVisibility(View.INVISIBLE);
                } else view.setVisibility(View.VISIBLE);
                break;

            // Right now only Sapienza is supported
            default:
                throw new IllegalArgumentException("Provider not supported");
        }
    }

    private void inject(WebView view, String url, int type) {
        switch (os.getProvider()) {
            case SAPIENZA:
                if (type == ClientHelper.WebViewType.EMAIL.getValue()) {
                    if (url.startsWith("https://login.studenti.uniroma1.it") && !url.contains("logout")) {
                        if (!javascriptInjected) {
                            view.loadUrl(
                                    "javascript:(function() { " +
                                            "setTimeout(function(){" +
                                            "var studentid = document.getElementById('username');"
                                            + "var password = document.getElementById('password');"
                                            + "var login = document.getElementsByName('samlButton');"
                                            + "if (password == undefined || studentid == undefined || login == undefined || login.length == 0) return;"
                                            + "studentid.value = '" + student.getStudentID() + "';"
                                            + "password.value = '" + os.getStudentPassword() + "';"
                                            + "login[0].click();" +
                                            "}, 100)})()");
                        }
                        javascriptInjected = true;
                    } else if (url.contains("logout")) onBackPressed();
                    else view.setVisibility(View.VISIBLE);
                }
                break;

            // Right now only Sapienza is supported
            default:
                throw new IllegalArgumentException("Provider not supported");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}
