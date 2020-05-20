package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;


public class AddFolderPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_addfolder);

        EditText inputTitle = (EditText)findViewById(R.id.inputTitle);

        Button cancelBtn = (Button)findViewById(R.id.cancelBtn);
        Button saveBtn = (Button)findViewById(R.id.saveBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditText와 사진을 받아 넘김

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+inputTitle.getText().toString());
                if(!dir.exists()){
                    dir.mkdirs();
                }


                //데이터 전달
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
            return false;
        }
        return true;
    }

}
