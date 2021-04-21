package com.example.bulle.musicplayerproject;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicListFragment.OnMusicSelectedListener, MusicListFragment.OnEnrollMusicList{

    private static final int MESSAGE_PERMISSION_GRANTED = 101;
    private static final int MESSAGE_PERMISSION_DENIED = 102;

    private MusicListFragment musicListFragment;

    private ArrayList<Song> musicList;

    public MainHandler mainHandler = new MainHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showPermissionDialog();

        musicListFragment = new MusicListFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, musicListFragment).commit();
    }

    private void showPermissionDialog() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                mainHandler.sendEmptyMessage(MESSAGE_PERMISSION_GRANTED);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied: " + deniedPermissions.get(0), Toast.LENGTH_SHORT).show();
                mainHandler.sendEmptyMessage(MESSAGE_PERMISSION_DENIED);
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("We need your permission for write external storage.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    public void stop_music(View view) {
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }

    public void onMusicSelected(ArrayList<Song> songList, int position){
        //MusicPlayer 프래그먼트 생성
        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
        //Argment로 인자 전달
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putParcelableArrayList("songList", songList);
        musicPlayerFragment.setArguments(args);

        //프래그먼트 교체
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, musicPlayerFragment)
                .addToBackStack(null).commit();

//        Intent intent = new Intent(MainActivity.this, MusicService.class);
//        intent.putParcelableArrayListExtra("list", songList);
//        intent.putExtra("position", position);
//        startService(intent);
    }

    public void setMusicList(ArrayList<Song> musicList){
        this.musicList = musicList;
    }

    public ArrayList<Song> getMusicList(){
        if (musicList == null){
            return null;
        }else {
            return musicList;
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PERMISSION_GRANTED:
                    break;
                case MESSAGE_PERMISSION_DENIED:
                    finish();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
