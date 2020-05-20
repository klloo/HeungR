package com.midisheetmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class ChooseSongActivity extends AppCompatActivity {

    String folderName;
    String fileName;
    public static Context cContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_song);
        cContext = this;
        folderName = getIntent().getStringExtra("folderName");
        loadFile();


    }



    public void loadFile()
    {
        ArrayList<MidiSong> midiSongs = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Capstone/"+folderName;
        File directory = new File(path);

        if(directory.exists()) {
            File[] files = directory.listFiles();
            for(int i=0;i<files.length;i++) {
                Uri uri = Uri.parse(files[i].getPath());
                FileUri fileUri = new FileUri(uri,files[i].getPath());
                midiSongs.add(new MidiSong(files[i].getName(),fileUri));
            }

        }


        ListView listview = (ListView)findViewById(R.id.list);
        FileAdapter adpater = new FileAdapter(getApplicationContext(), midiSongs);
        listview.setAdapter(adpater);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileUri file = midiSongs.get(position).fileUri;

                Log.d("TAG", "doOpenFile");
                byte[] data = file.getData();
                if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW,file.getUri(),ChooseSongActivity.this,SheetMusicActivity.class);
                Log.d("TAG", "make intent : " + file + " | " + SheetMusicActivity.MidiTitleID);
                Log.d("TAG",file.toString());
                intent.putExtra(SheetMusicActivity.MidiTitleID, file.toString());
                startActivity(intent);
            }
        });

        FloatingActionButton AddBtn = findViewById(R.id.addMidiFileBtn);
        AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseSongActivity.this, SetFileNameActivity.class);
                intent.putExtra("folderName",folderName);
                startActivity(intent);
            }
        });
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
