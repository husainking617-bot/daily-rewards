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
<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>H.K Battle Arena</title>
<style>
*{box-sizing:border-box;touch-action:none}body{margin:0;background:#07101b;color:#fff;font-family:Arial,sans-serif;overflow:hidden}
#app{height:100vh;display:flex;flex-direction:column}header{padding:12px 14px;background:#0d1728;border-bottom:1px solid #263858;display:flex;justify-content:space-between;align-items:center;z-index:3}
.logo{font-size:21px;font-weight:900}.sub{font-size:11px;opacity:.65}.points{font-weight:900}
main{flex:1;overflow:auto;padding:14px 14px 80px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.card{min-height:150px;border:1px solid #2a416b;border-radius:18px;background:#14233c;color:#fff;padding:15px;text-align:left;font-size:17px;font-weight:900}
.card b{display:block;font-size:40px;margin-bottom:14px}.wide{grid-column:1/3;min-height:92px}
.screen{display:none}.screen.on{display:block}.back{border:0;background:transparent;color:#9fc4ff;font-size:16px;padding:0 0 10px}
.panel{background:#101c30;border:1px solid #263b60;border-radius:18px;padding:14px;margin-bottom:12px}.hint{font-size:12px;opacity:.68;line-height:1.45}
button{font:inherit}.action{border:0;border-radius:13px;padding:13px;background:#285db8;color:#fff;font-weight:900;width:100%;margin-top:8px}
#game{position:relative;width:100%;height:calc(100vh - 100px);max-height:700px;background:#163b25;border:2px solid #385a3f;border-radius:18px;overflow:hidden}
canvas{display:block;width:100%;height:100%}.hud{position:absolute;top:8px;left:8px;right:8px;display:flex;justify-content:space-between;pointer-events:none;font-weight:900;text-shadow:0 2px 3px #000}
.joy{position:absolute;left:16px;bottom:22px;width:105px;height:105px;border:2px solid #ffffff55;border-radius:50%;background:#ffffff18}
.knob{position:absolute;width:48px;height:48px;left:26px;top:26px;border-radius:50%;background:#ffffff55}
.fire{position:absolute;right:18px;bottom:24px;width:88px;height:88px;border-radius:50%;border:2px solid #fff8;background:#d33b3b;color:#fff;font-size:18px;font-weight:900}
.sprint{position:absolute;right:115px;bottom:30px;width:60px;height:60px;border-radius:50%;border:1px solid #fff8;background:#ffffff28;color:#fff;font-size:11px}
.mode{display:flex;gap:8px}.mode button{flex:1;border:1px solid #385783;background:#162842;color:#fff;border-radius:12px;padding:10px;font-weight:900}
.nav{position:fixed;bottom:0;left:0;right:0;background:#0b1422;border-top:1px solid #263858;display:flex;gap:7px;padding:8px;z-index:5}.nav button{flex:1;background:#172640;border:0;border-radius:11px;color:#fff;padding:10px;font-weight:900}
</style></head><body><div id="app">
<header><div><div class="logo">⚡ H.K BATTLE ARENA</div><div class="sub">Original battle-royale action</div></div><div class="points">🏆 <span id="points">225</span></div></header>
<main>
<section id="home" class="screen on"><div class="panel"><b>SELECT BATTLE</b><p class="hint">Real-time touch gameplay. Move, aim, shoot, loot and survive. No diamond or real-money purchase system.</p></div>
<div class="grid"><button class="card" onclick="startGame('solo')"><b>🔥</b>SOLO ROYALE<br><small>24 players • bots</small></button>
<button class="card" onclick="startGame('squad')"><b>👥</b>SQUAD ROYALE<br><small>4-player squad</small></button>
<button class="card" onclick="startGame('tdm')"><b>🎯</b>TEAM BATTLE<br><small>Quick combat</small></button>
<button class="card" onclick="startGame('training')"><b>🏹</b>TRAINING<br><small>Practice range</small></button>
<button class="card wide" onclick="watchAd()"><b style="font-size:28px;display:inline">📺</b> WATCH ADS & EARN +10 POINTS</button></div></section>
<section id="battle" class="screen"><button class="back" onclick="leaveGame()">← Back to Home</button>
<div id="game"><canvas id="cv"></canvas><div class="hud"><span id="hp">❤️ 100</span><span id="ammo">🔫 30/120</span><span id="alive">👥 24</span></div>
<div class="joy"><div class="knob" id="knob"></div></div><button class="sprint" id="sprint">RUN</button><button class="fire" id="fire">FIRE</button></div></section>
<section id="history" class="screen"><div class="panel"><h2>Match History</h2><div id="historyLog" class="hint"></div></div></section>
</main><nav class="nav"><button onclick="home()">🏠 Home</button><button onclick="show('history')">📜 History</button><button onclick="watchAd()">📺 Watch Ads</button></nav></div>
<script>
const $=id=>document.getElementById(id);let points=225,mode='solo',running=false,ctx,canvas,raf,last=0,fire=false,joy={x:0,y:0},player,bots=[],bullets=[],loot=[],zone,score=0,kills=0,ammo=30,reserve=120,hp=100;
function save(){ $('points').textContent=points } function add(n,t){points+=n;save();$('historyLog').innerHTML='<p>+'+n+' points — '+t+'</p>'+$('historyLog').innerHTML}
function show(id){document.querySelectorAll('.screen').forEach(x=>x.classList.remove('on'));$(id).classList.add('on')}
function home(){running=false;if(raf)cancelAnimationFrame(raf);show('home')} function leaveGame(){home()}
function watchAd(){window.__dailyRewardsAdRewarded=function(){add(10,'Rewarded ad watched')};window.__dailyRewardsAdFailed=function(m){$('historyLog').innerHTML='<p>'+m+'</p>'+$('historyLog').innerHTML};if(window.DailyRewardsAd)DailyRewardsAd.showRewarded();else add(0,'Ad bridge loading')}
function resize(){canvas.width=canvas.clientWidth*devicePixelRatio;canvas.height=canvas.clientHeight*devicePixelRatio;ctx.setTransform(devicePixelRatio,0,0,devicePixelRatio,0,0)}
function startGame(m){mode=m;show('battle');canvas=$('cv');ctx=canvas.getContext('2d');resize();window.onresize=resize;let w=canvas.clientWidth,h=canvas.clientHeight;player={x:w/2,y:h/2,r:15,ang:0,speed:mode==='training'?3.6:2.8};ammo=30;reserve=120;hp=100;kills=0;score=0;bullets=[];loot=[];bots=[];zone={x:w/2,y:h/2,r:Math.min(w,h)*.43,target:Math.min(w,h)*.18};for(let i=0;i<(mode==='tdm'?10:23);i++)bots.push({x:30+Math.random()*(w-60),y:40+Math.random()*(h-90),r:13,hp:40,cd:Math.random()*80,ang:Math.random()*6.28});for(let i=0;i<10;i++)loot.push({x:25+Math.random()*(w-50),y:30+Math.random()*(h-70),type:Math.random()<.5?'ammo':'med'});running=true;last=performance.now();requestAnimationFrame(loop)}
function draw(){let w=canvas.clientWidth,h=canvas.clientHeight;ctx.clearRect(0,0,w,h);ctx.fillStyle='#28552e';ctx.fillRect(0,0,w,h);ctx.strokeStyle='#356d3d';for(let x=0;x<w;x+=42){ctx.beginPath();ctx.moveTo(x,0);ctx.lineTo(x,h);ctx.stroke()}for(let y=0;y<h;y+=42){ctx.beginPath();ctx.moveTo(0,y);ctx.lineTo(w,y);ctx.stroke()}
ctx.save();ctx.fillStyle='rgba(40,80,180,.16)';ctx.beginPath();ctx.arc(zone.x,zone.y,zone.r,0,7);ctx.fill();ctx.strokeStyle='#7eb5ff';ctx.lineWidth=3;ctx.stroke();ctx.restore();
loot.forEach(l=>{ctx.fillStyle=l.type==='ammo'?'#ffd84a':'#56e08a';ctx.fillRect(l.x-7,l.y-7,14,14)});
bots.forEach(b=>{ctx.fillStyle='#d94b4b';ctx.beginPath();ctx.arc(b.x,b.y,b.r,0,7);ctx.fill();ctx.fillStyle='#222';ctx.fillRect(b.x-9,b.y-20,18,3);ctx.fillStyle='#61e86b';ctx.fillRect(b.x-9,b.y-20,18*Math.max(0,b.hp/40),3)});
bullets.forEach(b=>{ctx.fillStyle='#ffe58a';ctx.beginPath();ctx.arc(b.x,b.y,3,0,7);ctx.fill()});
ctx.save();ctx.translate(player.x,player.y);ctx.rotate(player.ang);ctx.fillStyle='#5da9ff';ctx.beginPath();ctx.arc(0,0,player.r,0,7);ctx.fill();ctx.fillStyle='#e8f2ff';ctx.fillRect(7,-3,22,6);ctx.restore()}
function shoot(){if(!running||ammo<=0){if(reserve>0){ammo=Math.min(30,reserve);reserve-=ammo}else return}ammo--;let a=player.ang;bullets.push({x:player.x+Math.cos(a)*25,y:player.y+Math.sin(a)*25,vx:Math.cos(a)*8,vy:Math.sin(a)*8,life:70});if(navigator.vibrate)navigator.vibrate(20)}
function update(dt){let w=canvas.clientWidth,h=canvas.clientHeight;player.x+=joy.x*player.speed*dt;player.y+=joy.y*player.speed*dt;player.x=Math.max(15,Math.min(w-15,player.x));player.y=Math.max(25,Math.min(h-15,player.y));if(fire)shoot();bots.forEach(b=>{let dx=player.x-b.x,dy=player.y-b.y,d=Math.hypot(dx,dy)||1;b.x+=dx/d*.55*dt;b.y+=dy/d*.55*dt;b.ang=Math.atan2(dy,dx);b.cd-=dt;if(d<230&&b.cd<=0){b.cd=55+Math.random()*50;if(Math.random()<.18)hp-=5}});
bullets.forEach(b=>{b.x+=b.vx*dt;b.y+=b.vy*dt;b.life-=dt;bots.forEach(enemy=>{if(enemy.hp>0&&Math.hypot(b.x-enemy.x,b.y-enemy.y)<18){enemy.hp=0;kills++;score+=10;add(1,'Enemy hit');}})});bullets=bullets.filter(b=>b.life>0);
bots=bots.filter(b=>b.hp>0);if(Math.random()<.002&&zone.r>zone.target)zone.r-=1;if(Math.hypot(player.x-zone.x,player.y-zone.y)>zone.r)hp-=.35*dt;
loot=loot.filter(l=>{if(Math.hypot(player.x-l.x,player.y-l.y)<28){if(l.type==='ammo')reserve+=30;else hp=Math.min(100,hp+25);return false}return true});if(hp<=0){endGame(false)}if(bots.length===0&&mode!=='training'){endGame(true)}
$('hp').textContent='❤️ '+Math.max(0,Math.round(hp));$('ammo').textContent='🔫 '+ammo+'/'+reserve;$('alive').textContent='👥 '+(bots.length+1)}
function endGame(win){running=false;if(raf)cancelAnimationFrame(raf);let reward=mode==='training'?2:(win?25:5);add(reward,(win?'Victory':'Match finished'));setTimeout(()=>alert(win?'🏆 VICTORY! +'+reward+' points':'💀 YOU WERE ELIMINATED — +'+reward+' points'),50)}
function loop(t){if(!running)return;let dt=Math.min(2,(t-last)/16.67);last=t;update(dt);draw();raf=requestAnimationFrame(loop)}
function bind(){let k=$('knob'),j=$('.joy'),rect;function move(e){let p=e.touches?e.touches[0]:e;rect=j.getBoundingClientRect();let dx=p.clientX-(rect.left+52),dy=p.clientY-(rect.top+52),d=Math.min(42,Math.hypot(dx,dy));let a=Math.atan2(dy,dx);joy.x=Math.cos(a)*d/42;joy.y=Math.sin(a)*d/42;k.style.transform='translate('+joy.x*35+'px,'+joy.y*35+'px)';if(Math.abs(joy.x)+Math.abs(joy.y)>.2)player.ang=a}function stop(){joy.x=joy.y=0;k.style.transform='translate(0,0)'}j.addEventListener('touchmove',move);j.addEventListener('touchstart',move);j.addEventListener('touchend',stop);$('fire').addEventListener('touchstart',e=>{e.preventDefault();fire=true});$('fire').addEventListener('touchend',()=>fire=false);$('fire').addEventListener('mousedown',()=>fire=true);$('fire').addEventListener('mouseup',()=>fire=false);$('sprint').addEventListener('touchstart',()=>player.speed=5);$('sprint').addEventListener('touchend',()=>player.speed=mode==='training'?3.6:2.8);document.addEventListener('keydown',e=>{if(e.code==='Space')fire=true;if(e.code==='KeyR'){let n=Math.min(30-ammo,reserve);ammo+=n;reserve-=n}});document.addEventListener('keyup',e=>{if(e.code==='Space')fire=false})}bind();save();
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
