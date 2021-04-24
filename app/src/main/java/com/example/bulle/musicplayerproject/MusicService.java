package com.example.bulle.musicplayerproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private final String TAG = "MusicService";

    private MediaPlayer mediaPlayer;
    private String songId;

    private ArrayList<Song> songList;
    private int song_position;

    public static final String MUSIC_ACTION_FILTER = "com.example.bulle.musicplayerproject.action.ACTION_MUSIC_BROADCAST";

    private String MUSIC_PREV = "MUSIC_PREV";
    private String MUSIC_NOW = "MUSIC_NOW";
    private String MUSIC_NEXT = "MUSIC_NEXT";
    private String MUSIC_CLOSE = "MUSIC_CLOSE";
    private String MUSIC_PAUSE = "MUSIC_PAUSE";

    private IntentFilter musicFilter;

    private RemoteViews contentView;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder nBuilder;
    private Notification notification;

    private boolean isActiveNotification = false;
    private boolean isPaused = false;

    private IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }

    public MusicService() {
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(){
        super.onCreate();
        mediaPlayer = new MediaPlayer();

        musicFilter = new IntentFilter();
        musicFilter.addAction(MUSIC_PREV);
        musicFilter.addAction(MUSIC_NOW);
        musicFilter.addAction(MUSIC_NEXT);
        musicFilter.addAction(MUSIC_CLOSE);
        musicFilter.addAction(MUSIC_PAUSE);

        registerReceiver(broadcastReceiver, musicFilter);

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.e(TAG, "mediaPlayer error i : " + i + " , i1 : " + i1);
                return false;
            }
        });

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        return START_STICKY;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void startMusic(String songId){

        if (isActiveNotification == false){
            isActiveNotification = true;
            Intent intent = new Intent(MUSIC_ACTION_FILTER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);

            Intent prevIntent = new Intent(MUSIC_PREV);
            Intent nowIntent = new Intent(MUSIC_NOW);
            Intent nextIntent = new Intent(MUSIC_NEXT);
            Intent closeIntent = new Intent(MUSIC_CLOSE);
            Intent pauseIntent = new Intent(MUSIC_PAUSE);

            PendingIntent pdIntentPrev = PendingIntent.getBroadcast(this, 0, prevIntent, 0);
            PendingIntent pdIntentNow = PendingIntent.getBroadcast(this, 0, nowIntent, 0);
            PendingIntent pdIntentNext = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
            PendingIntent pdIntentClose = PendingIntent.getBroadcast(this, 0, closeIntent, 0);
            PendingIntent pdIntentPause = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);

            contentView.setTextViewText(R.id.notification_title, songList.get(song_position).getMusicTitle());

            contentView.setOnClickPendingIntent(R.id.notifcation_prevButton, pdIntentPrev);
            contentView.setOnClickPendingIntent(R.id.notification_startButton, pdIntentNow);
            contentView.setOnClickPendingIntent(R.id.notification_nextButton, pdIntentNext);
            contentView.setOnClickPendingIntent(R.id.notification_end, pdIntentClose);
            contentView.setOnClickPendingIntent(R.id.notification_stopButton, pdIntentPause);

            nBuilder = new NotificationCompat.Builder(this, "musicChannel");
            nBuilder.setSmallIcon(R.mipmap.ic_launcher);
            nBuilder.setContentTitle("music service");
            nBuilder.setContentText("now music running");
            nBuilder.setContentIntent(pendingIntent);
            nBuilder.setContent(contentView);

            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                notificationManager.createNotificationChannel(new NotificationChannel("musicChannel", "음악 채널", NotificationManager.IMPORTANCE_DEFAULT));
            }

            notification = nBuilder.build();

            //포그라운드로 시작
            startForeground(2127, notification);
        }

        this.songId = songId;

        mediaPlayer.reset();

        Uri musicUri = Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ""+songId
        );

        mediaPlayer = MediaPlayer.create(this, musicUri);
        mediaPlayer.start();
        isPaused = false;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public void reStartMusic(){
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public void stopMusic(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPaused = false;
        }
    }

    public void pauseMusic(){
        if(mediaPlayer != null){
            isPaused = true;
            mediaPlayer.pause();
        }
    }

    public void moveAndStart(int position){
        if(mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public int getCurrentMusicTime(){
        if(mediaPlayer != null){
//            Log.d("now musicPosition : ", "" + mediaPlayer.getCurrentPosition());
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public String getSongId(){
        if (songId != null) {
            return songId;
        }else{
            return null;
        }
    }

    public void setIsPaused(boolean state){
        isPaused = state;
    }

    public boolean getIsPaused(){
        return isPaused;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setSongList(ArrayList<Song> list){
        songList = list;
    }

    public void setSongPosition(int pos){
        song_position = pos;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void notificationChange(String state){
        if (state.equals(MUSIC_PAUSE)){
            contentView.setViewVisibility(R.id.notification_stopButton, View.GONE);
            contentView.setViewVisibility(R.id.notification_startButton, View.VISIBLE);
        } else if(state.equals(MUSIC_NOW)){
            contentView.setViewVisibility(R.id.notification_startButton, View.INVISIBLE);
            contentView.setViewVisibility(R.id.notification_stopButton, View.VISIBLE);
        }else if(state.equals(MUSIC_PREV)){
            contentView.setTextViewText(R.id.notification_title, songList.get(song_position).getMusicTitle());
            contentView.setViewVisibility(R.id.notification_startButton, View.INVISIBLE);
            contentView.setViewVisibility(R.id.notification_stopButton, View.VISIBLE);
        }

        notification.bigContentView = contentView;
        startForeground(2127, notification);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String music_action = intent.getAction();

            if (music_action.equals(MUSIC_PREV)){
                Log.d("Music notification", "music prev");

                if (song_position > 0){
                    song_position -= 1;
                }

                notificationChange(MUSIC_PREV);

                if (getIsPaused() == false){
                    //멈춰 있지 않는 상태에서
                    Log.d("Music Service Prev", "걍 이전");
                    Intent change_state_musicPlayer = new Intent("MUSIC_PLAYER_PREV");
                    sendBroadcast(change_state_musicPlayer);
                }else if (getIsPaused() == true){
                    //멈춰 있는 상태에서
                    Log.d("Music Service pPrev", "멈춰 있는 상태에서 이전");
                    Intent change_state_musicPlayer = new Intent("MUSIC_PLAYER_PAUSE_PREV");
                    sendBroadcast(change_state_musicPlayer);
                }

                startMusic(songList.get(song_position).getMusicId());

            }
            else if(music_action.equals(MUSIC_NEXT)){
                Log.d("Music notification", "music next");
                song_position += 1;
                startMusic(songList.get(song_position).getMusicId());
            }
            else if(music_action.equals(MUSIC_NOW)){
                Log.d("Music notification", "music now");

                reStartMusic();

                notificationChange(MUSIC_NOW);

                Intent change_state_musicPlayer = new Intent("MUSIC_PLAYER_START");
                sendBroadcast(change_state_musicPlayer);
            }
            else if(music_action.equals(MUSIC_CLOSE)){
                Log.d("Music notification", "music close");
                stopForeground(true);
                mediaPlayer.reset();
                isActiveNotification = false;

                Intent change_state_musicPlayer = new Intent("MUSIC_PLAYER_STOP");
                sendBroadcast(change_state_musicPlayer);

                Intent closeIntent = new Intent(MUSIC_ACTION_FILTER);
                sendBroadcast(closeIntent);
            }
            else if (music_action.equals(MUSIC_PAUSE)){
                Log.d("Music notification", "music pause");
                pauseMusic();

                notificationChange(MUSIC_PAUSE);

                Intent change_state_musicPlayer = new Intent("MUSIC_PLAYER_PAUSE");
                sendBroadcast(change_state_musicPlayer);
            }
        }
    };
}
