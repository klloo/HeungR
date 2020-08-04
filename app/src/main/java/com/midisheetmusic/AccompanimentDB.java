package com.midisheetmusic;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AccompanimentDB extends RealmObject {
    @PrimaryKey
    private int id;
    private byte[] midi;

    public void setId(int id) { this.id = id; }

    public int getId() { return this.id;}

    public void setMidi(byte[] midi){
        this.midi = midi;
    }

    public byte[] getMidi(){
        return this.midi;
    }
}
