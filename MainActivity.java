package com.dailyrewards.app;

import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdError;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private final Handler handler = new Handler();
    private int attempts = 0;
    private static final String TEST_REWARDED_AD_UNIT =
            "ca-app-pub-3940256099942544/5224354917";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showHKSplash();

        if (getBridge() != null && getBridge().getWebView() != null) {
            WebView webView = getBridge().getWebView();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new AdBridge(), "DailyRewardsAd");
        }

        MobileAds.initialize(this, status -> {});
        scheduleAdBridgeInjection();
    }

    private void showHKSplash() {
        ViewGroup content = findViewById(android.R.id.content);
        if (content == null) return;

        final FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.BLACK);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(32, 32, 32, 32);

        TextView hk = new TextView(this);
        hk.setText("H.K");
        hk.setTextColor(Color.WHITE);
        hk.setTextSize(58);
        hk.setTypeface(Typeface.DEFAULT_BOLD);
        hk.setGravity(Gravity.CENTER);

        TextView name = new TextView(this);
        name.setText("Husain King");
        name.setTextColor(Color.WHITE);
        name.setTextSize(24);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        name.setAlpha(0f);

        box.addView(hk, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        box.addView(name, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        splash.addView(box, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        content.addView(splash, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        ScaleAnimation intro = new ScaleAnimation(
                0.65f, 1f, 0.65f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        intro.setDuration(650);
        intro.setFillAfter(true);
        hk.startAnimation(intro);

        AlphaAnimation nameIn = new AlphaAnimation(0f, 1f);
        nameIn.setStartOffset(500);
        nameIn.setDuration(700);
        nameIn.setFillAfter(true);
        name.startAnimation(nameIn);

        handler.postDelayed(() -> {
            AlphaAnimation out = new AlphaAnimation(1f, 0f);
            out.setDuration(650);
            out.setFillAfter(true);
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    content.removeView(splash);
                }
            });
            splash.startAnimation(out);
        }, 2200);
    }

    private void scheduleAdBridgeInjection() {
        handler.postDelayed(() -> {
            injectAdBridge();
            attempts++;
            if (attempts < 30) {
                scheduleAdBridgeInjection();
            }
        }, 1000);
    }

    private void injectAdBridge() {
        if (getBridge() == null || getBridge().getWebView() == null) return;
        try {
            java.io.InputStream in = getAssets().open("admob-bridge.js");
            java.io.BufferedReader reader =
                    new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"));
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            reader.close();
            getBridge().eval(script.toString(), null);
        } catch (Exception e) {
            android.util.Log.e("DailyRewards", "Ad bridge injection failed", e);
        }
    }

    private class AdBridge {
        @JavascriptInterface
        public void showRewarded() {
            runOnUiThread(() -> {
                AdRequest request = new AdRequest.Builder().build();

                RewardedAd.load(
                        MainActivity.this,
                        TEST_REWARDED_AD_UNIT,
                        request,
                        new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(RewardedAd rewardedAd) {
                                rewardedAd.setFullScreenContentCallback(
                                        new FullScreenContentCallback() {
                                            @Override
                                            public void onAdDismissedFullScreenContent() {
                                            }

                                            @Override
                                            public void onAdFailedToShowFullScreenContent(
                                                    AdError adError) {
                                                notifyJsFailure("Ad show failed: " + adError.getMessage());
                                            }
                                        });

                                rewardedAd.show(
                                        MainActivity.this,
                                        rewardItem -> notifyJsRewarded(rewardItem)
                                );
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                notifyJsFailure(
                                        "Ad load failed [" + error.getCode() + "]: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
            });
        }
    }

    private void notifyJsRewarded(RewardItem rewardItem) {
        if (getBridge() == null || getBridge().getWebView() == null) return;
        getBridge().getWebView().post(() ->
                getBridge().getWebView().evaluateJavascript(
                        "window.__dailyRewardsAdRewarded && window.__dailyRewardsAdRewarded();",
                        null
                )
        );
    }

    private void notifyJsFailure(String message) {
        android.util.Log.e("DailyRewards", message);
        if (getBridge() != null && getBridge().getWebView() != null) {
            String safe = message.replace("\\", "\\\\")
                    .replace("\"", "\\\"");
            getBridge().getWebView().post(() ->
                    getBridge().getWebView().evaluateJavascript(
                            "window.__dailyRewardsAdFailed && window.__dailyRewardsAdFailed(\"" + safe + "\");",
                            null
                    )
            );
        }
        runOnUiThread(() ->
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show()
        );
    }
}
