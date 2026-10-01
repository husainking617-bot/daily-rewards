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
        }, 900);
    }

    private void injectGameHome() {
        if (getBridge() == null || getBridge().getWebView() == null) return;
        WebView w = getBridge().getWebView();
        w.post(() -> w.loadDataWithBaseURL("https://reward-arena.local/", gameHtml(), "text/html", "UTF-8", null));
    }

    private String gameHtml() {
        return """
<!doctype html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>Reward Arena</title>
<style>
*{box-sizing:border-box}body{margin:0;background:#080d19;color:#fff;font-family:Arial,sans-serif}
header{padding:20px 16px 12px;background:linear-gradient(135deg,#101b38,#18264a)}
h1{margin:0;font-size:28px}.sub{opacity:.7;margin-top:4px}.bal{margin-top:14px;padding:14px;border-radius:16px;background:#111a30;border:1px solid #26365f;font-weight:700}
main{padding:14px 14px 88px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.card{background:#17223b;border:1px solid #2b3c66;color:#fff;border-radius:18px;padding:18px 12px;text-align:left;min-height:125px;font-size:17px;font-weight:800}
.card span{display:block;font-size:34px;margin-bottom:12px}.wide{grid-column:1/3}
.screen{display:none}.screen.on{display:block}.back{background:none;border:0;color:#9fc1ff;font-size:16px;padding:4px 0 14px}
.panel{background:#111a2d;border:1px solid #26365a;border-radius:18px;padding:16px;margin-bottom:12px}
button{font:inherit}.action{width:100%;border:0;border-radius:13px;padding:13px;background:#315db7;color:white;font-weight:800;margin-top:10px}
.row{display:flex;gap:8px}.row button{flex:1}.score{font-size:30px;font-weight:900;text-align:center}.hint{opacity:.7;font-size:13px;line-height:1.45}
#field{height:310px;border-radius:18px;background:linear-gradient(#1f6a38,#18582e);position:relative;overflow:hidden;border:3px solid #d9e8d2}
#pitch{position:absolute;left:15%;right:15%;top:20px;bottom:20px;border:2px solid rgba(255,255,255,.7);border-radius:45%}
.player{position:absolute;width:28px;height:28px;border-radius:50%;background:#f1c27d;border:3px solid #fff}
.ball{position:absolute;width:12px;height:12px;border-radius:50%;background:#fff;left:50%;top:50%}
.goal{position:absolute;top:34%;height:32%;width:8px;background:#fff}.g1{left:0}.g2{right:0}
.choice{padding:12px;border:1px solid #334b7d;border-radius:12px;background:#17233d;color:#fff;flex:1;font-weight:800}
.choices{display:flex;gap:8px}.feed{max-height:145px;overflow:auto;font-size:13px;opacity:.85}.log{padding:5px 0;border-bottom:1px solid #23304b}
.nav{position:fixed;bottom:0;left:0;right:0;background:#0d1425;border-top:1px solid #24324f;display:flex;padding:9px;gap:8px;z-index:5}.nav button{flex:1;background:#182542;color:#fff;border:0;border-radius:12px;padding:11px;font-weight:800}
</style></head>
<body>
<header><h1>Reward Arena</h1><div class="sub">Play • Earn Rewards • Have Fun</div><div class="bal">🏆 Rewards: <b id="points">225</b> points</div></header>
<main>
<section id="home" class="screen on"><div class="grid">
<button class="card" onclick="openGame('cricket')"><span>🏏</span>Cricket</button>
<button class="card" onclick="openGame('football')"><span>⚽</span>Football</button>
<button class="card" onclick="openGame('br')"><span>🔥</span>Battle Royale</button>
<button class="card" onclick="openGame('br2')"><span>🎮</span>Battle Royale 2</button>
<button class="card wide" onclick="watchAd()"><span>📺</span>Watch Ads & Earn</button>
</div><div class="panel" style="margin-top:12px"><b>Game modes</b><p class="hint">Original gameplay inspired by popular sports and battle-royale formats. Real-world teams, players and logos are not included unless licensed.</p></div></section>

<section id="cricket" class="screen"><button class="back" onclick="home()">← Home</button><div class="panel"><h2>🏏 Cricket Challenge</h2><div class="score" id="cscore">0/0</div><p class="hint">6-ball over • choose your shot. Score runs by timing the shot.</p><div class="choices"><button class="choice" onclick="shot(1)">Defend</button><button class="choice" onclick="shot(2)">Drive</button><button class="choice" onclick="shot(3)">Loft</button></div><div class="feed" id="cfeed"></div></div></section>

<section id="football" class="screen"><button class="back" onclick="home()">← Home</button><div class="panel"><h2>⚽ Football Match</h2><div class="score" id="fscore">0 - 0</div><p class="hint">Attack through the pitch. Each possession gives a chance to shoot, pass or dribble.</p><div id="field"><div class="pitch"></div><div class="goal g1"></div><div class="goal g2"></div><div class="player" style="left:18%;top:45%"></div><div class="player" style="left:48%;top:28%"></div><div class="player" style="left:70%;top:58%"></div><div class="ball"></div></div><div class="choices" style="margin-top:10px"><button class="choice" onclick="football('pass')">Pass</button><button class="choice" onclick="football('dribble')">Dribble</button><button class="choice" onclick="football('shoot')">Shoot</button></div><div class="feed" id="ffeed"></div></div></section>

<section id="br" class="screen"><button class="back" onclick="home()">← Home</button><div class="panel"><h2>🔥 Battle Royale</h2><div class="score" id="brscore">Alive: 24</div><p class="hint">Original top-down survival mode. Move, loot, fight bots and survive the shrinking zone.</p><div class="choices"><button class="choice" onclick="brAction('loot')">Loot</button><button class="choice" onclick="brAction('move')">Move</button><button class="choice" onclick="brAction('fight')">Fight</button></div><div class="feed" id="brfeed"></div></div></section>

<section id="br2" class="screen"><button class="back" onclick="home()">← Home</button><div class="panel"><h2>🎮 Battle Royale 2</h2><div class="score" id="br2score">Squad: 4</div><p class="hint">Squad survival mode with armor, medkits and tactical choices. No ads during gameplay.</p><div class="choices"><button class="choice" onclick="br2('heal')">Medkit</button><button class="choice" onclick="br2('armor')">Armor</button><button class="choice" onclick="br2('rush')">Rush</button></div><div class="feed" id="br2feed"></div></div></section>

<section id="withdraw" class="screen"><div class="panel"><h2>Withdraw</h2><p>Current rewards: <b id="wp">225</b> points.</p><p class="hint">Withdrawal options can be connected to your chosen payout provider after its eligibility and verification rules are configured.</p></div></section>
<section id="history" class="screen"><div class="panel"><h2>History</h2><div id="historyLog" class="feed"><div class="log">Starting balance: 225 points</div></div></div></section>
</main>
<nav class="nav"><button onclick="home()">Home</button><button onclick="show('withdraw')">Withdraw</button><button onclick="show('history')">History</button></nav>
<script>
let points=225,cRuns=0,cBalls=0,f1=0,f2=0,alive=24,squad=4;
const $=id=>document.getElementById(id);
function save(){ $('points').textContent=points;$('wp').textContent=points; }
function add(n,why){points+=n;save();$('historyLog').innerHTML='<div class="log">+'+n+' points — '+why+'</div>'+$('historyLog').innerHTML;}
function show(id){document.querySelectorAll('.screen').forEach(x=>x.classList.remove('on'));$(id).classList.add('on');}
function home(){show('home')}
function openGame(id){show(id)}
function log(id,t){$(id).innerHTML='<div class="log">'+t+'</div>'+$(id).innerHTML}
function watchAd(){ $('historyLog').innerHTML='<div class="log">Rewarded ad requested</div>'+$('historyLog').innerHTML; window.__dailyRewardsAdRewarded=function(){add(10,'Watched rewarded ad')};window.__dailyRewardsAdFailed=function(m){log('historyLog',m)}; if(window.DailyRewardsAd)DailyRewardsAd.showRewarded();else log('historyLog','Ad bridge is loading; try again.')}
function shot(type){if(cBalls>=6){cBalls=0;cRuns=0;log('cfeed','New over started');}let r=Math.random();let runs=r<.12?6:r<.32?4:r<.62?2:r<.9?1:0;if(type===3&&r<.2)runs=6;if(type===1&&runs>=4)runs=1;cRuns+=runs;cBalls++;$('cscore').textContent=cRuns+'/'+cBalls;log('cfeed','Ball '+cBalls+': '+(runs?runs+' run(s)':'dot ball'));if(cBalls===6){add(5,'Completed cricket over');log('cfeed','Over complete — +5 points');}}
function football(a){let r=Math.random();if(a==='shoot'&&r>.55){f1++;add(4,'Football goal');log('ffeed','GOAL! Your team scores.');}else if(a==='pass'&&r>.25){log('ffeed','Great pass — attack continues.');}else if(a==='dribble'&&r>.45){log('ffeed','Dribble beats a defender.');}else{f2++;log('ffeed','Opponent wins the ball.');}$('fscore').textContent=f1+' - '+f2;}
function brAction(a){if(alive<=1){log('brfeed','You survived the match.');return}let r=Math.random();if(a==='loot'){add(2,'Battle Royale loot');log('brfeed','Loot found: +2 points.');}else if(a==='move'){alive=Math.max(1,alive-Math.ceil(Math.random()*3));log('brfeed','Zone closes. Alive: '+alive);}else{if(r>.35){alive--;add(3,'Battle Royale elimination');log('brfeed','Enemy eliminated. +3 points.');}else{log('brfeed','Fight survived; repositioning.');}}$('brscore').textContent='Alive: '+alive;}
function br2(a){if(a==='heal'){squad=Math.min(4,squad+1);log('br2feed','Medkit used. Squad: '+squad);}else if(a==='armor'){add(2,'Armor objective');log('br2feed','Armor secured. +2 points.');}else{if(Math.random()>.4){add(4,'Squad encounter');log('br2feed','Squad wins encounter. +4 points.')}else{ squad=Math.max(1,squad-1);log('br2feed','Teammate knocked. Squad: '+squad);}}$('br2score').textContent='Squad: '+squad;}
save();
</script></body></html>
""";
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
