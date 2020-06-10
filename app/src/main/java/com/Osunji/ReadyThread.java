package com.Osunji;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ReadyThread extends Thread {


    public int[] imageArray = {R.drawable.count1, R.drawable.count2, R.drawable.count3};
    private ImageView countView;
    Handler handler = new Handler();
    int i;




    public void setImageView(ImageView countView){
        this.countView = countView;
    }



    @Override
    public void run(){
            for( i = 0 ; i < 3 ; i++ ){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        countView.setImageResource(imageArray[i]);

                        Log.d("TAG", "Count "+ (i+1) );
                    }
                });
                try{
                    sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        return;
    }

}
