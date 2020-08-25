package com.Osunji;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AccompanimentDB extends RealmObject {
    @PrimaryKey
    private int id;
    public byte[] midi;

    public void setId(int id) { this.id = id; }

    public int getId() { return this.id;}

    public void setMidi(byte[] midi){
        this.midi = midi;
    }

    public byte[] getMidi(){
        return this.midi;
    }
}
