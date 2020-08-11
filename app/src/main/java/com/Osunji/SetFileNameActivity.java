package com.Osunji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import io.realm.Realm;
import io.realm.RealmResults;

public class SetFileNameActivity extends Activity {
    String folderName;
    int albumID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        folderName = getIntent().getStringExtra("folderName");
        albumID = getIntent().getIntExtra("AlbumID", -1);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_file_name);

        EditText fileName = (EditText)findViewById(R.id.fileNameEditText);

        Button backBtn = (Button)findViewById(R.id.backBtn);
        Button okBtn = (Button)findViewById(R.id.okBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Realm realm = Realm.getDefaultInstance();

                int albumid = realm.where(AlbumDB.class).equalTo("albumTitle",folderName).findFirst().getId();
                RealmResults<MusicDB> realmResults = realm.where(MusicDB.class).equalTo("title",fileName.getText().toString()+".mid")
                        .equalTo("albumId",albumid).findAll();
                if(realmResults.size() > 0) {
                    if(ChooseSongActivity.cContext!=null)
                        Toasty.custom(ChooseSongActivity.cContext, "이미 존재하는 파일명 입니다", R.drawable.warning, R.color.Faded_Denim, Toast.LENGTH_SHORT, true, true).show();
                    else
                        Toasty.custom(MainActivity.mContext, "이미 존재하는 파일명 입니다", R.drawable.warning, R.color.Faded_Denim, Toast.LENGTH_SHORT, true, true).show();
                }
                else{
                    Intent intent = new Intent(SetFileNameActivity.this, RecordingActivity.class);
                    intent.putExtra("fileName", fileName.getText().toString());
                    intent.putExtra("folderName", folderName);
                    intent.putExtra("AlbumID", albumID);

                    startActivity(intent);
                    finish();
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
