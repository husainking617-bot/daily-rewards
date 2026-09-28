package com.husainking.phonerecovery;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
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
        title.setText("Phone Recovery");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Apne khud ke phone/device ke liye details bharein. Number, Gmail ya IMEI se kisi aur phone ki live location automatically nahi milti.");
        info.setPadding(0,20,0,20);
        root.addView(info);

        email = field("Gmail ID (optional)");
        phone = field("Mobile number (optional)");
        imei = field("IMEI / box code (optional)");
        root.addView(email); root.addView(phone); root.addView(imei);

        Button locate = new Button(this);
        locate.setText("Get My Device Location");
        root.addView(locate);

        Button findHub = new Button(this);
        findHub.setText("Open Google Find Hub");
        root.addView(findHub);

        result = new TextView(this);
        result.setTextSize(16);
        result.setPadding(0,24,0,0);
        root.addView(result);

        locate.setOnClickListener(v -> locateOwnDevice());
        findHub.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://android.com/find"));
            startActivity(i);
        });
        setContentView(root);
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setPadding(0,12,0,12);
        return e;
    }

    void locateOwnDevice() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            result.setText("Location permission allow karein, phir button dobara dabayein.");
            return;
        }
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        Location loc = null;
        try {
            if (lm != null) {
                loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException ignored) {}
        if (loc == null) {
            result.setText("Location abhi available nahi hai. GPS/Location ON karke thodi der baad dobara try karein.");
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        String lat = String.valueOf(loc.getLatitude());
        String lon = String.valueOf(loc.getLongitude());
        result.setText("Current device location:\nLatitude: " + lat + "\nLongitude: " + lon);
        Intent map = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon));
        startActivity(map);
    }
}
