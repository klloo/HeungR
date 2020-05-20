package com.midisheetmusic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public  static Context mContext;

    public void loadFolder()
    {
        ArrayList<Data> data = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Capstone";
        File directory = new File(path);

        if(directory.exists()) {
            File[] files = directory.listFiles();
            for(int i=0;i<files.length;i++) {
                File[] tmp = files[i].listFiles();
                int n = 0;
                if(tmp != null)
                    n = tmp.length;
                data.add(new Data(files[i].getName(), n));
            }

        }


        ListView listview = (ListView)findViewById(R.id.folderList);
        FolderAdapter adpater = new FolderAdapter(this, data);
        listview.setAdapter(adpater);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ChooseSongActivity.class);
                intent.putExtra("folderName",data.get(position).title);
                startActivity(intent);

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        loadFolder();

        //폴더 추가 버튼
        Button addBtn = findViewById(R.id.addBtn);
        //퀵버튼
        Button quickBtn = findViewById(R.id.quickBtn);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //폴더 생성하는 팝업
                Intent intent = new Intent(getApplicationContext(), AddFolderPopupActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        quickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SetFileNameActivity.class);
                intent.putExtra("folderName","Quick");
                startActivity(intent);
            }
        });

    }

    //새로 추가한 폴더명과 사진을 리스트에 추가하기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                loadFolder();
            }
        }
    }
}

class FolderAdapter extends ArrayAdapter<Object> {
    private ArrayList<Data> items;
    private Data temp;
    public FolderAdapter(Context ctx, ArrayList items){
        super(ctx,0,items);
        this.items = items;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.folder_item,parent,false);
        }
        Data data = (Data) getItem(position);
        TextView title = (TextView)convertView.findViewById(R.id.folderTitleTextView);
        TextView tracknum = (TextView)convertView.findViewById(R.id.tracknumTextView);
        tracknum.setText(String.valueOf(data.getTracknum()));
        title.setText(data.getTitle());
        return convertView;
    }

}
class Data{
    String title;
    int tracknum;
    Data(String title,int tracknum){
        this.title = title;
        this.tracknum = tracknum;
    }

    public String getTitle() {
        return title;
    }

    public int getTracknum() {
        return tracknum;
    }
}


