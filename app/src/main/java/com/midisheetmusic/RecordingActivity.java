package com.midisheetmusic;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.GenericArrayType;
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
import be.tarsos.dsp.writer.WriterProcessor;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RecordingActivity extends AppCompatActivity implements
        RecordingSampler.CalculateVolumeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int RequestPermissionCode = 1;

    // Recording Info
    private RecordingSampler mRecordingSampler;

    // View
    private VisualizerView mVisualizerView;
    private VisualizerView mVisualizerView2;
    private VisualizerView mVisualizerView3;
    private FloatingActionButton mFloatingActionButton;


    //Metronome
    ReadyThread readyThread = new ReadyThread();
    MetronomeThread metronomeThread; //= new MetronomeThread();
    Spinner spinner;
    ArrayList<Integer> arr = new ArrayList<>();
    ImageView imageview;
    ImageView countview;
    boolean done = true;
    int spinnerBPM = 60;
    int count = 0;

    SoundPool soundPool;
    int clap;

    List<Integer> humming = new ArrayList<>();


    public int[] countArray = {R.drawable.count3, R.drawable.count2, R.drawable.count1};

    //tarsoDSP
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;
    File file;
    TextView pitchTextView;
    String filename = "recorded_sound.wav";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        soundPool = new SoundPool(1,AudioManager.STREAM_MUSIC, 0);
        clap = soundPool.load(this, R.raw.clap, 1);

        //tarsoDSP 객체 설정
        tarsosDSPAudioFormat=new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED, //encoding형식
                60, //sampleRate
                2 * 8, // SampleSizeInBit
                1, // Channels
                2 * 1, // frameSize
                22050, //frameRate
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder())); // 바이트 순서 형식

        pitchTextView = findViewById(R.id.pitchTextView);
        File sdCard = Environment.getExternalStorageDirectory();
        file = new File(sdCard, filename);

        countview = findViewById(R.id.countView);

        imageview = findViewById(R.id.imageView);
        if( countview == null){
            Log.d("TAG","countView is NULL");
        }
        else{

            Log.d("TAG", "countView  "+countview);

            readyThread.setImageView( (ImageView) countview);
        }

        {
            mVisualizerView = (VisualizerView) findViewById(R.id.visualizer);
            ViewTreeObserver observer = mVisualizerView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mVisualizerView.setBaseY(mVisualizerView.getHeight());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                        mVisualizerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mVisualizerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }

        {
            mVisualizerView2 = (VisualizerView) findViewById(R.id.visualizer2);
            ViewTreeObserver observer = mVisualizerView2.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mVisualizerView2.setBaseY(mVisualizerView2.getHeight() / 5);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mVisualizerView2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mVisualizerView2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }

        mVisualizerView3 = (VisualizerView) findViewById(R.id.visualizer3);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        // create AudioRecord
        mRecordingSampler = new RecordingSampler();
        mRecordingSampler.setVolumeListener(this);
        mRecordingSampler.setSamplingInterval(100);
        mRecordingSampler.link(mVisualizerView);
        mRecordingSampler.link(mVisualizerView2);
        mRecordingSampler.link(mVisualizerView3);


        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("TAG", "Icon Clicked");
                if(checkPermission()) {

                    Log.d("TAG", "OK");
                    if (mRecordingSampler.isRecording()) {

                        //녹음 끝나고 바로 넘길거면 여기다가 다음 액티비티로 넘기는 코드 넣으면 됨 그리고 어떤 파일 형식을 원하는지 몰라서 일단 놔뒀음 RecordingSampler 함수에서 뭐 getAudioSource이런거 만들어서 넘기면 될듯


                        mFloatingActionButton.setImageResource(R.drawable.ic_mic);
                        mRecordingSampler.stopRecording();

                        //tasroDSP
                        stopRecording();

                        //메트로놈
                        if (metronomeThread.isPlaying()) {
                            metronomeThread.setPlaying(false);
                            metronomeThread.interrupt();
                            metronomeThread = null;
                        }

                        imageview.setImageResource(R.drawable.a1);



                    } else { // 녹음 시작

                        metronomeThread = new MetronomeThread();
                        metronomeThread.setBpm(spinnerBPM);
                        metronomeThread.setImageView(imageview);
                        mFloatingActionButton.setImageResource(R.drawable.ic_mic_off);


                        new CountDownTimer(3000, 1000) {

                            public void onTick(long millisUntilFinished) {

                              //  mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);

                                countview.setVisibility(View.VISIBLE);
                                countview.setImageResource(countArray[count]);
                                soundPool.play(clap,1f,1f,0,0,1f);
                                count++;
                                Log.d("TAG", "Count "+ (count+1) );


                            }

                            public void onFinish() {
                                //mTextField.setText("done!");
                               // mTextField.setVisibility(View.GONE);

                                countview.setVisibility(View.GONE);

                                mRecordingSampler.startRecording();
                                //tarsoDSP
                                recordAudio();


                                //메트로놈
                                metronomeThread.start();

                            }
                        }.start();



                    }


                } else {

                    Log.d("TAG", "No");
                    requestPermission();
                }





            }
        });


        //Metronome

        imageview = findViewById(R.id.imageView);


        //bpm 설정
        spinner = findViewById(R.id.bpmSpinner);
        arr.add(60);
        arr.add(80);
        arr.add(100);
        arr.add(110);
        arr.add(120);
        arr.add(130);
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, arr);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spinnerBPM = ((Integer) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



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

        mRecordingSampler.release();

     /*   //메트로놈 정지
        metronomeThread.setPlaying(false);
        metronomeThread.interrupt();
        metronomeThread = null;
        playNstop.setChecked(false);*/
        super.onDestroy();
    }

    @Override
    public void onCalculateVolume(int volume) {
        // for custom implement
        Log.d(TAG, String.valueOf(volume));
    }

    public void recordAudio(){
        releaseDispatcher();
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        humming.clear();
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            AudioProcessor recordProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
            dispatcher.addAudioProcessor(recordProcessor);

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e){
                    final float pitchInHz = res.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pitchTextView.setText(pitchInHz + "");
                            humming.add((int) pitchInHz
                            );

                        }
                    });
                }
            };

            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

            Thread audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording()
    {
        releaseDispatcher();

        Log.d("TAG","humming\n"+ humming.toString());
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
    }



}
