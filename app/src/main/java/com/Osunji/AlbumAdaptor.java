package com.Osunji;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;


public class AlbumAdaptor  extends RecyclerView.Adapter<AlbumAdaptor.ViewHolder> {

    private ArrayList<Data> data;
    int color[] = { ContextCompat.getColor(MainActivity.mContext,R.color.Faded_Denim ) ,
            ContextCompat.getColor(MainActivity.mContext, R.color.Ether) ,
            ContextCompat.getColor(MainActivity.mContext, R.color.Tourmaline) };

    public AlbumAdaptor(ArrayList<Data> data)  {
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

        //여기서 카드 내용 설정

       //holder.albumname.setText(data.get(position).title);
       // holder.songNumber.setText(data.get(position).getTracknum()+"");

        holder.image.setBackgroundColor(color[position%3]);

        if(position == data.size()-1) {
            holder.image.setBackgroundColor(ContextCompat.getColor(MainActivity.mContext, R.color.Greenery));
            holder.image.setImageDrawable(ContextCompat.getDrawable(MainActivity.mContext, R.drawable.folder));
            holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        else{
            holder.image.setImageDrawable(ContextCompat.getDrawable(MainActivity.mContext, R.drawable.album));
            holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);

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
            image = (ImageView) itemView.findViewById(R.id.image);
            albumname = itemView.findViewById(R.id.albumname);
            songNumber = itemView.findViewById(R.id.numofsong);

            itemView.findViewById(R.id.cards).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                   ((MainActivity)MainActivity.mContext).go();

                }
            });


            itemView.findViewById(R.id.cards).setOnLongClickListener( new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {

                    ((MainActivity)MainActivity.mContext).deleteLong();
                    return false;
                }
            });
        }

    }

}
