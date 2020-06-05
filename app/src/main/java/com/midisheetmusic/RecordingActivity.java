package com.midisheetmusic;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.lang.Math.pow;

public class RecordingActivity extends AppCompatActivity implements
        RecordingSampler.CalculateVolumeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int RequestPermissionCode = 1;


    private FloatingActionButton mFloatingActionButton;


    //Metronome
    ReadyThread readyThread = new ReadyThread();
    MetronomeThread metronomeThread; //= new MetronomeThread();
    Spinner spinnerBpm;
    Spinner spinnerMeasure;
    ArrayList<Integer> arr = new ArrayList<>();
    ArrayList<String> arr2 = new ArrayList<>();
    ImageView imageview;
    ImageView countview;
    SoundPool soundPool;
    int clap;
    public int[] countArray = {R.drawable.count3, R.drawable.count2, R.drawable.count1};


    boolean isRecording = false;

    int nn, dd;
    int measure;
    String spinnerMSR = "4/4"; //박자
    int spinnerBPM = 60; //BPM
    int count = 0;
    int sampleNumber = 0;
    Long startTime;
    long now;
    Long length;
    int gap=0;
    public  static Vibrator vibrator;


    // Pitch Detection data
    ArrayList<Double> humming = new ArrayList<>();

    //MidiFile 생성을 위함
    static final int DEMISEMIQUAVER = 2; //32분음표(반의반의반박)
    static final int SEMIQUAVER = 4; //16분음표 (반의 반박)
    static final int QUAVER = 8; //8분음표 (|)
    static final int CROTCHET = 16; //4분음표 (V)
    static final int MINIM = 32; //2분음표 (VV)
    static final int SEMIBREVE = 64; //온음표 (VVVV)
    public Context context;



    //tarsoDSP
    //  TarsosDSPAudioFormat tarsosDSPAudioFormat;
    //  AudioDispatcher dispatcher;
    //   File file;
    TextView pitchTextView;
    TextView progressTimeTextView;
    //  String filename = "recorded_sound.wav";

    TextView bpmtext;
    TextView measuretext;


    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    Thread audioThread;
    GraphView realTimeGraph;
    Boolean recordState = false;

    private LineGraphSeries<DataPoint> realTimeSeries; //Series for real-time realTimeGraph
    private double graphLastXValue = 1d;
    ArrayList<Point> recordedPoints;


    String folderName;
    String fileName;

    public Vibrator getVibrator(){
        return vibrator;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);


        folderName = getIntent().getStringExtra("folderName");
        fileName = getIntent().getStringExtra("fileName");

        soundPool = new SoundPool(1,AudioManager.STREAM_MUSIC, 0);
        clap = soundPool.load(this, R.raw.clap, 1);
        pitchTextView = findViewById(R.id.pitchTextView);
        progressTimeTextView = findViewById(R.id.progressTextView);


        countview = findViewById(R.id.countView);
        imageview = findViewById(R.id.imageView);
        if( countview == null){
        }
        else{
            readyThread.setImageView( (ImageView) countview);
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        //Metronome

        imageview = findViewById(R.id.imageView);


        //bpm 설정
        spinnerBpm = findViewById(R.id.bpmSpinner);
        arr.add(60);
        arr.add(64);
        arr.add(70);
        arr.add(74);
        arr.add(80);
        arr.add(84);
        arr.add(90);
        arr.add(94);
        arr.add(100);
        arr.add(104);
        arr.add(110);
        arr.add(114);
        arr.add(120);

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, arr);
        spinnerBpm.setAdapter(arrayAdapter);

        spinnerBpm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spinnerBPM = ((Integer) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMeasure = findViewById(R.id.measureSpinner);

        arr2.add("4/4");
        arr2.add("3/4");

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, arr2);
        spinnerMeasure.setAdapter(arrayAdapter2);

        spinnerMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spinnerMSR = ((String) parent.getSelectedItem());

                if(spinnerMSR == "4/4"){ //4분의4박자(4/2^2)
                    nn = 4;
                    dd = 2;
                    measure = 4;
                }
                else{ //4분의 3박자(3/2^2)
                    nn = 3;
                    dd = 2;
                    measure = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final int[] countFlag = {0};
        final int[] middlestop = {0};
        bpmtext = findViewById(R.id.textView);
        measuretext = findViewById(R.id.textView2);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CountDownTimer timer = new CountDownTimer(60000 / spinnerBPM * nn ,60000 / spinnerBPM) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        //  mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);

                        if(countFlag[0] == 1){

                            countview.setVisibility(View.VISIBLE);
                            countview.setImageResource(countArray[0]);
                            soundPool.play(clap,1f,1f,0,0,1f);
                            count++;

                            vibrator.vibrate(10);

                        }else{//중간에 타이머를 멈췄다...

                            cancel();
                            middlestop[0] = 1;

                        }

                    }

                    public void onFinish() {
                        //bpmtext.setVisibility(View.GONE);
                        //measuretext.setVisibility(View.GONE);
                        //spinnerBpm.setVisibility(View.GONE);
                        //spinnerMeasure.setVisibility(View.GONE);
                        spinnerBpm.setEnabled(false);
                        spinnerMeasure.setEnabled(false);

                        countview.setVisibility(View.GONE);
                        //tarsoDSP
                        now= SystemClock.currentThreadTimeMillis();
                        initPitcher();
                        //메트로놈
                        metronomeThread.setPlaying(true);
                        metronomeThread.start();
                        countFlag[0] = 0;
                        middlestop[0] = 0;
                    }
                };

                if(checkPermission()) {

                    if (isRecording) {
                        countFlag[0] = 0;
                        timer.cancel();
                        if(middlestop[0]==1){
                            middlestop[0]=0;
                            recreate();

                        }


                        else{

                            isRecording = false;

                            mFloatingActionButton.setImageResource(R.drawable.ic_mic);

                            //tasroDSP
                            stopRecording();

                            //메트로놈
                            if (metronomeThread.isPlaying()) {
                                metronomeThread.setPlaying(false);
                                metronomeThread.interrupt();
                                metronomeThread = null;
                            }

                            imageview.setImageResource(R.drawable.sleep);
                        }

                    }


                    else { // 녹음 시작

                        isRecording = true;
                        countFlag[0]=1;
                        metronomeThread = new MetronomeThread();
                        metronomeThread.setBpm(spinnerBPM);
                        metronomeThread.setMeasure(measure);
                        metronomeThread.setImageView(imageview);
                        mFloatingActionButton.setImageResource(R.drawable.ic_mic_off);

                       timer.start();
                       middlestop[0] = 1;

                    }

                } else {

                    requestPermission();
                }

            }
        });


        realTimeGraph = findViewById(R.id.realTimeGraph);

        realTimeSeries = new LineGraphSeries<>();
        realTimeSeries.setColor(Color.rgb(0xF1,0x70,0x68));
        realTimeSeries.setDataPointsRadius(50);
        realTimeSeries.setThickness(10);
        realTimeGraph.addSeries(realTimeSeries);
        realTimeGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        realTimeGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        realTimeGraph.getViewport().setXAxisBoundsManual(true);
        realTimeGraph.getViewport().setMinX(0);
        realTimeGraph.getViewport().setMaxX(100);
        realTimeGraph.getViewport().setMinY(-1);
        realTimeGraph.getViewport().setMaxY(300);



    }


    public static int freq2MidiNum(double freq){
        int MidiNum = 0;
        int octa = 0;
        int mul;
        double C , Db , D, Eb , E, F, Gb, G,Ab, A, Bb, B;
        C = 32.703;
        Db = 34.648;
        D = 36.708;
        Eb = 38.891;
        E = 41.203;
        F = 43.654;
        Gb = 46.249;
        G = 48.999;
        Ab = 51.913;
        A = 55.000;
        Bb = 58.270;
        B = 61.735;

        //계산

        //setp 1 옥타브 계산
        if( C <= freq && freq < 2 * C)
            octa = 1;
        else if( 2 * C <= freq && freq < 4* C)
            octa = 2;
        else if( 4 * C <= freq && freq < 8 * C)
            octa = 3;
        else if(8 * C <= freq && freq < 16 * C)
            octa = 4;
        else if(16 * C <= freq && freq < 32 * C)
            octa = 5;
        else if(32 * C <= freq && freq < 64 * C)
            octa = 6;
        else if(64 * C <= freq && freq < 128 * C)
            octa = 7;
        else if(128 * C <= freq && freq < 256 * C)
            octa = 8;

        mul = (int) pow(2, octa-1); // 1옥타브는 C 2옥타브는 C*2 3옥타브는 C * 2^2 4옥타브는 C*2^3

        //setp 2 음정 계산

        if(freq >= (C * mul) && freq < ( Db* mul )) {
            //C
            if( (freq - (C* mul)) <= ((Db*mul) - freq) )
                MidiNum = 12 * octa + 12;
            else // Db
                MidiNum = 12 * octa + 12 + 1;
        }
        else if(freq >= (Db * mul)  && freq < (D* mul)) {
            //Db
            if( (freq - (Db* mul)) <= ((D*mul) - freq) )
                MidiNum = 12 * octa + 12 + 1 ;
            else //D
                MidiNum = 12 * octa + 12 + 2;
        }
        else if(freq >= (D * mul) && freq < (Eb* mul)) {
            //D
            if( (freq - (D* mul)) <= ((Eb*mul) - freq) )
                MidiNum = 12 * octa + 12 + 2 ;
            else // Eb
                MidiNum = 12 * octa + 12 + 3;
        }
        else if(freq >= (Eb * mul) && freq < (E* mul)) {
            //Eb
            if( (freq - (Eb* mul)) <= ((E*mul) - freq) )
                MidiNum = 12 * octa + 12 + 3 ;
            else // E
                MidiNum = 12 * octa + 12 + 4;
        }
        else if(freq >= (E * mul) && freq < (F* mul)) {
            //E
            if( (freq - (E* mul)) <= ((F*mul) - freq) )
                MidiNum = 12 * octa + 12 + 4 ;
            else // F
                MidiNum = 12 * octa + 12 + 5;
        }
        else if(freq >= (F * mul) && freq <= (Gb* mul)) {
            //F
            if( (freq - (F* mul)) <= ((Gb*mul) - freq) )
                MidiNum = 12 * octa +12 + 5 ;
            else // Gb
                MidiNum = 12 *octa + 12 + 6;
        }
        else if(freq >= (Gb * mul) && freq < (G* mul)) {
            //Gb
            if( (freq - (Gb* mul)) <= ((G*mul) - freq) )
                MidiNum = 12 * octa +12 + 6 ;
            else // G
                MidiNum = 12 * octa + 12 + 7;
        }
        else if(freq >= (G * mul)  && freq < (Ab * mul)) {
            //G
            if( (freq - (G* mul)) <= ((Ab*mul) - freq) )
                MidiNum = 12 * octa +12 + 7 ;
            else // Ab
                MidiNum = 12 * octa + 12 + 8;
        }
        else if(freq >= (Ab * mul) && freq < (A* mul)) {
            //Ab
            if( (freq - (Ab* mul)) <= ((A*mul) - freq) )
                MidiNum = 12 * octa +12 + 8 ;
            else // A
                MidiNum = 12 * octa + 12 + 9;
        }
        else if(freq >= (A * mul) && freq < (Bb* mul)) {
            //A
            if( (freq - (A* mul)) <= ((Bb*mul) - freq) )
                MidiNum = 12 * octa +12 + 9 ;
            else
                MidiNum = 12 * octa + 12 +10;
        }
        else if(freq >= (Bb * mul) && freq < (B* mul)) {
            //Bb
            if( (freq - (Bb* mul)) <= ((B*mul) - freq) )
                MidiNum = 12 * octa +12 + 10 ;
            else // B
                MidiNum = 12 * octa + 12 + 11;
        }
        else if(freq >= (B * mul) && freq < (2 * C * mul)) {
            //B
            if( (freq - (B* mul)) <= ((C*mul) - freq) )
                MidiNum = 12 * octa +12 + 11 ;
            else // C
                MidiNum = 12 * octa + 12 + 12;
        }

        //       System.out.println("octa " + octa + " freq  "+freq + " || MidiNum" + MidiNum);
        return MidiNum;
    }

    //음계 변환해서 list에 담고 return하는 함수
    public static ArrayList<Integer> store(ArrayList<Double> freq){
        ArrayList<Integer> list = new ArrayList();

        for(int i =0; i<freq.size(); i++){
            if(freq.get(i) == -1){
                list.add(-1);
                System.out.println(list.get(i)+" ");
                continue;
            }

            list.add(freq2MidiNum(freq.get(i)));
            System.out.println(list.get(i));
        }

        return list;
    }

    // 녹음 된 시간 측정...
    public static double CalSec(double size){

        double sec = size/60.0;
        return  sec;
    }


    //음계저장한 리스트를 개수 뽑아서 "1차 시퀀스(음계, 개수)" 로 나타내기
    public static ArrayList<Integer> CountMidiNum(ArrayList<Integer> scalelist) {

        ArrayList<Integer> Sequence1 = new ArrayList<>();
        int curMidi;
        int count = 1;
        curMidi = scalelist.get(0);


        for(int i =0; i<scalelist.size();i++){
            if(i==0){
                Sequence1.add(curMidi);
            }

            if(curMidi != scalelist.get(i)){
                Sequence1.add(count);
                curMidi = scalelist.get(i);
                Sequence1.add(curMidi);
                count = 1;
            }
            else{
                count++;
            }

            if(i==scalelist.size()-1){
                Sequence1.add(count);
            }

        }

 /*      while(Sequence1.get(0) == -1){
            Sequence1.remove(0);
            Sequence1.remove(0);
        }*/

        /*if(Sequence1.get(0) == -1){
            Sequence1.remove(0);
            Sequence1.remove(0);
        }*/

        //System.out.println("맨 앞 -1 지우기");

        /*for(int i=1; i<Sequence1.size();i+=2){
            System.out.println("MidiNum : " + Sequence1.get(i-1) + " || Counts : "+ Sequence1.get(i));

        }*/

        return Sequence1; //{음계, 음계개수}
    }

    //2차 시퀀스(1차 시퀀스 정리 및 개수를 노트로 변환)
    public static ArrayList<Integer> ReturnSequence(ArrayList<Integer> Sequence1, int gap, int bpm){

        int quarter_len ; // 4분음표의 길이 단위 : Milli second
        int quarter_note; //4분음표 결정개수
        int sixteenth_note;

        //quarter_len = 60 / bpm *1000;

        //quarter_note = quarter_len / gap;


        if(bpm ==180){
            quarter_note = 250/gap;
        }
        else if(bpm == 120)
            quarter_note = 500/gap;
        else
            quarter_note = 1000/gap;//(bpm == 60)*/

        int white_note = quarter_note*4; //온음표
        int dot_half_note = quarter_note*3;
        int half_note = quarter_note*2; //2분음표 결정개수
        int dot_quarter_note = (int)(quarter_note*1.5);
        int dot_eighth_note = (int)(quarter_note*0.75);
        int eighth_note = quarter_note/2; //8분음표 결정개수
        sixteenth_note = quarter_note/4; //16분음표 결정개수

        ArrayList<Integer> Sequence2 = new ArrayList<Integer>();

        //Sequence2 = {음계, 개수}정리 list (16분음표보다 작은 것 합치기)
        if(Sequence1 == null){
            ArrayList<Integer> nulllist = new ArrayList<>();
            nulllist.add(-1);
            nulllist.add(half_note);
            return nulllist;
        }
        Sequence2.add(Sequence1.get(0));
        Sequence2.add(Sequence1.get(1));
        int count;
        int exCount = 0;
        int k;
        for(int i = 3; i<Sequence1.size();i+=2){

            count = Sequence1.get(i);
            k = Sequence2.size();
            if(count >= sixteenth_note){
                Sequence2.add(Sequence1.get(i-1));
                Sequence2.add(count);
            }
            else{
                exCount = Sequence2.get(k-1);
                Sequence2.set(k-1, exCount + count);
                if(Sequence2.get(1) <sixteenth_note && Sequence2.size() == 4){
                    int first = Sequence2.get(1);
                    int third = Sequence2.get(3);
                    Sequence2.set(3, first + third );
                    Sequence2.remove(0);
                    Sequence2.remove(0);
                }
            }
        }
        // test2
        for (int i = 1; i < Sequence2.size(); i += 2) {
            System.out.println("Second - MidiNum:" + Sequence2.get(i - 1) + " || Counts:" + Sequence2.get(i));
        }
        System.out.println("\n");

        System.out.println("온음표 개수:"+white_note+" note:"+SEMIBREVE);
        System.out.println("점2분음표 개수:"+dot_half_note+" note:"+(MINIM+CROTCHET));
        System.out.println("2분음표 개수:"+half_note+" note:"+MINIM);
        System.out.println("점4분음표 개수:"+dot_quarter_note+" note:"+(CROTCHET + QUAVER));
        System.out.println("4분음표 개수:"+quarter_note+" note:"+CROTCHET);
        System.out.println("점8분음표 개수:"+dot_eighth_note+" note:"+(QUAVER + SEMIQUAVER));
        System.out.println("8분음표 개수:"+eighth_note+" note:"+QUAVER);
        System.out.println("16분음표 개수:"+sixteenth_note+" note:"+SEMIQUAVER);

        System.out.println("gpa : "+gap);


        int midi;
        int i = -1;
        while(true) {
            if(i == Sequence2.size()-1){
                break;
            }
            i+=2;

            count = Sequence2.get(i);
            midi = Sequence2.get(i-1);

            if(count > (white_note + dot_half_note) / 2) { //count 온음표로 표기
                Sequence2.set(i, SEMIBREVE);
                if((count - white_note) > sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - white_note); //큰부분빼서
                    Sequence2.add(i+1,count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else if(count > (dot_half_note + half_note) / 2) { //count가 점2분음표로 표기
                Sequence2.set(i, MINIM+CROTCHET);
                if((count - dot_half_note) >= sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - dot_half_note); //큰부분빼서
                    Sequence2.add(i+1, count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else if(count > (half_note + dot_quarter_note) / 2){ //count 2분음표

                Sequence2.set(i, MINIM);
                if((count - half_note) >= sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - half_note); //큰부분빼서
                    Sequence2.add(i+1, count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else if(count > (dot_quarter_note + quarter_note) / 2) { //점4분음표

                Sequence2.set(i, CROTCHET + QUAVER);
                if((count - dot_quarter_note) >= sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - dot_quarter_note); //큰부분빼서
                    Sequence2.add(i+1, count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else if(count > (quarter_note + dot_eighth_note) / 2) { //4분음표

                Sequence2.set(i, CROTCHET);
                if((count - quarter_note) >= sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - quarter_note); //큰부분빼서
                    Sequence2.add(i+1, count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else if(count > (dot_eighth_note + eighth_note) / 2) { //점 8분음표

                Sequence2.set(i, QUAVER + SEMIQUAVER);
                if((count - dot_eighth_note) >= sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - dot_eighth_note); //큰부분빼서
                    Sequence2.add(i+1, count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else if(count > (eighth_note + sixteenth_note) / 2) { //8분음표

                Sequence2.set(i, QUAVER);
                if((count - eighth_note) >= sixteenth_note ) {//count-온음표가 16분 음표보다 클 때, 붙임줄필요하고 다음 음표를 구한다.
                    count = (int)(count - eighth_note); //큰부분빼서
                    Sequence2.add(i+1, count);
                    Sequence2.add(i+1, midi);
                    continue;
                }
                continue;

            }else { // 16분 음표
                Sequence2.set(i, SEMIQUAVER);
                continue;
            }

        }

 /*       if(Sequence2.get(0) == -1){
            Sequence2.remove(0);
            Sequence2.remove(0);
        }*/
        while(Sequence2.get(0) == -1){
            if(Sequence2.size()==2){
                return Sequence2;
            }
            Sequence2.remove(0);
            Sequence2.remove(0);
        }

        for ( i = 1; i < Sequence2.size(); i += 2) {
            System.out.println("Final - MidiNum:" + Sequence2.get(i - 1) + " || NoteNum:" + Sequence2.get(i));
        }

        return Sequence2;

    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(RecordingActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {


        //메트로놈 정지
        if(metronomeThread != null){

            metronomeThread.setPlaying(false);
            metronomeThread.interrupt();
            metronomeThread = null;

        }

        super.onDestroy();
    }

    @Override
    public void onCalculateVolume(int volume) {
        // for custom implement
        //       Log.d(TAG, String.valueOf(volume));
    }

    public boolean isFlat(int key, int midinum){
        int notFlat[] = { 0,2,4,5,7,9,11}; //Ckey

        for(int i = 0 ; i < notFlat.length ; i++){
            notFlat[i] = (notFlat[i] +key)%12;
        }

        for(int i = 0 ; i < notFlat.length ; i++){
            if(notFlat[i] == midinum%12)
                return true;
        }
        return false;
    }

    public ArrayList<Integer> smoothing ( ArrayList<Integer> seq, int key){

        ArrayList<Integer> sequence ;

        for(int i =0; i < seq.size(); i+=2){
            int midinum = seq.get(i);

            if(isFlat(key, midinum)){
                if( !isFlat(key, midinum+1)){
                    midinum++;
                    seq.set(i, midinum);
                }
                else{
                    if(!isFlat(key, midinum-1)){
                        midinum--;
                        seq.set(i, midinum);
                    }
                }
            }

        }
        return  seq;

    }


    public void stopRecording(){
        length = SystemClock.elapsedRealtime() - startTime;

        releaseDispatcher();
        gap = (int)(length/sampleNumber);


        Log.d("TAG",sampleNumber +"개의 sample");
        Log.d("TAG",length+" MiilliSec");
        Log.d("TAG", "gap : "+ gap);

        //출력
        int line = 0;
        for(int i = 0 ; i < humming.size() ; i++){

            System.out.print(humming.get(i) +",");
            if( line == 20 ){
                line = 0;
                System.out.println("");
            }
            line ++;

        }



        //MidiFile 생성
        MidiFileMaker midiFileMaker = new MidiFileMaker();
        ArrayList<Integer> scalelist = store(humming);
        ArrayList<Integer> sequence1 = CountMidiNum(scalelist);

        //최종시퀀스
        ArrayList<Integer> sequence2 = ReturnSequence(sequence1, gap, spinnerBPM );

        //KeySIgnature 추정 후 smoothing
        int key = KeySignature.Record_guess(sequence2).Notescale();
        Log.v("TAG", "first_key signature은 " + KeySignature.KeyToString(key));
        ArrayList<Integer> sequence = smoothing(sequence2, key);

        int Afterkey = KeySignature.Record_guess(sequence).Notescale();
        Log.v("TAG", "after_key signature은 " + KeySignature.KeyToString(Afterkey));

        midiFileMaker.setTempo(spinnerBPM);
        midiFileMaker.setTimeSignature(dd,nn);
     //   midiFileMaker.setKeySignature(key);
        midiFileMaker.noteSequenceFixedVelocity (sequence, 127);


        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+folderName);
        if(!dir.exists()){
            dir.mkdirs();
        }


        File file = new File(dir, fileName + ".mid") ;
        midiFileMaker.writeToFile (file);

        ((MainActivity)MainActivity.mContext).setAlbum();
        Log.d("HELLO", folderName);

        if(!folderName.equals("Quick"))
            ((ChooseSongActivity)ChooseSongActivity.cContext).loadFile();



        Uri uri = Uri.parse(file.getPath());
        FileUri fileUri = new FileUri(uri, file.getPath());

        Intent intent = new Intent(Intent.ACTION_VIEW, fileUri.getUri() , this, SheetMusicActivity.class);
        intent.putExtra(SheetMusicActivity.MidiTitleID, file.toString());

        startActivity(intent);

        recreate();

    }

    public void releaseDispatcher(){
        if(dispatcher != null)
        {
            if(!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseDispatcher();
        if(metronomeThread != null)
            metronomeThread.setPlaying(false);
    }

    public void initPitcher()
    {
        humming.clear();

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 2048 , 1024);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };


        pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 2048, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);
        audioThread = new Thread(dispatcher, "Audio Thread");

        startTime = SystemClock.elapsedRealtime();
        audioThread.start();
    }


    public void processPitch(float pitchInHz){



        pitchTextView.setText(pitchInHz +"" );
        humming.add((double) pitchInHz);
        sampleNumber++;


        long outTime = SystemClock.elapsedRealtime() - startTime;
        String viewOutTime = String.format("%02d:%02d:%02d", (outTime/1000)/60,(outTime/1000)%60,(outTime%1000)/10);
        progressTimeTextView.setText(viewOutTime);


        if (pitchInHz < 0)
            pitchInHz = 20;

        graphLastXValue += 1d;
        realTimeSeries.appendData(new DataPoint(graphLastXValue, pitchInHz), true, 300);
    }

    class Point{
        public double t;
        public float x;

        public Point(double t, float x)
        {
            this.t = t;
            this.x = x;
        }
    }

}