package ehud.marchi.astromusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, SongAdapter.onSongSelectedListener , SeekBar.OnSeekBarChangeListener{

    private MediaPlayer mediaPlayer = new MediaPlayer();
    public static ArrayList<Song> songs = new ArrayList<>();
    int currentPlaying = 0;
    NotificationCompat.Builder builder;
    NotificationManager manager;
    final int NOTIF_ID = 1;
    RemoteViews remoteViews;
    SeekBar songProgressBar;
    TextView duration;
    boolean isStopped = false;
    private Handler handler = new Handler();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();
        songProgressBar = MusicPlayerFragment.songProgress;
        songProgressBar.setOnSeekBarChangeListener(this);
        duration = MusicPlayerFragment.duration;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(mUpdateTimeTask);
        String command=intent.getStringExtra("command");
        String notif = intent.getStringExtra("notif");
        currentPlaying = intent.getIntExtra("song",0);
        if(notif!=null) {
            if (notif.equals("next")) {
                currentPlaying++;
                if (currentPlaying < songs.size()) {
                    Intent notifNextIntent = new Intent(this, MusicPlayerFragment.ActionsReceiver.class).setAction("ehud.marchi.astromusic.refresh");
                    notifNextIntent.putExtra("broadcast_command","next");
                    sendBroadcast(notifNextIntent);
                }
            } else if (notif.equals("prev")) {
                currentPlaying--;
                Intent notifPrevIntent = new Intent(this, MusicPlayerFragment.ActionsReceiver.class).setAction("ehud.marchi.astromusic.refresh");
                notifPrevIntent.putExtra("broadcast_command","prev");
                sendBroadcast(notifPrevIntent);
            }
            else if (notif.equals("pause"))
            {
                Intent notifPrevIntent = new Intent(this, MusicPlayerFragment.ActionsReceiver.class).setAction("ehud.marchi.astromusic.refresh");
                notifPrevIntent.putExtra("broadcast_command","pause");
                sendBroadcast(notifPrevIntent);
            }
            else if (notif.equals("play"))
            {
                Intent notifPrevIntent = new Intent(this, MusicPlayerFragment.ActionsReceiver.class).setAction("ehud.marchi.astromusic.refresh");
                notifPrevIntent.putExtra("broadcast_command","play");
                sendBroadcast(notifPrevIntent);
            }
            else if (notif.equals("stop"))
            {
                Intent notifPrevIntent = new Intent(this, MusicPlayerFragment.ActionsReceiver.class).setAction("ehud.marchi.astromusic.refresh");
                notifPrevIntent.putExtra("broadcast_command","stop");
                sendBroadcast(notifPrevIntent);
            }
        }
        loadData();
        ShowNotification();
        if(command!=null) {
            switch (command) {
                case "new_song":
                    newSong();
                    break;

                case "play":
                    play();
                    break;

                case "next":
                    next();
                    break;

                case "prev":
                    previous();
                    break;

                case "pause":
                    pause();
                    break;

                case "stop":
                    stop();
                    break;
                case "close":
                    Log.d("command","close");
                    stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void newSong() {
        Log.d("command","new_song");
        mediaPlayer.reset();
        if (!songs.isEmpty() && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void play() {
        Log.d("command","play");
        if (!songs.isEmpty() && !mediaPlayer.isPlaying()) {
            if(!isStopped) {
                mediaPlayer.start();
            }
            else
            {
                newSong();
            }
                remoteViews.setViewVisibility(R.id.play, View.INVISIBLE);
                remoteViews.setViewVisibility(R.id.pause, View.VISIBLE);
            updateProgressBar();
            isStopped = false;
        }
        Notification notification = builder.build();
        manager.notify(NOTIF_ID, notification);
    }

    private void next() {
        Log.d("command", "next");
        mediaPlayer.reset();
        if (!songs.isEmpty()) {
            if (currentPlaying < songs.size()) {
                try {
                    mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.prepareAsync();
            }
        }
    }

    private void previous() {
        Log.d("command","prev");
        mediaPlayer.reset();
        if(!songs.isEmpty()) {
            if(currentPlaying>=0) {
            try {
                mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
            }
             catch (IOException e) {
                e.printStackTrace();
            }
                mediaPlayer.prepareAsync();
            }
        }
    }

    private void pause() {
        if (mediaPlayer.isPlaying()) {
            Log.d("command","pause");
            remoteViews.setViewVisibility(R.id.pause, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.play, View.VISIBLE);
            mediaPlayer.pause();
            Notification notification = builder.build();
            manager.notify(NOTIF_ID, notification);
            Log.d("command","paused");
        }
    }

    private void stop() {
        Log.d("command","stop");
        isStopped = true;
        if (mediaPlayer.isPlaying())
        {mediaPlayer.stop();
            mediaPlayer.reset();
            if(!songs.isEmpty()) {
                try {
                    mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
                    remoteViews.setViewVisibility(R.id.pause, View.GONE);
                    remoteViews.setViewVisibility(R.id.play, View.VISIBLE);
                    Notification notification = builder.build();
                    manager.notify(NOTIF_ID, notification);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer !=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        handler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isStopped = false;
        this.mediaPlayer.start();
        songProgressBar.setProgress(0);
        moveSeekbar(songProgressBar);
        remoteViews.setTextViewText(R.id.song_name, songs.get(currentPlaying).getSongName());
        remoteViews.setTextViewText(R.id.song_artist, songs.get(currentPlaying).getSongArtist());
        Glide.with(this).asBitmap().load(songs.get(currentPlaying).getImageURL()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                remoteViews.setImageViewBitmap(R.id.song_image, resource);
                builder.setCustomBigContentView(remoteViews);
                Notification notification = builder.build();
                manager.notify(NOTIF_ID, notification);
            }

            @Override
            public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {
            }
        });
        Notification notification = builder.build();
        manager.notify(NOTIF_ID, notification);
    }
    private void loadData() {
        try {
            FileInputStream fis = openFileInput("songList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.songs = (ArrayList<Song>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void ShowNotification() {

        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        String channelId = null;
        if(Build.VERSION.SDK_INT >= 26) {

            channelId =  "astromusoc_channel_id" ;
            CharSequence channelName =  "Astromusic Channel" ;
            NotificationChannel notificationChannel =  new  NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(notificationChannel);
        }

        builder = new NotificationCompat.Builder(getApplicationContext(),channelId);
        builder.setSmallIcon(R.drawable.planet);

        remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);

        Intent playIntent = new Intent(this, MusicPlayerService.class);
        playIntent.putExtra("command", "play");
        playIntent.putExtra("song", currentPlaying);
        playIntent.putExtra("notif", "play");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play, playPendingIntent);

        Intent pauseIntent = new Intent(this, MusicPlayerService.class);
        pauseIntent.putExtra("command", "pause");
        pauseIntent.putExtra("song", currentPlaying);
        pauseIntent.putExtra("notif", "pause");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 5, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.pause, pausePendingIntent);

        Intent stopIntent = new Intent(this,MusicPlayerService.class);
        stopIntent.putExtra("command","stop");
        stopIntent.putExtra("song", currentPlaying);
        stopIntent.putExtra("notif", "stop");
        PendingIntent stopPendingIntent = PendingIntent.getService(this,1,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.stop,stopPendingIntent);

        Intent nextIntent=new Intent(this, MusicPlayerService.class);
        nextIntent.putExtra("command", "next");
        nextIntent.putExtra("song", currentPlaying);
        nextIntent.putExtra("notif", "next");
        PendingIntent nextPendingIntent=PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next, nextPendingIntent);


        Intent prevIntent=new Intent(this, MusicPlayerService.class);
        prevIntent.putExtra("command", "prev");
        prevIntent.putExtra("song", currentPlaying);
        prevIntent.putExtra("notif", "prev");
        PendingIntent prevPendingIntent=PendingIntent.getService(this, 3, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.previous, prevPendingIntent);

        Intent closeIntent=new Intent(this, MusicPlayerService.class);
        closeIntent.putExtra("command", "close");
        PendingIntent closePendingIntent=PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close, closePendingIntent);

        builder.setCustomBigContentView(remoteViews);
        startForeground(NOTIF_ID,builder.build());
    }

    @Override
    public void onSongSelected(int songIndex)
    {
        currentPlaying = songIndex;
    }
    //Progress bar section:
    public void updateProgressBar() {
        if(mediaPlayer.isPlaying())
            handler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();
            duration.setText(milliSecondsToTimer(currentDuration));
            // Updating progress bar
            int progress = (int)(getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);
            if(progress==100)
            {
                handler.removeCallbacks(mUpdateTimeTask);
                currentPlaying++;
                Intent finishIntent = new Intent(getApplicationContext(), MusicPlayerFragment.ActionsReceiver.class).setAction("ehud.marchi.astromusic.refresh");
                finishIntent.putExtra("broadcast_command","finish");
                sendBroadcast(finishIntent);
            }
            handler.postDelayed(this, 100);
        }
    };
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        handler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        moveSeekbar(seekBar);
    }

    private void moveSeekbar(SeekBar seekBar) {
        if(mediaPlayer.isPlaying()) {
            handler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = mediaPlayer.getDuration();
            int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);
            mediaPlayer.seekTo(currentPosition);
            updateProgressBar();
        }
    }
}
