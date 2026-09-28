package com.dailyrewards.app;

import android.os.Bundle;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private final Handler handler = new Handler();
    private int attempts = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            InputStream in = getAssets().open("admob-bridge.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            reader.close();
            getBridge().eval(script.toString(), null);
        } catch (Exception e) {
            android.util.Log.e("DailyRewards", "AdMob bridge injection failed", e);
        }
    }
}