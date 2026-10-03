package com.husainking.project3;
import android.app.Service; import android.content.Intent; import android.os.IBinder;
public class LocationService extends Service { public int onStartCommand(Intent i,int f,int s){ return START_STICKY; } public IBinder onBind(Intent i){return null;} }