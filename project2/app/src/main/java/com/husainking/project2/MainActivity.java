package com.husainking.project2;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.*;

public class MainActivity extends Activity {
  private void open(String url){ startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
  private Button btn(String text){ Button b=new Button(this); b.setText(text); b.setTextSize(16); b.setAllCaps(false); b.setOnClickListener(v->{ switch(text){
    case "📍 Google Find Hub": open("https://android.com/find"); break;
    case "🔐 Google Account Recovery": open("https://accounts.google.com/signin/recovery"); break;
    case "📵 CEIR: Block Lost/Stolen Phone": open("https://ceir.sancharsaathi.gov.in/Request/CeirUserBlockRequestDirect.jsp"); break;
    case "🔎 CEIR: Check IMEI": open("https://ceir.sancharsaathi.gov.in/Device/CeirImeiVerification.jsp"); break;
    case "📋 CEIR Request Status": open("https://ceir.sancharsaathi.gov.in/Request/CeirRequestStatus.jsp"); break;
    case "📖 CEIR Help": open("https://ceir.sancharsaathi.gov.in/Home/help.jsp"); break;
  }}); return b; }
  @Override public void onCreate(Bundle x){ super.onCreate(x);
    LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,28,32,24); root.setBackgroundColor(Color.WHITE);
    TextView title=new TextView(this); title.setText("📱 Phone Recovery Helper"); title.setTextSize(26); title.setTextColor(Color.rgb(20,20,20)); title.setPadding(0,0,0,12); root.addView(title);
    TextView info=new TextView(this); info.setText("Lost/stolen Android phone ke liye official recovery tools.\n\n⚠️ Sirf Gmail ID se kisi ki live location ya password nahi nikala ja sakta. Find Hub tabhi location dikha sakta hai jab Google account/device ki required access available ho."); info.setTextSize(16); info.setTextColor(Color.DKGRAY); info.setPadding(0,0,0,16); root.addView(info);
    String[] bs={"📍 Google Find Hub","🔐 Google Account Recovery","📵 CEIR: Block Lost/Stolen Phone","🔎 CEIR: Check IMEI","📋 CEIR Request Status","📖 CEIR Help"};
    for(String s:bs){ Button b=btn(s); root.addView(b,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)); }
    TextView note=new TextView(this); note.setText("\nTip: CEIR ke liye police complaint/FIR, re-issued SIM aur IMEI jaise details ki zaroorat ho sakti hai."); note.setTextSize(14); note.setTextColor(Color.GRAY); root.addView(note);
    setContentView(root);
  }
}
