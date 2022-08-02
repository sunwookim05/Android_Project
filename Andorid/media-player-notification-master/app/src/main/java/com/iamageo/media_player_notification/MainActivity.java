package com.iamageo.media_player_notification;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static FloatingActionButton fb;
    PlayerService mBoundService;
    boolean mServiceBound = false;

    public static String URL = "https://s1.guaracast.com:8427/stream";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.MyBinder myBinder = (PlayerService.MyBinder)service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    BroadcastReceiver mMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra("isPlay", false);
            flipPlayButton(isPlaying);
        }
    };

    public static void flipPlayButton(boolean isPlay) {
        if(isPlay) {
            fb.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            fb.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fb = findViewById(R.id.fab);

        startStream(URL);

        fb.setOnClickListener(v -> {
            if(mServiceBound) {
                mBoundService.togglerPlayer();
            }
        });


    }

    public void startStream(String url) {
        Intent i = new Intent(MainActivity.this, PlayerService.class);
        i.putExtra("url", url);
        startService(i);
        bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessage, new IntentFilter("changePlayButton"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }
}