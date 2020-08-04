package com.midisheetmusic;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AlbumDB extends RealmObject {

    @PrimaryKey private int id;
    private String albumTitle;
    private byte[] coverImage;
    private int trackNum;

    public void setAlbumInfo(String albumTitle, byte[] coverImage,int trackNum){
        this.albumTitle = albumTitle;
        this.coverImage = coverImage;
        this.trackNum = trackNum;
    }
    public int getId() {
        return id;
    }
    public String getAlbumTitle() {
        return albumTitle;
    }
    public byte[] getCoverImage() {
        return coverImage;
    }
    public int getTrackNum() {
        return trackNum;
    }
}