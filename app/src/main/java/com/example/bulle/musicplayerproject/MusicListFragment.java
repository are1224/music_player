package com.example.bulle.musicplayerproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MusicListFragment extends Fragment implements MyRecyclerAdapter.MyRecyclerViewClickListener{

    private RecyclerView recyclerView;
    private ArrayList<Song> songList;
    private TextView textView;
    private MyRecyclerAdapter adapter;
    private MusicListFragment musicListFragment = this;

    interface OnMusicSelectedListener{
        void onMusicSelected(ArrayList<Song> songList, int position);
    }

    interface OnEnrollMusicList{
        void setMusicList(ArrayList<Song> musicList);
        ArrayList<Song> getMusicList();
    }

    private OnMusicSelectedListener mListener;
    private OnEnrollMusicList eListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        if (savedInstanceState != null){
            Log.d("look at this", "songList 가 있네 !!!!!!!!!!!!!!!");
            songList = savedInstanceState.getParcelableArrayList("songList");
            adapter = new MyRecyclerAdapter(songList);
            adapter.setOnClickListener(musicListFragment);
            recyclerView.setAdapter(adapter);
        }
        return inflater.inflate(R.layout.music_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        recyclerView = (RecyclerView)getView().findViewById(R.id.recycler_view);
        //레이아웃 매니저로 LinearLayoutManager를 설정
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        if (songList == null) {
            songList = eListener.getMusicList();
        }

        if (songList == null) {
            new AdapterSetting().execute();
        }else{
            Log.d("Success", "드디어 뮤직 리스트 가지고 오는데 성공!");
            adapter = new MyRecyclerAdapter(songList);
            adapter.setOnClickListener(musicListFragment);
            recyclerView.setAdapter(adapter);
        }
    }

    class AdapterSetting extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            String[] projection = {MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE};
            Cursor cursor = getContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC"
            );

            songList = new ArrayList<>();
            if (cursor != null) {
                Log.d("cursor is ?", "cursor 있냐?");
                while (cursor.moveToNext()) {
                    Log.d("cursor how many?", "커서는 몇개냐?");
                    if (cursor.getInt(0) != 0) {
                        Log.d("music is ?", "mp3 있냐?");
                        Song song = new Song();
                        song.setMusicId(cursor.getString(1));
                        song.setMusicTitle(cursor.getString(2).trim());
                        songList.add(song);
                    }
                }
            }

            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids){
            eListener.setMusicList(songList);
            adapter = new MyRecyclerAdapter(songList);
            adapter.setOnClickListener(musicListFragment);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try{
            mListener = (OnMusicSelectedListener)context;
            eListener = (OnEnrollMusicList)context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnMusicSelectedListener");
        }
    }

    @Override
    public void onItemClicked(int position){
        Log.d("song touched", songList.get(position).getMusicTitle());
        mListener.onMusicSelected(songList, position);
        songList = null;
//        Intent intent = new Intent(getContext(), MusicService.class);
//        intent.putParcelableArrayListExtra("list", songList);
//        intent.putExtra("position", position);
//        getContext().startService(intent);
    }

    public void setStopService(View view){
        Intent intent = new Intent(getContext(), MusicService.class);
        getContext().stopService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("songList", songList);
    }

}
