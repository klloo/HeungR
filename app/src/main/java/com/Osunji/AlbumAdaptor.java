package com.Osunji;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;

import io.realm.Realm;
import io.realm.RealmResults;



public class AlbumAdaptor  extends RecyclerView.Adapter<AlbumAdaptor.ViewHolder> {

    Realm realm = Realm.getDefaultInstance();
    RealmResults<AlbumDB> data;
    int color[] = { ContextCompat.getColor(MainActivity.mContext,R.color.Faded_Denim ) ,
            ContextCompat.getColor(MainActivity.mContext, R.color.Ether) ,
            ContextCompat.getColor(MainActivity.mContext, R.color.Tourmaline) };

    public AlbumAdaptor(RealmResults<AlbumDB> data)  {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_album_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(data!=null) {
            AlbumDB item = data.get(position);
            byte[] arr = item.getCoverImage();
            holder.albumname.setText(item.getAlbumTitle());
            holder.image.setBackgroundColor(color[position%3]);

            //new album이면
            if(item.getId()==-2) {
                holder.image.setBackgroundColor(ContextCompat.getColor(MainActivity.mContext, R.color.Greenery));
                holder.image.setImageDrawable(ContextCompat.getDrawable(MainActivity.mContext, R.drawable.folder));
                holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.songNumber.setText("새로운 앨범을 만들어보세요");
            }
            //다른앨범들이면
            else{
                String n = String.valueOf(realm.where(MusicDB.class).equalTo("albumId",item.getId()).findAll().size());
                holder.songNumber.setText(n);
                //이미지가 있으면
                if(arr!=null) {
                    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(arr);
                    Bitmap coverImage = BitmapFactory.decodeStream(arrayInputStream);
                    holder.image.setImageBitmap(coverImage);
                }
                else //이미지가 없으면
                    holder.image.setImageDrawable(ContextCompat.getDrawable(MainActivity.mContext, R.drawable.album));

                holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }

    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder  {

        private ImageView image;
        private TextView albumname;
        private TextView songNumber;


        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView)itemView.findViewById(R.id.coverImageView);
            albumname = itemView.findViewById(R.id.albumname);
            songNumber = itemView.findViewById(R.id.numofsong);

            itemView.findViewById(R.id.coverImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((MainActivity)MainActivity.mContext).go(albumname.getText().toString());

                }
            });


            itemView.findViewById(R.id.coverImageView).setOnLongClickListener( new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {

                    ((MainActivity)MainActivity.mContext).deleteLong(albumname.getText().toString());
                    return false;
                }
            });
        }

    }

}