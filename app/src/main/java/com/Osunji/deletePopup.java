package com.Osunji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class deletePopup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_delete_popup);

        ImageView warnning = (ImageView)findViewById(R.id.warnningIcon);
        Button cancelBtn = (Button) findViewById(R.id.deleteNoBtn);
        Button deleteBtn = (Button) findViewById(R.id.deleteokBtn);

        warnning.setImageDrawable(getResources().getDrawable(R.drawable.warning));
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check = getIntent().getBooleanExtra("isQuick" , false);
                String albumname = getIntent().getStringExtra("albumname");
                Intent intent = new Intent();
                intent.putExtra("albumname",albumname);
                if(check) //삭제하면안됨
                    setResult(RESULT_FIRST_USER, intent);

                else // 삭제해도 ㄱㅊ
                    setResult(RESULT_OK, intent);
                finish();


            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }
}