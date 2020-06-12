package com.Osunji;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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


import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ChooseSongActivity extends BaseActivity {

    String folderName;
    File[] files;
    File selectFile;
    ListView listview;
    ArrayList<MidiSong> midiSongs = new ArrayList<>();

    FileUri current;

    public static Context cContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_song);
        cContext = this;

        folderName = getIntent().getStringExtra("folderName");

        TextView title = findViewById(R.id.chooseSongAlbum);
        title.setText(folderName);


        listview = (ListView)findViewById(R.id.list);

        loadFile();

        ImageButton AddBtn = findViewById(R.id.addBtn);
        AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseSongActivity.this, SetFileNameActivity.class);
                intent.putExtra("folderName",folderName);
                startActivity(intent);
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                current =  midiSongs.get(position).fileUri;
                go();
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectFile = files[position];
                current =  midiSongs.get(position).fileUri;

                Intent intent = new Intent(getApplicationContext(), songEditActivity.class);

                startActivityForResult(intent, 1);

                return true;
            }
        });

    }


    public void go(){


        Log.d("TAG", "doOpenFile");
        byte[] data = current.getData();
        if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW,current.getUri(),ChooseSongActivity.this,SheetMusicActivity.class);
        Log.d("TAG", "make intent : " + current + " | " + SheetMusicActivity.MidiTitleID);
        Log.d("TAG",current.toString());
        intent.putExtra(SheetMusicActivity.MidiTitleID, current.toString());
        startActivity(intent);

    }

    public void delete(){

        Intent intent = new Intent(getApplicationContext(), deletePopup.class);
        startActivityForResult(intent, 2);


    }

    public void rename(){
//폴더 생성하는 팝업

        Intent intent = new Intent(getApplicationContext(), RenameFilePopup.class);

        intent.putExtra("album", folderName);
        Log.d("YUJIN" , current.getUri().getLastPathSegment());
        intent.putExtra("song",   current.getUri().getLastPathSegment().toString());

        startActivityForResult(intent, 3);


    }

    public void loadFile()
    {
        midiSongs.clear();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Capstone/"+folderName;
        File directory = new File(path);

        if(directory.exists()) {
            files = directory.listFiles();

            for(int i=0;i<files.length;i++) {
                Uri uri = Uri.parse(files[i].getPath());
                FileUri fileUri = new FileUri(uri,files[i].getPath());
                midiSongs.add(new MidiSong(files[i].getName(),fileUri));
            }

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

                selectFile.delete();
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
    FileUri fileUri;
    MidiSong(String title, FileUri fileUri){
        this.title = title;
        this.fileUri = fileUri;
    }
    public String getTitle() {
        return title;
    }
    public FileUri getFileUri() {
        return fileUri;
    }
}
