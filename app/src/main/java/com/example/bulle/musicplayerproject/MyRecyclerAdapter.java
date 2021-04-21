package com.example.bulle.musicplayerproject;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>{

    private final List<Song> songList;
    private MyRecyclerViewClickListener mListener;

    public MyRecyclerAdapter(List<Song> list){
        songList = list;
    }
    public void setOnClickListener(MyRecyclerViewClickListener listener){
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.title.setText(song.getMusicTitle());

        if (mListener != null){
            final int pos = position;
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mListener.onItemClicked(pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    //각각의 아이템의 레퍼런스를 저장할 뷰 홀더 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        public ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
        }
    }

    public interface MyRecyclerViewClickListener{
        //아이템 전체 부분 클릭
        void onItemClicked(int position);
    }
}