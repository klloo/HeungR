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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DiscreteScrollView.OnItemChangedListener,
        View.OnClickListener {

    public  static Context mContext;

    private DiscreteScrollView itemPicker;
    private InfiniteScrollAdapter infiniteAdapter;
    ArrayList<Data> data = new ArrayList<>();

    Data currentData;


    public void setAlbum(){

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

        itemPicker = (DiscreteScrollView) findViewById(R.id.item_picker);
        itemPicker.setOrientation(DSVOrientation.HORIZONTAL);
        itemPicker.addOnItemChangedListener(this);
        infiniteAdapter = InfiniteScrollAdapter.wrap(new AlbumAdaptor(data));
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
        //폴더 추가 버튼
        Button addBtn = findViewById(R.id.addBtn);
        //퀵버튼
        Button quickBtn = findViewById(R.id.quickBtn);

        Button start = findViewById(R.id.gogobtn);



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
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),ChooseSongActivity.class);

                intent.putExtra("folderName",currentData.title);
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
                setAlbum();
            }
        }
    }



    @Override
    public void onClick(View v) {
/*
        switch (v.getId()) {
            case R.id.item_btn_rate:
                int realPosition = infiniteAdapter.getRealPosition(itemPicker.getCurrentItem());
                Item current = data.get(realPosition);
                shop.setRated(current.getId(), !shop.isRated(current.getId()));
                changeRateButtonState(current);
                break;
            case R.id.home:
                finish();
                break;
            case R.id.btn_transition_time:
                DiscreteScrollViewOptions.configureTransitionTime(itemPicker);
                break;
            case R.id.btn_smooth_scroll:
                DiscreteScrollViewOptions.smoothScrollToUserSelectedPosition(itemPicker, v);
                break;
            default:
                showUnsupportedSnackBar();
                break;
        }
*/
    }
    private void onItemChanged(Data item) {

        currentData = item;

    }
    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int position) {
        int positionInDataSet = infiniteAdapter.getRealPosition(position);
        onItemChanged(data.get(positionInDataSet));
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


