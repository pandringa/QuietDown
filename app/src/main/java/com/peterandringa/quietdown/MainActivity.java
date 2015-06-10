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

import java.util.List;


public class MainActivity extends ActionBarActivity {
    RelativeLayout circleButton;
    TextView statusText;
    boolean isOn = false;
    TransitionDrawable animator;

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

        Log.w("MainActivity", "is running.");

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.w("MainActivity", "Ranged beacons: " + beacons);
            }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        // THE VIEW N STUFF
        setContentView(R.layout.activity_main);

        circleButton = (RelativeLayout)findViewById(R.id.circleButton);
        statusText = (TextView)findViewById(R.id.statusText);

        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = getResources().getDrawable(R.drawable.circle_off);
//        backgrounds[1] = getResources().getDrawable(R.drawable.circle_none);
        backgrounds[1] = getResources().getDrawable(R.drawable.circle_on);

        animator = new TransitionDrawable(backgrounds);

        circleButton.setBackground(animator);

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
        Log.i("CLICK", "turn on");
        animator.startTransition(200);
        statusText.setText(R.string.on);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        isOn = true;
    }

    public void turnOffNotifications() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        Log.i("CLICK", "turn off");
        animator.reverseTransition(200);
        statusText.setText(R.string.off);
        isOn = false;
    }
}
