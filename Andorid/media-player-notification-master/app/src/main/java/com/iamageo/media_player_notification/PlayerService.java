package com.iamageo.media_player_notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class PlayerService extends Service {


    private static final String CHANNEL_2 = "CHANNEL_2";
    MediaPlayer mediaPlayer = new MediaPlayer();
    private final IBinder mBinder = new MyBinder();
    MediaSession mediaSession;

    public void flipPlayPause(boolean isPlay) {
        Intent intent = new Intent("changePlayButton");
        intent.putExtra("isPlay", isPlay);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public PlayerService() {

    }

    public void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);

        Intent broadcast = new Intent(this, NotificationBroad.class);
        broadcast.putExtra(Constants.ACTION.MAIN_PLAY, "PLAY_ACTION");
        PendingIntent PPlay = PendingIntent.getBroadcast(this, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_2)
                .setContentIntent(pendingIntent)
                .setTicker("Ticker")
                .setSubText("Play")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(R.drawable.ic_baseline_play_arrow_24, "Play", PPlay)
                .setOngoing(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken())))
                .build();
        startForeground(1, notification);
    }

    public class MyBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    private void createChannelId() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_2, "channel2", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("");
            notificationChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mediaSession = new MediaSession(this, "tag");

        createChannelId();

        if(intent.getStringExtra("url")!=null)
        playStream(intent.getStringExtra("url"));

        if(intent.getAction().equals(Constants.ACTION.STARTFORGROUND_PLAY)) {
            showNotification();
        } else if (intent.getAction().equals(Constants.ACTION.MAIN_PLAY)) {
            togglerPlayer();
            showNotification();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;

    }

    public void playPlayer() {
        try {
            mediaPlayer.start();
            flipPlayPause(true);
        } catch (Exception e){
            Log.i("TAG","Player not working");
        }
    }

    public void pausePlayer() {
        try {
            mediaPlayer.pause();
            flipPlayPause(false);
        } catch (Exception e){
            Log.i("asd","Player not working");
        }
    }

    public void togglerPlayer() {
        try {
            if(mediaPlayer.isPlaying()) {
                pausePlayer();
            } else {
                playPlayer();
            }
        } catch (Exception e){
            Log.i("asd","Player not working");
        }
    }

    public void playStream(String url) {
        mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                playPlayer();

            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
