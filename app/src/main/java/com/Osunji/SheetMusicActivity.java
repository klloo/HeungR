/*
 * Copyright (c) 2011-2012 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package com.Osunji;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.drawerlayout.widget.DrawerLayout;

import com.Osunji.drawerItems.ExpandableSwitchDrawerItem;
import com.Osunji.sheets.ClefSymbol;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.zip.CRC32;

import es.dmoral.toasty.Toasty;

/**
 * SheetMusicActivity is the main activity. The main components are:
 * <ul>
 *  <li> MidiPlayer : The buttons and speed bar at the top.
 *  <li> Piano : For highlighting the piano notes during playback.
 *  <li> SheetMusic : For highlighting the sheet music notes during playback.
 */
public class SheetMusicActivity extends MidiHandlingActivity {

    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;
    public static final int ID_LOOP_ENABLE = 10;
    public static final int ID_LOOP_START = 11;
    public static final int ID_LOOP_END = 12;
    public Uri uri;
    public String title;

    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* The layout */
    private MidiFile midifile;   /* The midi file to play */
    public MidiOptions options; /* The options for sheet music and sound */
    private long midiCRC;        /* CRC of the midi bytes */
    private Drawer drawer;

    String folderName = "banju";
    String fileName;


    int octa = 0;
    int count = 0;


    /** Create this SheetMusicActivity.
      * The Intent should have two parameters:
      * - data: The uri of the midi file to open.
      * - MidiTitleID: The title of the song (String)
      */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        BaseActivity.actList.add(this);

        // Hide the navigation bar before the views are laid out
        hideSystemUI();

        setContentView(R.layout.sheet_music_layout);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ClefSymbol.LoadImages(this);
        TimeSigSymbol.LoadImages(this);



        // Parse the MidiFile from the raw bytes

        uri = this.getIntent().getData();
        if (uri == null) {
            this.finish();
            return;
        }

        title = this.getIntent().getStringExtra(MidiTitleID);
        if (title == null) {
            title = uri.getLastPathSegment();
        }

        TextView songname = findViewById(R.id.sheet1Title);
        songname.setText(uri.getLastPathSegment().substring(0,uri.getLastPathSegment().length()-4));
        TextView albumname = findViewById(R.id.sheet1Folder);
        albumname.setText(uri.getPathSegments().get(4));
     //   Log.v("TAG", uri.getPathSegments().toString());

        FileUri file = new FileUri(uri, title);
        this.setTitle("MidiSheetMusic: " + title);
        byte[] data;
        try {
            data = file.getData();
            midifile = new MidiFile(data, title);
        }
        catch (MidiFileException e) {
            this.finish();
            return;
        }



        // Initialize the settings (MidiOptions).
        // If previous settings have been saved, use those

        options = new MidiOptions(midifile);
        CRC32 crc = new CRC32();
        crc.update(data);
        midiCRC = crc.getValue();
        SharedPreferences settings = getPreferences(0);
        options.scrollVert = settings.getBoolean("scrollVert", true);
        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
        String json = settings.getString("" + midiCRC, null);
        MidiOptions savedOptions = MidiOptions.fromJson(json);
        if (savedOptions != null) {
            options.merge(savedOptions);
        }


        createViews();

        init();


        printSequence(getSequence());

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void init() {

        ImageView temp = findViewById(R.id.imageView3);
        temp.setImageDrawable(getResources().getDrawable(R.drawable.music_96));

        ImageButton backButton = findViewById(R.id.btn_back);
        ImageButton rewindButton = findViewById(R.id.btn_rewind);
        ImageButton resetButton = findViewById(R.id.btn_replay);
        ImageButton playButton = findViewById(R.id.btn_play);
        ImageButton fastFwdButton = findViewById(R.id.btn_forward);
        ImageButton settingsButton = findViewById(R.id.btn_settings);

        player.setMidiButton(findViewById(R.id.btn_midi));


        ImageButton upButton = findViewById(R.id.up_button);
        upButton.setOnClickListener(v -> upNote());

        ImageButton downButton = findViewById(R.id.down_button);
        downButton.setOnClickListener(v -> downNote());

        ImageButton saveButton = findViewById(R.id.save_btn);
        saveButton.setOnClickListener( v -> save());

        ImageButton chordButton = findViewById(R.id.chord);
        chordButton.setOnClickListener( v -> makeMidiFile());

        backButton.setOnClickListener(v -> this.onBackPressed());
        rewindButton.setOnClickListener(v -> player.Rewind());
        resetButton.setOnClickListener(v -> player.Reset());
        playButton.setOnClickListener(v -> player.Play());
        fastFwdButton.setOnClickListener(v -> player.FastForward());

        settingsButton.setOnClickListener(v -> {
            drawer.deselect();
            drawer.openDrawer();
        });


    }

