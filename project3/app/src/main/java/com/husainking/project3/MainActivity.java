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
    DevicePolicyManager dpm; ComponentName admin; EditText email; TextView status, codeView, permStatus;
    static final int REQ_STANDARD=20, REQ_NOTIF=21;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        dpm=(DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        admin=new ComponentName(this,AdminReceiver.class);
        showHome();
    }

    TextView tv(String s,int size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setPadding(0,10,0,10); return t; }
    Button btn(String s){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); return b; }

    void base(LinearLayout l){
        l.setOrientation(LinearLayout.VERTICAL); l.setPadding(28,34,28,28);
        l.addView(tv("H.K Phone Manager",28));
        l.addView(tv("PROJECT 3 • Authorized second-phone management",15));
    }

    void showHome(){
        ScrollView sc=new ScrollView(this);
        LinearLayout l=new LinearLayout(this); base(l);
        l.addView(tv("Target phone ko is app se pair karke uska authorized dashboard yahan dikhaya jayega.",16));

        email=new EditText(this);
        email.setHint("Dusre phone ki Owner Gmail ID");
        email.setInputType(33);
        String saved=getPreferences(0).getString("owner_email","");
        email.setText(saved);
        l.addView(email);

        Button pair=btn("Pair / Add Second Phone");
        pair.setOnClickListener(v->{ saveEmail(); showController(); });
        l.addView(pair);

        Button setup=btn("Dusre phone par Permission Setup");
        setup.setOnClickListener(v->showPermissionWizard());
        l.addView(setup);

        l.addView(tv("Security: Gmail password kabhi enter/store nahi hoga. Pairing ke liye target phone owner ki explicit approval zaroori hai.",14));
        sc.addView(l); setContentView(sc);
    }

    void showController(){
        saveEmail();
        ScrollView sc=new ScrollView(this);
        LinearLayout l=new LinearLayout(this); base(l);
        l.addView(tv("SECOND PHONE — Controller Dashboard",22));
        l.addView(tv("Owner: "+getPreferences(0).getString("owner_email","")+
                "\nPairing code: "+pairCode()+"\nConnection: Waiting for authorized target device",14));

        Button connect=btn("Connect / Refresh Device");
        connect.setOnClickListener(v->toast("Secure backend/pairing connection abhi configure karna baaki hai."));
        l.addView(connect);

        l.addView(tv("DEVICE STATUS",18));
        l.addView(btn("📍 Location — Waiting for target connection"));
        l.addView(btn("🔋 Battery — Waiting for target connection"));
        l.addView(btn("📶 Network — Waiting for target connection"));
        l.addView(btn("📱 Device Info — Waiting for target connection"));

        l.addView(tv("AUTHORIZED ACTIONS",18));
        Button ring=btn("🔔 Ring Second Phone");
        ring.setOnClickListener(v->toast("Target device connection required."));
        l.addView(ring);
        Button lock=btn("🔒 Lock Second Phone");
        lock.setOnClickListener(v->toast("Target device connection + Device Admin authorization required."));
        l.addView(lock);
        Button screen=btn("📱 View / Control Screen");
        screen.setOnClickListener(v->toast("Target phone must explicitly approve Screen Capture and Accessibility first."));
        l.addView(screen);

        Button wizard=btn("⚙️ Open Target Permission Setup");
        wizard.setOnClickListener(v->showPermissionWizard());
        l.addView(wizard);

        Button back=btn("← Back");
        back.setOnClickListener(v->showHome());
        l.addView(back);

        sc.addView(l); setContentView(sc);
    }

    void showPermissionWizard(){
        ScrollView sc=new ScrollView(this);
        LinearLayout l=new LinearLayout(this); base(l);
        l.addView(tv("TARGET PHONE — ONE SETUP SCREEN",22));
        l.addView(tv("Neeche se sab permissions isi setup screen se start hongi. Android protected permissions ko ek single automatic grant mein combine nahi karta; owner ko har protected prompt/settings screen par khud approve karna hota hai.",14));

        permStatus=tv(permissionSummary(),15); l.addView(permStatus);

        Button standard=btn("1️⃣ Allow Location + basic permissions");
        standard.setOnClickListener(v->requestStandard());
        l.addView(standard);

        Button background=btn("2️⃣ Allow Background Location");
        background.setOnClickListener(v->{
            if(Build.VERSION.SDK_INT>=30) startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            else requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},30);
        });
        l.addView(background);

        Button adminBtn=btn("3️⃣ Enable Device Management");
        adminBtn.setOnClickListener(v->enableAdmin()); l.addView(adminBtn);

        Button acc=btn("4️⃣ Enable Accessibility (remote-control authorization)");
        acc.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); l.addView(acc);

        Button notif=btn("5️⃣ Allow Notifications");
        notif.setOnClickListener(v->{if(Build.VERSION.SDK_INT>=33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIF); else toast("Notifications permission is not required on this Android version.");});
        l.addView(notif);

        Button mail=btn("6️⃣ Send Approval Request to Owner Gmail");
        mail.setOnClickListener(v->sendApprovalMail()); l.addView(mail);

        Button refresh=btn("↻ Refresh permission status");
        refresh.setOnClickListener(v->permStatus.setText(permissionSummary())); l.addView(refresh);

        Button back=btn("← Back to Controller");
        back.setOnClickListener(v->showController()); l.addView(back);

        sc.addView(l); setContentView(sc);
    }

    void saveEmail(){ String s=email==null?"":email.getText().toString().trim(); if(!s.isEmpty()) getPreferences(0).edit().putString("owner_email",s).apply(); }

    String pairCode(){
        String c=getPreferences(0).getString("pair_code",null);
        if(c==null){ c=String.format(Locale.US,"%06d",new Random().nextInt(1000000)); getPreferences(0).edit().putString("pair_code",c).apply(); }
        return c;
    }

    String permissionSummary(){
        boolean loc=Build.VERSION.SDK_INT<23 || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
        boolean notif=Build.VERSION.SDK_INT<33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;
        return "Location: "+(loc?"ON":"OFF")+"\nNotifications: "+(notif?"ON":"OFF")+"\nDevice Management: "+(dpm.isAdminActive(admin)?"ON":"OFF")+"\nAccessibility: check Android Settings\nScreen Capture: Android requires explicit approval when started.";
    }

    void requestStandard(){
        if(Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_STANDARD);
        else toast("Basic permissions already handled.");
    }

    void enableAdmin(){
        Intent i=new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,admin);
        i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"Enable only on your own second phone. This permits authorized device-management actions such as locking the device.");
        startActivity(i);
    }

    void sendApprovalMail(){
        String to=getPreferences(0).getString("owner_email","");
        if(to.isEmpty()){toast("Pehle Owner Gmail ID enter karo."); return;}
        String body="H.K Phone Manager\nPairing code: "+pairCode()+"\nDevice: "+Build.MANUFACTURER+" "+Build.MODEL+"\n\nPlease approve this request only if you own/control this device.";
        Intent i=new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+Uri.encode(to)));
        i.putExtra(Intent.EXTRA_SUBJECT,"H.K Phone Manager — Permission Request");
        i.putExtra(Intent.EXTRA_TEXT,body);
        try{startActivity(i);}catch(Exception e){toast("Mail app available nahi hai.");}
    }

    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}