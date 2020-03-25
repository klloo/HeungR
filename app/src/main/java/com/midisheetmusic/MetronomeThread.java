package com.midisheetmusic;


import android.media.Image;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


public class MetronomeThread extends Thread {

    //60bpm -> 1초에 1번
    private int bpm = 60;
    private int milliSeconds = 1000;
    public int[] imageArray = {R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4};
    private ImageView imageView;
    private ImageView countView;
    int index=0;
    Handler handler = new Handler();
    boolean playing = false;




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
        playing = true;
        while(playing){
           handler.post(new Runnable() {
               @Override
               public void run() {
                   imageView.setImageResource(imageArray[index]);
               }
           });
           try{
               sleep(60000/bpm);
           }catch (InterruptedException e){
               e.printStackTrace();
           }
           index++;
           if(index>=imageArray.length){
               index = 0;
           }
        }
        return;
    }

}
