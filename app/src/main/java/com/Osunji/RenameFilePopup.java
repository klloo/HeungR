package com.Osunji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;


public class RenameFilePopup extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String folder = getIntent().getStringExtra("album");
        String musicId = getIntent().getStringExtra("musicId");

        Realm realm = Realm.getDefaultInstance();
        RealmResults<AlbumDB> realmResults;


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

                MusicDB editItem = realm.where(MusicDB.class).equalTo("id",musicId).findFirst();
                RealmResults<MusicDB> realmResults = realm.where(MusicDB.class).equalTo("title",inputtitle.getText().toString())
                        .equalTo("albumId",editItem.getAlbumId()).findAll();
                //같은 앨범 내에는 동일한 이름 안되고 다른 앨범이면 가능
                //realmResults에 같을 앨범이 하나라도 있으면 안됨 하나 있는게 자기 자신이면 가능
                if(realmResults.size()==0 || (realmResults.size()==1 && realmResults.get(0).getId() == editItem.getId())) {
                    editItem.setTitle(inputtitle.getText().toString());
                    setResult(RESULT_OK, intent);
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
