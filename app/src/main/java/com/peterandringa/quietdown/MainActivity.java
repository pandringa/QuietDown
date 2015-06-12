package com.peterandringa.quietdown;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;

import java.util.Collections;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    RelativeLayout circleButton;
    TextView statusText;
    boolean isOn = true;
    TransitionDrawable animator;
    int oldVolume;
    int oldRingerMode;

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static Region ALL_ESTIMOTE_BEACONS;

    private BeaconManager beaconManager;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // THE LOGIC
        // initialize the thing
        EstimoteSDK.initialize(this, "quietdown", "04ceee898ae59ced3374c7bd79e2d267");

        beaconManager = new BeaconManager(this);
        ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
        beaconManager.setBackgroundScanPeriod(5000, 5000);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Beacon beacon = null;
                for(Beacon b : beacons){
                    if(b.getMajor() == 41792 && b.getMinor() == 11312){
                        Log.w("", "Found beacon");
                        beacon = b;
                    }
                }

                if(!isOn && beacon == null){
                    Log.w("BEACON", "turn on");
                    turnOnNotifications();
                }else if(isOn && beacon != null){
                    turnOffNotifications();
                    Log.w("BEACON", "turn off");
                    Log.w("", beacons.toString());
                }
            }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        // THE VIEW N STUFF
        setContentView(R.layout.activity_main);

        circleButton = (RelativeLayout)findViewById(R.id.circleButton);
        statusText = (TextView)findViewById(R.id.statusText);

        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = getResources().getDrawable(R.drawable.circle_on);
        backgrounds[1] = getResources().getDrawable(R.drawable.circle_off);

        animator = new TransitionDrawable(backgrounds);

        circleButton.setBackground(animator);

        circleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isOn){
                    Log.w("CLICK", "turn on");

                    turnOnNotifications();
                }else{
                    Log.w("CLICK", "turn off");
                    turnOffNotifications();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //connect the beaconmanager to the activity thing
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                } catch (RemoteException e) {
                    Log.e("MainActivity", "Cannot start ranging", e);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
        } catch (RemoteException e) {
            Log.e("MainActivity", "Cannot stop but it does not matter now", e);
        }

    }

    public void turnOnNotifications() {
        animator.reverseTransition(200);
        statusText.setText(R.string.on);
        oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldVolume, 0);
        audioManager.setRingerMode(oldRingerMode);
        isOn = true;
    }

    public void turnOffNotifications() {
        oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        oldRingerMode = audioManager.getRingerMode();
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setRingerMode(0);
        animator.startTransition(200);
        statusText.setText(R.string.off);
        isOn = false;
    }
}
