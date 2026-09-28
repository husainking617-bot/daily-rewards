package com.dailyrewards.app;

import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

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

        if (getBridge() != null && getBridge().getWebView() != null) {
            WebView webView = getBridge().getWebView();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new AdBridge(), "DailyRewardsAd");
        }

        MobileAds.initialize(this, status -> {});
        scheduleAdBridgeInjection();
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
                                                // A new ad will be loaded on the next tap.
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
                    .replace(""", "\\"");
            getBridge().getWebView().post(() ->
                    getBridge().getWebView().evaluateJavascript(
                            "window.__dailyRewardsAdFailed && window.__dailyRewardsAdFailed("
                                    + """ + safe + """ + ");",
                            null
                    )
            );
        }
        runOnUiThread(() ->
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show()
        );
    }
}
