package com.midisheetmusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

//import com.developer.kalert.KAlertDialog;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity implements DiscreteScrollView.OnItemChangedListener{

    public  static Context mContext;

    private DiscreteScrollView itemPicker;
    private InfiniteScrollAdapter infiniteAdapter;
    ArrayList<Data> data = new ArrayList<>();

    Data currentData = null;
    TextView albumname;
    TextView numberofSong;

    public void setAlbum(){

        data = new ArrayList<>();


        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Capstone";
        File directory = new File(path);

        if(directory.exists()) {
            File[] files = directory.listFiles();
            for(int i=0;i<files.length;i++) {
                File[] tmp = files[i].listFiles();
                int n = 0;
                if(tmp != null)
                    n = tmp.length;
                if(files[i].getName().equals("banju"))
                    continue;
                data.add(new Data(files[i].getName(), n));
            }

        }

        data.add(new Data("New Album" , -1));


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


        ImageButton editBtn = findViewById(R.id.renameButton);
        ImageButton quickBtn = findViewById(R.id.quickBtn);
        ImageButton setBtn = findViewById(R.id.settingBtn);


        albumname = findViewById(R.id.albumname);
        numberofSong = findViewById(R.id.numofsong);

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/Quick");
        if(!dir.exists()){
            dir.mkdirs();
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //폴더 생성하는 팝업d인데 수정도같이함
                Intent intent = new Intent(getApplicationContext(), AddFolderPopupActivity.class);
                intent.putExtra("change",true);
                intent.putExtra("name", currentData.getTitle());
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
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getApplicationContext(), Girls.class);
                startActivity(intent);

            }
        });

    }

    public void go(){
        if(albumname.getText().equals("New Album")){

            //폴더 생성하는 팝업
            Intent intent = new Intent(getApplicationContext(), AddFolderPopupActivity.class);
            intent.putExtra("change",false);
            intent.putExtra("name", "");

            startActivityForResult(intent, 1);
        }
        else{

            Intent intent = new Intent(getApplicationContext(),ChooseSongActivity.class);

            intent.putExtra("folderName",currentData.title);
            startActivity(intent);
        }

    }

    public  void deleteLong(){

        Intent intent = new Intent(getApplicationContext(), deletePopup.class);
        if(albumname.getText().equals("Quick"))
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
                Toasty.custom(this, "앨범을 생성하지 못하였습니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
            }
        }

        else if( requestCode ==2){

            if( resultCode == RESULT_OK){
                // 파일 삭제

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Capstone/"+currentData.title);
                if(dir.exists()){

                    if(dir.isDirectory()){
                        File[] files = dir.listFiles();
                        for( File file : files){ // 내부파일 하나씩 지움
                            if( !file.delete())
                                Toasty.custom(mContext, "폴더를 삭제하지못했습니다.", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
                        }
                    }

                    if(dir.delete()){ // 폴더삭제
                        Toasty.custom(mContext, "폴더를 삭제했습니다", R.drawable.success, R.color.Greenery,  Toast.LENGTH_SHORT, true, true).show();

                    }

                    else{
                        Toasty.custom(mContext, "폴더를 삭제하지못했습니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();

                    }


                    setAlbum();

                }



            }
            else if( resultCode == RESULT_FIRST_USER ){
                Toasty.custom(this, "삭제할수 없는 앨범입니다", R.drawable.warning, R.color.Faded_Denim,  Toast.LENGTH_SHORT, true, true).show();
            }
        }
    }


    private void onItemChanged(Data item) {
        currentData = item;
        albumname.setText(item.getTitle());
        numberofSong.setText(item.getTracknum()+"");

        if(numberofSong.getText().equals("-1"))
            numberofSong.setText("새로운 앨범을 만들어보세요");

    }
    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int position) {
        int positionInDataSet = infiniteAdapter.getRealPosition(position);
        onItemChanged(data.get(positionInDataSet));
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


