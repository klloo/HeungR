package com.Osunji;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import es.dmoral.toasty.Toasty;
import io.realm.Realm;
import io.realm.RealmResults;

//import com.developer.kalert.KAlertDialog;


public class MainActivity extends BaseActivity implements DiscreteScrollView.OnItemChangedListener{

    public  static Context mContext;

    private DiscreteScrollView itemPicker;
    private InfiniteScrollAdapter infiniteAdapter;

    AlbumDB currentData = null;
    TextView albumname;
    TextView numberofSong;

    Realm realm = Realm.getDefaultInstance();
    RealmResults<AlbumDB> realmResults;

    public void setAlbum(){


        realmResults = realm.where(AlbumDB.class).findAll();


        itemPicker = (DiscreteScrollView) findViewById(R.id.item_picker);
        itemPicker.setOrientation(DSVOrientation.HORIZONTAL);
        itemPicker.addOnItemChangedListener(this);
        infiniteAdapter = InfiniteScrollAdapter.wrap(new AlbumAdaptor(realmResults));
        itemPicker.setAdapter(infiniteAdapter);
        itemPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContext = this;
        setAlbum();

        RealmResults<AlbumDB> realmResults = realm.where(AlbumDB.class).findAll();
        if(realmResults.isEmpty()){
            realm.beginTransaction();
            AlbumDB newAlbum = realm.createObject(AlbumDB.class, -2);
            newAlbum.setAlbumInfo("New Album",null);
            AlbumDB quickAlbum = realm.createObject(AlbumDB.class, -1);
            quickAlbum.setAlbumInfo("Quick",null);
            realm.commitTransaction();
        }


        ImageButton editBtn = findViewById(R.id.renameButton);
        ImageButton quickBtn = findViewById(R.id.quickBtn);
        ImageButton setBtn = findViewById(R.id.settingBtn);


        albumname = findViewById(R.id.albumname);
        numberofSong = findViewById(R.id.numofsong);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //폴더 생성하는 팝업d인데 수정도같이함
                Intent intent = new Intent(getApplicationContext(), AddFolderPopupActivity.class);
                intent.putExtra("change",true);
                intent.putExtra("name", currentData.getAlbumTitle());
                startActivityForResult(intent, 1);
            }
        });
        quickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SetFileNameActivity.class);
                intent.putExtra("folderName","Quick");
                intent.putExtra("AlbumID",-1);
                startActivity(intent);
            }
        });
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getApplicationContext(), Girls.class);
                startActivity(intent);

            }
        });

    }

    public void go(String albumname){
        if(albumname.equals("New Album")){

            //폴더 생성하는 팝업
            Intent intent = new Intent(getApplicationContext(), AddFolderPopupActivity.class);
            intent.putExtra("change",false);
            intent.putExtra("name", "");

            startActivityForResult(intent, 1);
        }
        else{

            Intent intent = new Intent(getApplicationContext(),ChooseSongActivity.class);

            int id = currentData.getId();
            if(currentData.getAlbumTitle()=="Quick")
                id = -1;
            intent.putExtra("folderName",currentData.getAlbumTitle());
            intent.putExtra("AlbumID",id);
            startActivity(intent);
        }

    }

    public  void deleteLong(String albumname){

        Intent intent = new Intent(getApplicationContext(), deletePopup.class);
        intent.putExtra("albumname",albumname);
        if(albumname.equals("Quick")||albumname.equals("New Album"))
            intent.putExtra("isQuick", true);
        else
            intent.putExtra("isQuick", false);

        startActivityForResult(intent, 2);
    }

    //새로 추가한 폴더명과 사진을 리스트에 추가하기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                setAlbum();
                Toasty.custom(this, "앨범을 생성했습니다", R.drawable.success, R.color.Greenery,  Toast.LENGTH_SHORT, true, true).show();


            }
            else if( resultCode == RESULT_FIRST_USER ){
                //존재합니다 알림
                Toasty.custom(this, "이미 존재하는 앨범입니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
            }
        }

        else if( requestCode ==2){
            if( resultCode == RESULT_OK){
                String albumname = data.getStringExtra("albumname");
                System.out.println(albumname);
                //폴더 삭제
                realm.beginTransaction();
                AlbumDB deleteItem = realm.where(AlbumDB.class).equalTo("albumTitle",albumname).findFirst();
                int delAlbumId = deleteItem.getId();
                deleteItem.deleteFromRealm();
                // 폴더안에 파일 삭제
                RealmResults<MusicDB> deleteItemInner = realm.where(MusicDB.class).equalTo("id", delAlbumId).findAll();
                deleteItemInner.deleteAllFromRealm();
                realm.commitTransaction();
                setAlbum();
                Toasty.custom(this, "앨범을 삭제했습니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
            }
            else if( resultCode == RESULT_FIRST_USER ){
                Toasty.custom(this, "삭제할수 없는 앨범입니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
            }
        }
    }


    private void onItemChanged(AlbumDB item) {
        currentData = item;
    }
    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int position) {
        int positionInDataSet = infiniteAdapter.getRealPosition(position);
        if(realmResults.size()!=0)
            onItemChanged(realmResults.get(positionInDataSet));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        actList.remove(this);
        realm.close();
    }

}


