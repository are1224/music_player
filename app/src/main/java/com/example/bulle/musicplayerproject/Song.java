package com.example.bulle.musicplayerproject;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{

    private String musicId;
    private String musicTitle;

    public Song() {
    }

    public Song(Parcel in) {
        readFromParcel(in);
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(musicId);
        parcel.writeString(musicTitle);
    }

    private void readFromParcel(Parcel in){
        musicId = in.readString();
        musicTitle = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

}