    public void downNote()
    {
        int midiNote = 60 ;

        ArrayList<MidiTrack> tracks = midifile.getTracks();
        ArrayList<ArrayList<MidiEvent>> allevents = midifile.getAllEvents();

        ArrayList<MidiEvent> events = allevents.get(0);
        MidiTrack  track = tracks.get(0);
        ArrayList<MidiNote> notes = track.getNotes();

        for(MidiNote note : notes){
            if(note.getStartTime() == (int)player.currentPulseTime){

                //note.setNumber(midiNote -1);
                midiNote = note.getNumber();
                if(midiNote <= 0)
                    return;
                note.setNumber(note.getNumber() -1);

                Log.d("Note", note.getNumber() +"");

            }
        }
        for (MidiEvent event : events) {
            if(event.StartTime == (int)player.currentPulseTime){
                event.Notenumber = (byte)(midiNote -1);
                Log.d("Note", event.Notenumber+"");
            }
        }



        createSheetMusic(options);


    }

    public void upNote()
    {
        int midiNote = 60 ;

        ArrayList<MidiTrack> tracks = midifile.getTracks();
        ArrayList<ArrayList<MidiEvent>> allevents = midifile.getAllEvents();

        ArrayList<MidiEvent> events = allevents.get(0);
        MidiTrack  track = tracks.get(0);
        ArrayList<MidiNote> notes = track.getNotes();


        for(MidiNote note : notes){
            if(note.getStartTime() == (int)player.currentPulseTime){

                //note.setNumber(midiNote -1);
                midiNote = note.getNumber();
                if(midiNote >= 120)
                    return;
                note.setNumber(note.getNumber() +1);

                Log.d("Note", note.getNumber() +"");

            }
        }


        for (MidiEvent event : events) {
            if(event.StartTime == (int)player.currentPulseTime){
                event.Notenumber = (byte)(midiNote +1);
                Log.d("Note", event.Notenumber+"");
            }
        }



        createSheetMusic(options);


    }

    public void save(){


        FileOutputStream fos = null;
        File file = new File( uri.getPath() );
        Log.d("TAG", uri.getPath().toString());

        try {
            fos = new FileOutputStream(file);
            Log.d("TAG", "heere");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("TAG", e.toString() +"error");

        }

        player.save(fos);

        Toasty.custom(this, "수정내용을 저장했습니다", R.drawable.music_96, R.color.Greenery,  Toast.LENGTH_SHORT, true, true).show();

    }

