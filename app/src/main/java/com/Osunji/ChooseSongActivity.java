package com.Osunji;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import io.realm.Realm;
import io.realm.RealmResults;

public class ChooseSongActivity extends BaseActivity {

    String folderName;
    MusicDB deleteItem;
    ListView listview;
    ArrayList<MidiSong> midiSongs = new ArrayList<>();


    Realm realm = Realm.getDefaultInstance();
    RealmResults<AlbumDB> realmResults;

    int currentid;

    public static Context cContext;

    boolean check = true;

    int albumId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_song);
        cContext = this;
        folderName = getIntent().getStringExtra("folderName");

        TextView title = findViewById(R.id.chooseSongAlbum);
        title.setText(folderName);


        listview = (ListView)findViewById(R.id.list);
        albumId = getIntent().getIntExtra("AlbumID", 1);

        loadFile();

        ImageButton AddBtn = findViewById(R.id.addBtn);
        AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseSongActivity.this, SetFileNameActivity.class);
                intent.putExtra("folderName",folderName);
                intent.putExtra("AlbumID", albumId);
                startActivity(intent);
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //현재 선택된 midi의 id 저장
                currentid = midiSongs.get(position).getId();
                go();
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteItem = realm.where(MusicDB.class).equalTo("id",midiSongs.get(position).getId()).findFirst();
                delete();
                return true;
            }
        });

    }


    public void go(){

        Intent intent = new Intent(getApplicationContext(), SheetMusicActivity.class);

        //id넘겨줌
        intent.putExtra("MusicID", currentid);
        startActivity(intent);

    }

    public void delete(){
        check = false;
        Intent intent = new Intent(getApplicationContext(), deletePopup.class);
        startActivityForResult(intent, 2);
        check = true;

    }

    public void rename(){
//폴더 생성하는 팝업

        Intent intent = new Intent(getApplicationContext(), RenameFilePopup.class);

        intent.putExtra("album", folderName);

        intent.putExtra("musicId", currentid);

        startActivityForResult(intent, 3);


    }

    public void loadFile()
    {
        midiSongs.clear();

        Realm realm = Realm.getDefaultInstance();
        try {
            final RealmResults<MusicDB> musicList = realm.where(MusicDB.class).equalTo("albumId", albumId).findAll();

            System.out.println("ChooseSongActivity Music DB: " + musicList + "albumId is " + albumId);

            for(int i=0;i<musicList.size();i++){
                midiSongs.add(new MidiSong(musicList.get(i).getTitle(), musicList.get(i).getId()));
            }
        }
        catch (MidiFileException e) {
            this.finish();
            return;
        }

        FileAdapter adpater = new FileAdapter(getApplicationContext(), midiSongs);
        listview.setAdapter(adpater);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == 1){
            if( resultCode == RESULT_OK) { //파일삭제 지시
                delete();
            }
            else if( resultCode == RESULT_FIRST_USER){ //파일명 수정
                rename();
            }

        }
        else if( requestCode == 2){

            if( resultCode == RESULT_OK){
                // 파일 삭제
                realm.beginTransaction();
                deleteItem.deleteFromRealm();
                realm.commitTransaction();
                Toasty.custom(this, "파일을 삭제했습니다", R.drawable.success, R.color.Greenery,  Toast.LENGTH_SHORT, true, true).show();


            }
            else if( resultCode == RESULT_FIRST_USER ){
                Toasty.custom(this, "삭제할 수 없습니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
            }


            loadFile();

            ((MainActivity)MainActivity.mContext).setAlbum();
        }
        else if( requestCode == 3 ){

            if(resultCode == RESULT_OK) {
                loadFile();

                Toasty.custom(this, "이름을 변경했습니다", R.drawable.success, R.color.Greenery,  Toast.LENGTH_SHORT, true, true).show();
            }
            else{
                Toasty.custom(this, "변경할 수 없습니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();

            }

        }
    }

    @Override
    public void onBackPressed() {
        actFinish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}

class FileAdapter extends ArrayAdapter<Object> {
    private ArrayList<MidiSong> items;
    public FileAdapter(Context ctx, ArrayList items){
        super(ctx,0,items);
        this.items = items;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.choose_song_item,parent,false);
        }
        MidiSong midiSong = (MidiSong) getItem(position);
        TextView title = (TextView)convertView.findViewById(R.id.choose_song_name);
        ImageView image = (ImageView) convertView.findViewById(R.id.choose_song_icon);
        image.setImageDrawable(getContext().getDrawable(R.drawable.musiccc));
        title.setText(midiSong.getTitle());

        return convertView;
    }


}

class MidiSong{
    String title;
    private int id;

    MidiSong(String title, int id){
        this.title = title;
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public int getId(){ return id;}
}
