package com.example.bulle.musicplayerproject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicPlayerFragment extends Fragment{

    private int position;
    private ArrayList<Song> songList;
    private Button play_button;
    private Button previous_button;
    private Button next_button;
    private Button stop_button;
    private TextView player_music_title;
    private SeekBar music_seekBar;

    private String duration;
    private int total_second;

    private MusicService mService;

    private ProgressSettingTask progressSettingTask;

    private boolean isPaused = false;

    interface OnMusicServiceListener{
        MusicService getMusciService();
        void setMusicState(boolean state);
        boolean getMusicState();
    }

    private OnMusicServiceListener mServiceListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mServiceListener = (OnMusicServiceListener) context;
            mService = mServiceListener.getMusciService();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMusicSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.music_player, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if(args != null){
            position = args.getInt("position");
            songList = args.getParcelableArrayList("songList");
            player_music_title = (TextView)getView().findViewById(R.id.player_music_title);
            player_music_title.setText(songList.get(position).getMusicTitle());

            music_seekBar = (SeekBar)getView().findViewById(R.id.music_seek_bar);
            music_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if(progressSettingTask != null && !progressSettingTask.isCancelled()){
                        progressSettingTask.cancel(true);
                    }
                    mService.pauseMusic();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(seekBar.getProgress() == seekBar.getMax()){
                        mService.stopMusic();
                        seekBar.setProgress(0);
                    }else {
                        int changePosition = seekBar.getProgress();
                        mService.moveAndStart(changePosition);
                        start_progress_seek_bar(changePosition);
                    }
                }
            });
            settingButton();

            if (mService.getSongId() != null) {
                if (mService.getSongId() != songList.get(position).getMusicId()) {
                    play_button.callOnClick();
                } else {
                    Log.d("Music is current run!", "hurry up, 음악은 이미 시작된지 오래야");
                    createReSetting();
                }
            }else{
                play_button.callOnClick();
            }
        }
    }

    public void createReSetting(){

        duration = songList.get(position).getDuration();
        total_second = Integer.valueOf(duration);
        music_seekBar.setMax(total_second);

        int start_time = mService.getCurrentMusicTime();
        music_seekBar.setProgress(start_time);

        if (mServiceListener.getMusicState() == true) {
            play_button.setVisibility(View.INVISIBLE);
            stop_button.setVisibility(View.VISIBLE);

            start_progress_seek_bar(start_time);
        }else{
            stop_button.setVisibility(View.GONE);
            play_button.setVisibility(View.VISIBLE);

            isPaused = true;
        }
    }

    public void settingButton(){
        play_button = (Button)getView().findViewById(R.id.play_button);
        previous_button = (Button)getView().findViewById(R.id.previous_button);
        next_button = (Button)getView().findViewById(R.id.next_button);
        stop_button = (Button)getView().findViewById(R.id.stop_button);

        play_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                mServiceListener.setMusicState(true);

                if (isPaused == false) {
                    if (progressSettingTask != null && !progressSettingTask.isCancelled()) {
                        progressSettingTask.cancel(true);
                    }

                    music_seekBar.setProgress(0);
                    duration = songList.get(position).getDuration();
                    total_second = Integer.valueOf(duration);
                    music_seekBar.setMax(total_second);

                    mService.startMusic(songList.get(position).getMusicId());
                    start_progress_seek_bar(0);

                }else{
                    mService.moveAndStart(mService.getCurrentMusicTime());
                    start_progress_seek_bar(mService.getCurrentMusicTime());
                    isPaused = false;
                }

                play_button.setVisibility(View.INVISIBLE);
                stop_button.setVisibility(View.VISIBLE);
            }

        });

        previous_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                mServiceListener.setMusicState(true);

                if(progressSettingTask != null && !progressSettingTask.isCancelled()){
                    progressSettingTask.cancel(true);
                }

                music_seekBar.setProgress(0);
                if (position == 0){
                    position = songList.size() - 1;
                }else{
                    position -= 1;
                }

                player_music_title.setText(songList.get(position).getMusicTitle());

                duration = songList.get(position).getDuration();
                total_second = Integer.valueOf(duration);
                music_seekBar.setMax(total_second);
                mService.startMusic(songList.get(position).getMusicId());
                start_progress_seek_bar(0);
            }
        });

        next_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                mServiceListener.setMusicState(true);

                if(progressSettingTask != null && !progressSettingTask.isCancelled()){
                    progressSettingTask.cancel(true);
                }

                music_seekBar.setProgress(0);
                if (position == songList.size() - 1){
                    position = 0;
                }else{
                    position += 1;
                }

                player_music_title.setText(songList.get(position).getMusicTitle());

                duration = songList.get(position).getDuration();
                total_second = Integer.valueOf(duration);
                music_seekBar.setMax(total_second);

                mService.startMusic(songList.get(position).getMusicId());
                start_progress_seek_bar(0);
            }
        });

        stop_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                mServiceListener.setMusicState(false);

                mService.pauseMusic();
                isPaused = true;

                if (progressSettingTask != null && !progressSettingTask.isCancelled()) {
                    progressSettingTask.cancel(true);
                }

                stop_button.setVisibility(View.GONE);
                play_button.setVisibility(View.VISIBLE);
            }
        });
    }

    public void start_progress_seek_bar(int start_time){
        progressSettingTask = new ProgressSettingTask();
        progressSettingTask.execute(start_time);
    }

    class ProgressSettingTask extends AsyncTask<Integer, Integer, Void>{

        @Override
        protected Void doInBackground(Integer... args) {

            for (int i = args[0]; i<=total_second; i++){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                publishProgress(mService.getCurrentMusicTime());

                if (isCancelled()){
                    Log.d("music canceled", "음악 멈췄다.");
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            music_seekBar.setProgress(values[0]);
        }

        @Override
        protected void onCancelled(Void aVoid){
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (progressSettingTask != null && !progressSettingTask.isCancelled()) {
            progressSettingTask.cancel(true);
        }
    }


    public String convertToTime(String time_text){
        if(Integer.valueOf(time_text) != null){
            int time = Integer.valueOf(time_text);

            int second = time / 1000;
            int minute = second / 60;
            second = second % 60;

            if (second < 10){
                return String.valueOf(minute) + ":0" + String.valueOf(second);
            }else{
                return String.valueOf(minute) + ":" + String.valueOf(second);
            }
        }
        return null;
    }
}
