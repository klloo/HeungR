package com.midisheetmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.File;


public class RenameFilePopup extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String folder = getIntent().getStringExtra("album");
        String songname = getIntent().getStringExtra("song");


        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_rename_file_popup);

        TextView inputtitle = findViewById(R.id.fileNameEditText2);

        Button okbtn = findViewById(R.id.okBtn22);
        Button cancleBtn = findViewById(R.id.backBtn22);


        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.putExtra();\

                File old = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+folder+"/"+songname);
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+folder+"/"+inputtitle.getText().toString()+".mid");

                Log.d("YUJIN", old.toString());
                Log.d("YUJIN", file.toString());


                if(!file.exists()) { //존재하지않으면
                    setResult(RESULT_OK, intent);
                    old.renameTo(file);

                }
                else{
                    setResult(RESULT_CANCELED,intent);
                }



                finish();
            }
        });

        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.putExtra();
                setResult(RESULT_CANCELED, intent);

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