    /* Create the MidiPlayer and Piano views */
    void createViews() {

        layout = findViewById(R.id.sheet_content);

  /*      SwitchDrawerItem scrollVertically = new SwitchDrawerItem()
                .withName(R.string.scroll_vertically)
                .withChecked(options.scrollVert)
                .withOnCheckedChangeListener((iDrawerItem, compoundButton, isChecked) -> {
                    options.scrollVert = isChecked;
                    createSheetMusic(options);
                });*/

        SwitchDrawerItem useColors = new SwitchDrawerItem()
                .withName(R.string.use_note_colors)
                .withChecked(options.useColors)
                .withOnCheckedChangeListener((iDrawerItem, compoundButton, isChecked) -> {
                    options.useColors = isChecked;
                    createSheetMusic(options);
                });

        SecondarySwitchDrawerItem showMeasures = new SecondarySwitchDrawerItem()
                .withName(R.string.show_measures)
                .withLevel(2)
                .withChecked(options.showMeasures)
                .withOnCheckedChangeListener((iDrawerItem, compoundButton, isChecked) -> {
                    options.showMeasures = isChecked;
                    createSheetMusic(options);
                });

        SecondaryDrawerItem loopStart = new SecondaryDrawerItem()
                .withIdentifier(ID_LOOP_START)
                .withBadge(Integer.toString(options.playMeasuresInLoopStart + 1))
                .withName(R.string.play_measures_in_loop_start)
                .withLevel(2);

        SecondaryDrawerItem loopEnd = new SecondaryDrawerItem()
                .withIdentifier(ID_LOOP_END)
                .withBadge(Integer.toString(options.playMeasuresInLoopEnd + 1))
                .withName(R.string.play_measures_in_loop_end)
                .withLevel(2);

        ExpandableSwitchDrawerItem loopSettings = new ExpandableSwitchDrawerItem()
                .withIdentifier(ID_LOOP_ENABLE)
                .withName(R.string.loop_on_measures)
                .withChecked(options.playMeasuresInLoop)
                .withOnCheckedChangeListener((iDrawerItem, compoundButton, isChecked) -> {
                    options.playMeasuresInLoop = isChecked;
                })
                .withSubItems(showMeasures, loopStart, loopEnd);


        // Drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withInnerShadow(true)
                .addDrawerItems(
                     //   scrollVertically,
                        useColors,
                        loopSettings,
                        new DividerDrawerItem()
                )
                .inflateMenu(R.menu.sheet_menu)
                .withOnDrawerItemClickListener((view, i, item) -> drawerItemClickListener(item))
                .withDrawerGravity(Gravity.LEFT)
                .build();


        // Make sure that the view extends over the navigation buttons area
        drawer.getDrawerLayout().setFitsSystemWindows(false);


        // Lock the drawer so swiping doesn't open it
        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        player = new MidiPlayer(this);
        player.setDrawer(drawer);
        //layout.addView(player);


        layout.requestLayout();

        player.setSheetUpdateRequestListener(() -> createSheetMusic(options));

        createSheetMusic(options);

    }

    /** Create the SheetMusic view with the given options */
    private void
    createSheetMusic(MidiOptions options) {

        if (sheet != null) {
            layout.removeView(sheet);
        }

        sheet = new SheetMusic(this);
        sheet.init(midifile, options);
        sheet.setPlayer(player);
        layout.addView(sheet);

        player.SetMidiFile(midifile, options, sheet);
        player.updateToolbarButtons();
        layout.requestLayout();

        sheet.draw();

    }



    public  ArrayList<Integer> getSequence(){
        ArrayList<Integer> sequence = new ArrayList<Integer>();

        ArrayList<MidiTrack> tracks = midifile.getTracks();


        int time = 0;

        MidiTrack track = tracks.get(0);
        ArrayList<MidiNote> notes = track.getNotes();
        for (MidiNote note : notes) {
            if( time != note.getStartTime()){
                sequence.add(-1);
                sequence.add( note.getStartTime() - time);
                time = note.getStartTime();
            }
            sequence.add(note.getNumber());
            sequence.add(note.getDuration());
            time +=  note.getDuration();
        }


     //   System.out.println("track0");
      //  printSequence(sequence);




        return sequence;
    }



    int getKeySignature(){
        ArrayList<MidiTrack> tracks = midifile.getTracks();
        KeySignature key = SheetMusic.GetKeySignature(tracks);
        int new_key = key.getNumber();
        Log.v("TAG", "getkeySignature은 " + key.toString());
        return new_key;
    }

