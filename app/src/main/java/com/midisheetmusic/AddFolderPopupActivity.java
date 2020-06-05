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

    boolean check;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        check = getIntent().getBooleanExtra("change",false);
        title = getIntent().getStringExtra("name");
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_addfolder);

        EditText inputTitle = (EditText)findViewById(R.id.inputTitle);
        if(check)
            inputTitle.setText(title);
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

                if(check){ //파일 변경할것
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+inputTitle.getText().toString());
                    if(!dir.exists()){ //존재하지않으면

                        File temp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+title);
                        if(temp.exists()){
                            temp.renameTo(dir);

                            //데이터 전달
                            Intent intent = new Intent();
                            //intent.putExtra();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        else{

                            //데이터 전달
                            Intent intent = new Intent();
                            //intent.putExtra();
                            setResult(RESULT_FIRST_USER, intent);
                            finish();
                        }

                    }
                    else{ //존재하면
                        //데이터 전달
                        Intent intent = new Intent();
                        //intent.putExtra();
                        setResult(RESULT_FIRST_USER, intent);
                        finish();
                    }
                }
                else{ //새로운 폴더 생성
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+inputTitle.getText().toString());
                    if(!dir.exists()){ //존재하지않으면
                        dir.mkdirs();  //만들고

                        //데이터 전달
                        Intent intent = new Intent();
                        //intent.putExtra();
                        setResult(RESULT_OK, intent);
                        finish();

                    }
                    else{ //존재하면
                        //데이터 전달
                        Intent intent = new Intent();
                        //intent.putExtra();
                        setResult(RESULT_FIRST_USER, intent);
                        finish();
                    }
                }




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
