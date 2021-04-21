package com.example.bulle.musicplayerproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private String songId;
//    private List<Song> songList;
//    private int nowPosition;


    public MusicService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();

//        songList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

//        nowPosition = intent.getExtras().getInt("position");
//        songList = intent.getParcelableArrayListExtra("list");
        songId = intent.getStringExtra("songId");

        startMusic();

        return START_STICKY;
    }

    public void startMusic(){
        mediaPlayer.reset();

        Uri musicUri = Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ""+songId
        );

        mediaPlayer = MediaPlayer.create(this, musicUri);
        mediaPlayer.start();
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
