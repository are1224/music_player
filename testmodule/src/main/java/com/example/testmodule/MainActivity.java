package com.example.testmodule;

import android.Manifest;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MESSAGE_PERMISSION_GRANTED = 101;
    private static final int MESSAGE_PERMISSION_DENIED = 102;

    private RecyclerView recyclerView;
    private List<Song> songList;
    private TextView textView;
    private MyRecyclerAdapter adapter;

    public MainHandler mainHandler = new MainHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        //레이아웃 매니저로 LinearLayoutManager를 설정
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        showPermissionDialog();
        textView = (TextView) findViewById(R.id.permission);
        textView.setText("Disable write to external storage.");

        new AdapterSetting().execute();

    }

    class AdapterSetting extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            String[] projection = {MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE};
            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC"
            );

            songList = new ArrayList<>();
            if (cursor != null){
                Log.d("cursor is ?", "cursor 있냐?");
                while(cursor.moveToNext()){
                    Log.d("cursor how many?", "커서는 몇개냐?");
                    if (cursor.getInt(0) != 0){
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
//            super(songList);
            adapter = new MyRecyclerAdapter(songList);
            recyclerView.setAdapter(adapter);
        }
    }

    public void getSize(View view) {
        Toast.makeText(MainActivity.this, "크기 : "+songList.size(), Toast.LENGTH_SHORT).show();
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

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PERMISSION_GRANTED:
                    textView.setText("Enable write to external storage.");
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
