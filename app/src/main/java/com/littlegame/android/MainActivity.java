package com.littlegame.android;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pingstart.adsdk.listener.BannerListener;
import com.pingstart.adsdk.listener.InterstitialListener;
import com.pingstart.adsdk.mediation.PingStartBanner;
import com.pingstart.adsdk.mediation.PingStartInterstitial;

public class MainActivity extends AppCompatActivity {

    private PingStartInterstitial mPingStartInterstitial;
    private PingStartBanner mPingStartBanner;
    private WebView mWebView;
    private FrameLayout mBannerContainer;
    private ProgressBar mProgressBar;
    private long mClickTime;
    private static boolean isInterstitialLoaded, isBannerLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBannerContainer = (FrameLayout) findViewById(R.id.banner_ad_container);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        initWebView();
        if (getResources().getBoolean(R.bool.start_interstitial_ad_switch) && !isInterstitialLoaded) {
            loadInterstitialAd();
        }

        if (getResources().getBoolean(R.bool.banner_ad_switch) && !isBannerLoaded) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadBannerAd();
                }
            }, 30 * 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getResources().getBoolean(R.bool.is_game_landscape) && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void initWebView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mWebView.loadUrl(getString(R.string.game_url));
    }

    private void loadInterstitialAd() {
        mPingStartInterstitial = new PingStartInterstitial(this, getString(R.string.interstitial_pingstart_appid), getString(R.string.interstitial_pingstart_slotid));
        mPingStartInterstitial.setAdListener(new InterstitialListener() {
            @Override
            public void onAdClosed() {
                if (mPingStartInterstitial != null) {
                    mPingStartInterstitial.destroy();
                    mPingStartInterstitial = null;
                }
                isInterstitialLoaded = false;
            }

            @Override
            public void onAdError(String error) {
                Log.i("Lebron", "  interError" + error);
            }

            @Override
            public void onAdLoaded() {
                Log.i("Lebron", "Inter onAdLoaded");
                mPingStartInterstitial.showAd();
            }

            @Override
            public void onAdClicked() {
                Log.i("Lebron", "interClick");
                isInterstitialLoaded = false;
            }
        });
        mPingStartInterstitial.loadAd();
        isInterstitialLoaded = true;
    }

    private void loadBannerAd() {
        mPingStartBanner = new PingStartBanner(this, getString(R.string.banner_pingstart_appid), getString(R.string.banner_pingstart_slotid));
        mPingStartBanner.setAdListener(new BannerListener() {
            @Override
            public void onAdLoaded(View view) {
                Log.i("Lebron", " onAdLoaded ");
                if (view != null) {
                    mBannerContainer.removeAllViews();
                    mBannerContainer.addView(view);
                    mBannerContainer.setVisibility(View.VISIBLE);
                }
                isBannerLoaded = false;
            }

            @Override
            public void onAdError(String s) {
                Log.i("Lebron", "onAdError " + s);
            }

            @Override
            public void onAdClicked() {
                mBannerContainer.setVisibility(View.GONE);
                isBannerLoaded = false;
            }
        });
        mPingStartBanner.loadBanner();
        isBannerLoaded = true;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mClickTime) > 3000) {
            if (getResources().getBoolean(R.bool.end_interstitial_ad_switch) && !isInterstitialLoaded) {
                loadInterstitialAd();
            }
            Toast.makeText(getApplicationContext(), "Tap to exit", Toast.LENGTH_SHORT).show();
            mClickTime = System.currentTimeMillis();
        } else {
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPingStartInterstitial != null) {
            mPingStartInterstitial.destroy();
        }
        if (mPingStartBanner != null) {
            mPingStartBanner.destroy();
            mPingStartBanner = null;
        }
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }
}
