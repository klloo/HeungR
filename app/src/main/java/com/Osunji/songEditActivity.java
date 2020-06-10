package com.Osunji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;


public class songEditActivity extends Activity {

    boolean check;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        check = getIntent().getBooleanExtra("change",false);
        title = getIntent().getStringExtra("name");
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_song_edit);


        View editLayout = findViewById(R.id.editLayout);
        View deleteLayout = findViewById(R.id.deleteLayout);
        editLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.putExtra();
                setResult(RESULT_FIRST_USER, intent);
                finish();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.putExtra();
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            finish();
            return false;
        }
        return true;
    }

}
