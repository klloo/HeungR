package com.Osunji;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AlbumDB extends RealmObject {

    @PrimaryKey private int id;
    private String albumTitle;
    private byte[] coverImage;

    public void setAlbumInfo(String albumTitle, byte[] coverImage){
        this.albumTitle = albumTitle;
        this.coverImage = coverImage;
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
}