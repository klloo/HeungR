package com.Osunji;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MusicDB extends RealmObject {
    @PrimaryKey
    private int id;
    private int albumId;
    private String title;
    private byte[] midi;
    private int dd;
    private int nn;
    private int bpm;


    public void setId(int id) { this.id = id; }

    public int getId() { return this.id;}

    public void setAlbumId(int albumId){ this.albumId = albumId; }

    public int getAlbumId(){ return this.albumId; }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){ return this.title; }

    public void setMidi(byte[] midi){
        this.midi = midi;
    }

    public byte[] getMidi(){
        return this.midi;
    }

    public int getDd() {
        return dd;
    }

    public void setDd(int dd) {
        this.dd = dd;
    }

    public int getNn() {
        return nn;
    }

    public void setNn(int nn) {
        this.nn = nn;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

}
