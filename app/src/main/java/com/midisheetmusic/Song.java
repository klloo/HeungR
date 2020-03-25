package com.midisheetmusic;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Song {

    public String id;
    public String album;
    public String title;
    public String key; // 단조 장조
    public int quater_note; // 몇박자인지
    public int bpm; //bmp 정보
    public List<String>  pitch;
    public List<String>  chord;
    public List<Integer> midi;
    public double total_length;


    public Song(){
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public Song(String id, String album, String title, String key, int quater_note , int bpm, List<String> pitch , List<String>  chord, double total_length , List<Integer>  midi){
        this.id = id;
        this.title = title;
        this.album=album;
        this.quater_note = quater_note;
        this.bpm=bpm;
        this.key = key;
        this.pitch = pitch;
        this.chord=chord;
        this.total_length = total_length;
        this.midi = midi;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("quater_note", quater_note);
        result.put("key", key);
        result.put("total_length", total_length);
        return result;
    }


}