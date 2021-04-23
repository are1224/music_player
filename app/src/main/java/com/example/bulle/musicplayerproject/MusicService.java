package com.example.bulle.musicplayerproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private String songId;
    private IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }


    public MusicService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy(){

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

//        nowPosition = intent.getExtras().getInt("position");
//        songList = intent.getParcelableArrayListExtra("list");
//        songId = intent.getStringExtra("songId");
//
//        startMusic();

        return START_STICKY;
    }

    public void startMusic(String songId){

        this.songId = songId;

        mediaPlayer.reset();

        Uri musicUri = Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ""+songId
        );

        mediaPlayer = MediaPlayer.create(this, musicUri);
        mediaPlayer.start();
    }

    public void stopMusic(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void pauseMusic(){
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    public void moveAndStart(int position){
        if(mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
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

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
