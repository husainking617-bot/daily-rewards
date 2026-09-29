package com.husainking.phonerecovery;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    EditText email, phone, imei;
    TextView result;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Stolen Phone Recovery");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Lost/stolen phone recovery ke liye official tools. Gmail/number/IMEI ko bina authorized access ke kisi phone ki live location nikalne ke liye use nahi kiya ja sakta.");
        info.setPadding(0, 20, 0, 20);
        root.addView(info);

        email = field("Lost phone ka Gmail");
        phone = field("Lost phone ka mobile number");
        imei = field("Lost phone ka IMEI");
        root.addView(email); root.addView(phone); root.addView(imei);

        Button findHub = new Button(this);
        findHub.setText("LOCATE STOLEN PHONE — GOOGLE FIND HUB");
        root.addView(findHub);

        Button recovery = new Button(this);
        recovery.setText("Recover Google Account");
        root.addView(recovery);

        Button ceir = new Button(this);
        ceir.setText("CEIR — Block / Trace IMEI");
        root.addView(ceir);

        Button help = new Button(this);
        help.setText("Lost Phone Recovery Guide");
        root.addView(help);

        result = new TextView(this);
        result.setTextSize(16);
        result.setPadding(0, 24, 0, 0);
        root.addView(result);

        findHub.setOnClickListener(v -> openFindHub());
        recovery.setOnClickListener(v -> openRecovery());
        ceir.setOnClickListener(v -> openCeir());
        help.setOnClickListener(v -> showGuide());
        setContentView(root);
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setPadding(0, 12, 0, 12);
        return e;
    }

    void openFindHub() {
        String account = email.getText().toString().trim();
        result.setText(TextUtils.isEmpty(account)
                ? "Google Find Hub khul raha hai. Lost phone wale Google account se sign in karein."
                : "Find Hub khul raha hai. " + account + " wale account se sign in karein.");
        openUrl("https://android.com/find");
    }

    void openRecovery() {
        openUrl("https://accounts.google.com/signin/recovery");
    }

    void openCeir() {
        String id = imei.getText().toString().trim();
        result.setText(TextUtils.isEmpty(id)
                ? "CEIR khul raha hai. IMEI available ho to lost/stolen phone block/trace process follow karein."
                : "CEIR khul raha hai. Entered IMEI: " + id);
        openUrl("https://ceir.sancharsaathi.gov.in/");
    }

    void showGuide() {
        result.setText("1. Lost phone ka Gmail enter karein.\n" +
                "2. Google Find Hub kholen aur authorized account se sign in karein.\n" +
                "3. Redmi 12C/device select karein.\n" +
                "4. Agar Google location available hai, map par device location/status dikhega.\n" +
                "5. Password yaad nahi ho to Recover Google Account use karein.\n" +
                "6. IMEI ho to CEIR par block/trace process follow karein.\n\n" +
                "Ye app Google security ko bypass nahi karta aur bina authorization kisi device ko secretly track nahi karta.");
    }

    void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            result.setText("Browser open nahi ho paya. Internet/browser check karein.");
        }
    }
}
