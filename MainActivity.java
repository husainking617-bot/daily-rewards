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
            injectGameHome();
            attempts++;
            if (attempts < 30) {
                scheduleAdBridgeInjection();
            }
        }, 1000);
    }

    private void injectGameHome() {
        if (getBridge() == null || getBridge().getWebView() == null) return;
        getBridge().getWebView().post(() -> {
            String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>" +
                    "body{margin:0;background:#0b1020;color:#fff;font-family:Arial,sans-serif}" +
                    ".wrap{padding:22px 16px 90px}.brand{font-size:30px;font-weight:800}.sub{opacity:.7;margin:4px 0 18px}" +
                    ".balance{background:#171f38;border-radius:18px;padding:18px;margin-bottom:18px}.points{font-size:28px;font-weight:800}" +
                    ".grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.card{border:0;border-radius:18px;padding:20px 12px;text-align:left;color:#fff;background:#202a48;font-size:18px;font-weight:700}.emoji{font-size:34px;display:block;margin-bottom:12px}.wide{grid-column:1/3}.nav{position:fixed;bottom:0;left:0;right:0;background:#12182b;padding:12px;display:flex;gap:8px}.nav button{flex:1;border:0;border-radius:12px;padding:12px;background:#273150;color:#fff;font-weight:700}.watch{background:#283d68}.msg{margin-top:16px;opacity:.75;font-size:13px}" +
                    "</style></head><body><div class='wrap'><div class='brand'>Reward Arena</div><div class='sub'>Play. Earn Rewards. Enjoy.</div>" +
                    "<div class='balance'><div>Rewards Balance</div><div class='points' id='pts'>225</div></div>" +
                    "<div class='grid'><button class='card' onclick='game("Cricket")'><span class='emoji'>🏏</span>Cricket</button>" +
                    "<button class='card' onclick='game("Football")'><span class='emoji'>⚽</span>Football</button>" +
                    "<button class='card' onclick='game("Battle Royale")'><span class='emoji'>🔥</span>Battle Royale</button>" +
                    "<button class='card' onclick='game("Battle Royale 2")'><span class='emoji'>🎮</span>Battle Royale 2</button>" +
                    "<button class='card wide watch' onclick='watch()'><span class='emoji'>📺</span>Watch Ads & Earn</button></div>" +
                    "<div class='msg' id='msg'>Games are being built in stages. Rewards and Watch Ads stay active.</div></div>" +
                    "<div class='nav'><button onclick='home()'>Home</button><button onclick='withdraw()'>Withdraw</button><button onclick='historyPage()'>History</button></div>" +
                    "<script>let p=225;function watch(){document.getElementById('msg').innerText='Ad starting...';window.__dailyRewardsAdRewarded=function(){p+=10;document.getElementById('pts').innerText=p;document.getElementById('msg').innerText='Reward added: +10';};window.__dailyRewardsAdFailed=function(m){document.getElementById('msg').innerText=m;};window.DailyRewardsAd&&DailyRewardsAd.showRewarded();}" +
                    "function game(n){document.getElementById('msg').innerText=n+' mode selected. Full playable version is being added next.';}" +
                    "function home(){location.reload()}function withdraw(){document.getElementById('msg').innerText='Withdraw section ready for the existing rewards balance.'}function historyPage(){document.getElementById('msg').innerText='Reward history will appear here.'}</script></body></html>";
            String js = "document.open();document.write(" + org.json.JSONObject.quote(html) + ");document.close();";
            getBridge().eval(js, null);
        });
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
