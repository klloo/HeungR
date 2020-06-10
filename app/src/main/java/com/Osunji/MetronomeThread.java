package com.Osunji;


import android.os.Handler;
import android.widget.ImageView;

import static com.Osunji.RecordingActivity.vibrator;


public class MetronomeThread extends Thread {

    //60bpm -> 1초에 1번
    private int bpm = 60;
    private int measure = 4;
    private int milliSeconds = 1000;
    public int[] imageArray1 = {R.drawable.fourth_1, R.drawable.fourth_2, R.drawable.fourth_3, R.drawable.fourth_4};
    public int[] imageArray2 = {R.drawable.third_1, R.drawable.third_2, R.drawable.third_3};

    private ImageView imageView;
    private ImageView countView;
    int index=0;
    Handler handler = new Handler();
    boolean playing = false;


    public void setMeasure(int measure){
        this.measure = measure;
    }

    public void setBpm(int bpm){
        this.bpm = bpm;
        milliSeconds = milliSeconds * (bpm/60);
    }

    public int getBpm(){
        return this.bpm;
    }

    public void setImageView(ImageView imageView){
        this.imageView = imageView;
    }

    public void setPlaying(boolean playing){
        this.playing = playing;
    }
    public boolean isPlaying(){
        return playing;
    }


    @Override
    public void run(){

        while(playing){
           handler.post(new Runnable() {
               @Override
               public void run() {
                   if(measure == 4){
                       imageView.setImageResource(imageArray1[index]);
                       vibrator.vibrate(5);
                   }
                   else{
                       imageView.setImageResource(imageArray2[index]);
                       vibrator.vibrate(5);
                   }
               }
           });
           try{
               sleep(60000/bpm);

           }catch (InterruptedException e){
               e.printStackTrace();
           }
           index++;
            if((measure == 4 && index>=imageArray1.length) || (measure == 3 && index>=imageArray2.length)){
                index = 0;
            }
        }
        return;
    }

}