    ArrayList<ArrayList<Integer>> getBars(){
        ArrayList<Integer> temp = new ArrayList<>();
        ArrayList<ArrayList<Integer>> bars = new ArrayList<>();
        ArrayList<MidiTrack> tracks = midifile.getTracks();
        MidiTrack track = tracks.get(0);
        ArrayList<MidiNote> notes = track.getNotes();
        TimeSignature timeSignature = midifile.getTimesig();
        int nn = timeSignature.getNumerator();
        int gap = (nn==3) ? 16*3 : 16*2;
        int len = track.getLength();
        int count = 0;
        gap++;
        for(int time = 0 ; time <= len ; time+=gap){
            for(MidiNote note : notes){
                if(note.isPlaying(time)){
                    //추가

                    temp.add(note.getNumber());
                    count ++;

                    // nn이 4면 짝수일때마다 nn이 3이면 매번 새로움
                    if( ( (count % (nn-2) ) == 0 ) ){ //새로운 마디이면
                        if( temp.size() != 0){
                            bars.add(temp);
                            temp = new ArrayList<>();
                        }
                    }

                    break;
                }

            }

        }

        if(temp.size() != 0)
            bars.add(temp);


        Log.d("TAG", "bars : " + bars.toString());
        return  bars;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    ArrayList<ArrayList<Integer>> makeChords(){
        int[] score = {0, 0, 0, 0, 0,0};

        ArrayList<ArrayList<Integer>> banjuList = new ArrayList<>();
        ArrayList<Integer> banju = new ArrayList<>();
        int  key = getKeySignature();
        ArrayList<ArrayList<Integer>> bars = getBars();
        //ex { 0, 4, 0, 4};
        // 가중치는 순서대로 11, 9 ,7, 5
        int[][] ChordTable = {  {0,4,7} ,        // C
                {2,5,0,9} ,    // D-7
                {4,7,2,11},     // E-7
                {5,9,0},          // F
                {7,11,5,2} ,    // G7
                {9,0,7,4}     // A-7
        } ;
        // 가중치는 - 100
        int[][] AvoidTable ={   {5} ,        // C
                {11} ,    // D-7
                { 4, 0},     // E-7
                { 11},          // F
                {0} ,    // G7
                {5 }     // A-7
        } ;
        //값 업데이트
        for(int i = 0 ; i < 6 ; i++){
            for(int j= 0 ; j < ChordTable[i].length ; j++)
                ChordTable[i][j] = (ChordTable[i][j] +=key)%12;

            for(int j= 0 ; j < AvoidTable[i].length ; j++)
                AvoidTable[i][j] = (AvoidTable[i][j] +=key)%12;
        }


        for (ArrayList<Integer> bar : bars){
            // score 계산

            for( int ele : bar){

                for(int i = 0 ; i < 6 ; i++){
                    //Avoid Table 계산

                    for(int j = 0 ; j < AvoidTable[i].length ; j++){
                        if(AvoidTable[i][j] == (ele%12) )
                            score[i] -= 100;
                            octa += ele;
                            count ++;
                    }
                    // Chord table 계산
                    if( score[i] < 0)
                        continue; //Avoid 있으면 계산할 가치없음 그냥 x

                    for(int j = 0 ; j < ChordTable[i].length ; j++){
                        if(ChordTable[i][j] == (ele%12) ) {
                            score[i] += (11 - (j * 2));
                            octa += ele;
                            count++;
                        }
                    }
                }
            }

            ArrayList<Pair<Integer,Integer>> scores = new ArrayList<>();
            for(int i = 0; i<6; i++)
                scores.add(new Pair<Integer, Integer>(score[i],i));

            //sort
            scores.sort(new mycmp());
            System.out.println(bar + "  " + scores.toString());

            for(int i=0;i<6;i++){
                if(scores.get(i).first >= 0)
                    banju.add(scores.get(i).second);
            }
            //반주에
            banjuList.add(banju);
            banju = new ArrayList<>();
        }
        System.out.println("banjuList : " + banjuList.toString());
        System.out.println("average  : " + octa/count);

        return banjuList;//  ( 마디별로 코드 하나씩 )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void makeMidiFile(){
        ArrayList<Integer> sequence = getSequence();
        ArrayList<ArrayList<Integer>> banju = makeChords();

        TimeSignature timeSignature = midifile.getTimesig();
        int nn = timeSignature.getNumerator();
        int bpm = (60 * 1000000) / timeSignature.getTempo();
        int key = (getKeySignature() -3 ) % 12 ;
        // A가 0
        MidiFileMaker2 midiFileMaker = new MidiFileMaker2();


        midiFileMaker.setTempo(bpm);
        midiFileMaker.setTimeSignature(2, nn);
   //     midiFileMaker.setKeySignature(key);
        midiFileMaker.noteSequenceFixedVelocity(sequence, 127);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/" + folderName);

        if(!dir.exists()){
            dir.mkdirs();
        }


        Log.d("TAG", "sheet) title  : " +  uri.getLastPathSegment());

        String newtitle = uri.getLastPathSegment();
        File file = new File(dir, newtitle) ;

        midiFileMaker.writeToFile(file, banju,key, nn, 127 , octa/count );

      /*  //여기 조건문 하나 달려야 함 파일 생성시간 비교하는.. 누군가 하세요
        if(!file.exists()) //존재하지않을때 파일생성

        else{ // 존재할때 확인
            File hummingAlbum = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"
                    +uri.getPathSegments().get(4));
            File humming = new File(hummingAlbum, newtitle);
            if( file.lastModified() < humming.lastModified()){

                midiFileMaker.writeToFile(file, banju,key, nn, 127);
            }

        }*/

        Uri uri2 = Uri.parse(file.getPath());
        FileUri fileUri = new FileUri(uri2, file.getPath());

        Intent intent = new Intent(Intent.ACTION_VIEW, fileUri.getUri() , this, SheetMusicActivity2.class);
        intent.putExtra(SheetMusicActivity.MidiTitleID, file.toString());

        //반주 코드 배열 전달
        ArrayList<Integer> lenInfo = new ArrayList<>();
        ArrayList<Integer> banjuInfo = new ArrayList<>();
        int len = banju.size();
        for(int i=0;i<len;i++) {
            lenInfo.add(banju.get(i).size());
            for(int b:banju.get(i))
                banjuInfo.add(b);
        }


        intent.putExtra("lenInfo",lenInfo);
        intent.putExtra("banjuInfo",banjuInfo);
        intent.putExtra("key",key);


        startActivity(intent);
    }


    public void printSequence(ArrayList<Integer> seq ){
        Log.d("seq", "getSeqnece");
        for(int i = 0 ; i < seq.size() -1 ; i+=2){
            Log.d("seq", seq.get(i) +" | " + seq.get(i+1));

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /** Create a string list of the numbers between listStart and listEnd (inclusive) */
    private String[] makeStringList(int listStart, int listEnd) {
        String[] list = new String[listEnd];
        for (int i = 0; i < list.length; i++) {
            list[i] = Integer.toString(i + listStart);
        }
        return list;
    }


    /** Handle clicks on the drawer menu */
    public boolean drawerItemClickListener(IDrawerItem item) {
        switch ((int)item.getIdentifier()) {
            case R.id.song_settings:
                changeSettings();
                drawer.closeDrawer();
                break;
            case R.id.save_images:
                showSaveImagesDialog();
                drawer.closeDrawer();
                break;
            case ID_LOOP_START:
                // Note that we display the measure numbers starting at 1,
                // but the actual playMeasuresInLoopStart field starts at 0.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.play_measures_in_loop_start);
                String[] items = makeStringList(1, options.lastMeasure + 1);
                builder.setItems(items, (dialog, i) -> {
                    options.playMeasuresInLoopStart = Integer.parseInt(items[i]) - 1;
                    // Make sure End is not smaller than Start
                    if (options.playMeasuresInLoopStart > options.playMeasuresInLoopEnd) {
                        options.playMeasuresInLoopEnd = options.playMeasuresInLoopStart;
                        drawer.updateBadge(ID_LOOP_END, new StringHolder(items[i]));
                    }
                    ((SecondaryDrawerItem) item).withBadge(items[i]);
                    drawer.updateItem(item);
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getListView().setSelection(options.playMeasuresInLoopStart);
                break;
            case ID_LOOP_END:
                // Note that we display the measure numbers starting at 1,
                // but the actual playMeasuresInLoopEnd field starts at 0.
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.play_measures_in_loop_end);
                items = makeStringList(1, options.lastMeasure + 1);
                builder.setItems(items, (dialog, i) -> {
                    options.playMeasuresInLoopEnd = Integer.parseInt(items[i]) - 1;
                    // Make sure End is not smaller than Start
                    if (options.playMeasuresInLoopStart > options.playMeasuresInLoopEnd) {
                        options.playMeasuresInLoopStart = options.playMeasuresInLoopEnd;
                        drawer.updateBadge(ID_LOOP_START, new StringHolder(items[i]));
                    }
                    ((SecondaryDrawerItem) item).withBadge(items[i]);
                    drawer.updateItem(item);
                });
                alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getListView().setSelection(options.playMeasuresInLoopEnd);
                break;
        }
        return true;
    }


    /** To change the sheet music options, start the SettingsActivity.
     *  Pass the current MidiOptions as a parameter to the Intent.
     *  Also pass the 'default' MidiOptions as a parameter to the Intent.
     *  When the SettingsActivity has finished, the onActivityResult()
     *  method will be called.
     */
    private void changeSettings() {
        MidiOptions defaultOptions = new MidiOptions(midifile);
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.settingsID, options);
        intent.putExtra(SettingsActivity.defaultSettingsID, defaultOptions);
        startActivityForResult(intent, settingsRequestCode);
    }


    /* Show the "Save As Images" dialog */
    private void showSaveImagesDialog() {
         LayoutInflater inflator = LayoutInflater.from(this);
         final View dialogView= inflator.inflate(R.layout.save_images_dialog, layout, false);
         final EditText filenameView = dialogView.findViewById(R.id.save_images_filename);
         filenameView.setText(midifile.getFileName().replace("_", " ") );
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.save_images_str);
         builder.setView(dialogView);
         builder.setPositiveButton("OK",
                 (builder1, whichButton) -> saveAsImages(filenameView.getText().toString()));
         builder.setNegativeButton("Cancel",
                 (builder12, whichButton) -> {
         });
         AlertDialog dialog = builder.create();
         dialog.show();
    }


    /* Save the current sheet music as PNG images. */
    private void saveAsImages(String name) {
        String filename = name;
        try {
            filename = URLEncoder.encode(name, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Error: unsupported encoding in filename", Toast.LENGTH_SHORT).show();
        }
        if (!options.scrollVert) {
            options.scrollVert = true;
            createSheetMusic(options);
        }
        try {
            int numpages = sheet.GetTotalPages();
            for (int page = 1; page <= numpages; page++) {
                Bitmap image= Bitmap.createBitmap(SheetMusic.PageWidth + 40, SheetMusic.PageHeight + 40, Bitmap.Config.ARGB_8888);
                Canvas imageCanvas = new Canvas(image);
                sheet.DrawPage(imageCanvas, page);
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MidiSheetMusic");
                File file = new File(path, "" + filename + page + ".png");
                path.mkdirs();
                OutputStream stream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 0, stream);
                stream.close();

                // Inform the media scanner about the file
                MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null, null);
            }
        }
        catch (IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Error saving image to file " + Environment.DIRECTORY_PICTURES + "/MidiSheetMusic/" + filename  + ".png");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", (dialog, id) -> { });
            AlertDialog alert = builder.create();
            alert.show();
        }
        catch (NullPointerException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Ran out of memory while saving image to file " + Environment.DIRECTORY_PICTURES + "/MidiSheetMusic/" + filename  + ".png");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", (dialog, id) -> {});
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    /** Show the HTML help screen. */
    private void showHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    /** Save the options in the SharedPreferences */
    private void saveOptions() {
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putBoolean("scrollVert", options.scrollVert);
        editor.putInt("shade1Color", options.shade1Color);
        editor.putInt("shade2Color", options.shade2Color);
        for (int i = 0; i < options.noteColors.length; i++) {
            editor.putInt("noteColor" + i, options.noteColors[i]);
        }
        String json = options.toJson();
        if (json != null) {
            editor.putString("" + midiCRC, json);
        }
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseActivity.actList.remove(this);
    }

    @Override
    public void onBackPressed() {
        for(Object act : BaseActivity.actList)
            ((Activity)act).finish();
        finish();
        saveOptions();
    }

    /** This is the callback when the SettingsActivity is finished.
     *  Get the modified MidiOptions (passed as a parameter in the Intent).
     *  Save the MidiOptions.  The key is the CRC checksum of the midi data,
     *  and the value is a JSON dump of the MidiOptions.
     *  Finally, re-create the SheetMusic View with the new options.
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
        if (requestCode != settingsRequestCode) {
            return;
        }
        options = (MidiOptions)
            intent.getSerializableExtra(SettingsActivity.settingsID);

        // Check whether the default instruments have changed.
        for (int i = 0; i < options.instruments.length; i++) {
            if (options.instruments[i] !=
                midifile.getTracks().get(i).getInstrument()) {
                options.useDefaultInstruments = false;
            }
        }

        saveOptions();

        // Recreate the sheet music with the new options
        createSheetMusic(options);
    }

    /** When this activity resumes, redraw all the views */
    @Override
    protected void onResume() {
        super.onResume();
        layout.requestLayout();
        player.invalidate();
        if (sheet != null) {
            sheet.invalidate();
        }
        layout.requestLayout();
    }

    /** When this activity pauses, stop the music */
    @Override
    protected void onPause() {
        if (player != null) {
            player.Pause();
        }
        super.onPause();
    }

    @Override
    void OnMidiDeviceStatus(boolean connected) {
        player.OnMidiDeviceStatus(connected);
    }

    @Override
    void OnMidiNote(int note, boolean pressed) {
        player.OnMidiNote(note, pressed);
    }

    /************************** Hide navigation buttons **************************/

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables sticky immersive mode.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}

class mycmp implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        return Integer.compare(((Pair<Integer,Integer>)o2).first,((Pair<Integer,Integer>)o1).first);
    }
}