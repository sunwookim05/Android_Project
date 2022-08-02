package com.iamageo.media_player_notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class NotificationBroad extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {

        if(Objects.equals(intent.getStringExtra(Constants.ACTION.MAIN_PLAY), "PLAY_ACTION")) {
            Intent i = new Intent(context, PlayerService.class);
            i.setAction(Constants.ACTION.MAIN_PLAY);
            ContextCompat.startForegroundService(context, i);
        }

    }
}
