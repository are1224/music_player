package com.example.bulle.musicplayerproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicListFragment.OnMusicSelectedListener, MusicListFragment.OnEnrollMusicList, MusicPlayerFragment.OnMusicServiceListener{

    private static final int MESSAGE_PERMISSION_GRANTED = 101;
    private static final int MESSAGE_PERMISSION_DENIED = 102;

    private MusicListFragment musicListFragment;

    private ArrayList<Song> musicList;

    private MusicService mService;
    private boolean mBound;

    private boolean music_play_or_stop = true;

    private BroadcastReceiver musicPlayerReceiver;

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

    @Override
    protected void onStart(){
        super.onStart();
        //서비스에 바인딩
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(musicPlayerReceiver);

        //서비스 연결 해제
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    //바인드서비스를 통해 서비스와 연결될 때의 콜백 정의
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //예기치 않은 종료
        }
    };

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
//        Intent intent = new Intent(this, MusicService.class);
//        stopService(intent);
        mService.stopMusic();
    }

    public void onMusicSelected(ArrayList<Song> songList, int position){

        if (musicList != null) {
            mService.setSongList(musicList);
        }

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

    @Override
    public MusicService getMusciService() {
        return mService;
    }

    @Override
    public void setMusicState(boolean state) {
        music_play_or_stop = state;
    }

    @Override
    public boolean getMusicState() {
        return music_play_or_stop;
    }

    @Override
    public void setMusicPlayerReceiver(BroadcastReceiver musicPlayerReceiver) {
        this.musicPlayerReceiver = musicPlayerReceiver;
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
