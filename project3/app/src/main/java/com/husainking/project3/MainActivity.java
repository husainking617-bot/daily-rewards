package com.husainking.project3;

import android.Manifest;
import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    DevicePolicyManager dpm; ComponentName admin; EditText email; TextView status, codeView;
    static final int REQ_LOC=20, REQ_NOTIF=21, REQ_CAPTURE=22;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        dpm=(DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        admin=new ComponentName(this,AdminReceiver.class);
        buildUi();
    }

    TextView tv(String s,int size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setPadding(0,12,0,12); return t; }
    Button btn(String s){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); return b; }

    void buildUi(){
        ScrollView sc=new ScrollView(this);
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(32,40,32,32);
        l.addView(tv("H.K Phone Manager",28));
        l.addView(tv("PROJECT 3 • Authorized second-phone management",15));
        l.addView(tv("Target phone par ye app install karo. Owner Gmail aur permissions target phone ke owner ko khud approve karni hongi.",15));

        email=new EditText(this); email.setHint("Owner Gmail (example@gmail.com)"); email.setInputType(33); l.addView(email);
        Button save=btn("Save Owner Gmail"); save.setOnClickListener(v->{getPreferences(0).edit().putString("owner_email",email.getText().toString().trim()).apply(); toast("Owner Gmail saved");}); l.addView(save);

        codeView=tv("Pairing code: "+pairCode(),20); l.addView(codeView);
        Button newCode=btn("Generate New Pairing Code"); newCode.setOnClickListener(v->{String c=pairCode(); getPreferences(0).edit().putString("pair_code",c).apply(); codeView.setText("Pairing code: "+c);}); l.addView(newCode);

        status=tv(statusText(),15); l.addView(status);

        Button adminBtn=btn("1. Enable Device Management"); adminBtn.setOnClickListener(v->enableAdmin()); l.addView(adminBtn);
        Button loc=btn("2. Allow Location"); loc.setOnClickListener(v->requestLocation()); l.addView(loc);
        Button acc=btn("3. Allow Accessibility (for authorized remote-control features)"); acc.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); l.addView(acc);
        Button notif=btn("4. Allow Notifications"); notif.setOnClickListener(v->{if(Build.VERSION.SDK_INT>=33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIF);}); l.addView(notif);
        Button screen=btn("5. Allow Screen Sharing when requested"); screen.setOnClickListener(v->{ Intent i=new Intent(Settings.ACTION_SETTINGS); startActivity(i); toast("Screen sharing is a protected Android permission and must be approved on this phone.");}); l.addView(screen);

        Button request=btn("Send Permission Request to Owner Gmail"); request.setOnClickListener(v->sendApprovalMail()); l.addView(request);

        Button lock=btn("Test: Lock This Device"); lock.setOnClickListener(v->{if(dpm.isAdminActive(admin)) dpm.lockNow(); else toast("Enable Device Management first");}); l.addView(lock);
        Button ring=btn("Test: Ring This Device"); ring.setOnClickListener(v->{((android.media.AudioManager)getSystemService(AUDIO_SERVICE)).setStreamVolume(android.media.AudioManager.STREAM_RING, ((android.media.AudioManager)getSystemService(AUDIO_SERVICE)).getStreamMaxVolume(android.media.AudioManager.STREAM_RING),0); ((android.os.Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createWaveform(new long[]{0,500,500,500,500,500},-1));}); l.addView(ring);

        Button location=btn("Show Current Location (on this phone)"); location.setOnClickListener(v->showLocation()); l.addView(location);
        l.addView(tv("Important: Gmail alone cannot deliver Android permission prompts. Real cross-device remote commands need an authenticated internet relay/backend. This app never bypasses Android permissions; the target phone must explicitly approve Device Admin, Location, Accessibility and Screen Capture.",14));

        sc.addView(l); setContentView(sc);
    }

    String pairCode(){ String c=getPreferences(0).getString("pair_code",null); if(c==null){c=String.format(Locale.US,"%06d",new Random().nextInt(1000000)); getPreferences(0).edit().putString("pair_code",c).apply();} return c; }
    String statusText(){return "Device ID: "+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+"\nAdmin: "+(dpm.isAdminActive(admin)?"ENABLED":"OFF");}
    void enableAdmin(){startActivity(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,admin).putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"Only enable this on your own device. It allows authorized device-management actions such as remote lock once a secure controller is connected."));}
    void requestLocation(){ if(Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOC); }
    void showLocation(){ LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE); try{ Location x=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); if(x==null)x=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); toast(x==null?"No recent location available":String.format(Locale.US,"Lat %.6f, Lon %.6f",x.getLatitude(),x.getLongitude())); }catch(Exception e){toast("Location permission required");}}
    void sendApprovalMail(){ String to=email.getText().toString().trim(); if(to.isEmpty()) to=getPreferences(0).getString("owner_email",""); if(to.isEmpty()){toast("Owner Gmail enter karo"); return;} String body="H.K Phone Manager\nDevice: "+Build.MANUFACTURER+" "+Build.MODEL+"\nPairing code: "+pairCode()+"\nThis request is for an owner-authorized second phone. Please approve only if you own/control this device."; Intent i=new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+Uri.encode(to))); i.putExtra(Intent.EXTRA_SUBJECT,"H.K Phone Manager — Permission Request"); i.putExtra(Intent.EXTRA_TEXT,body); try{startActivity(i);}catch(Exception e){toast("Gmail app available nahi hai");}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
