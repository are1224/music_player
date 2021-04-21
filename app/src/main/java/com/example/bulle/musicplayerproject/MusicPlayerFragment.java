package com.example.bulle.musicplayerproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicPlayerFragment extends Fragment{

    private int position;
    private ArrayList<Song> songList;
    private Button play_button;
    private Button previous_button;
    private Button next_button;
    private TextView player_music_title;


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
            settingButton();
        }
    }

    public void settingButton(){
        play_button = (Button)getView().findViewById(R.id.play_button);
        previous_button = (Button)getView().findViewById(R.id.previous_button);
        next_button = (Button)getView().findViewById(R.id.next_button);

        play_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getContext(), MusicService.class);
                intent.putExtra("songId", songList.get(position).getMusicId());
                getContext().startService(intent);
            }
        });

        previous_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (position == 0){
                    position = songList.size() - 1;
                }else{
                    position -= 1;
                }

                player_music_title.setText(songList.get(position).getMusicTitle());

                Intent intent = new Intent(getContext(), MusicService.class);
                intent.putExtra("songId", songList.get(position).getMusicId());
                getContext().startService(intent);
            }
        });

        next_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (position == songList.size() - 1){
                    position = 0;
                }else{
                    position += 1;
                }

                player_music_title.setText(songList.get(position).getMusicTitle());

                Intent intent = new Intent(getContext(), MusicService.class);
                intent.putExtra("songId", songList.get(position).getMusicId());
                getContext().startService(intent);
            }
        });
    }
}
