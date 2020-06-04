package com.midisheetmusic;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
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


    }


    @Override
    public int getItemCount() {
        return data.size();
    }

   /* class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView albumname;
        private TextView songNumber;
        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            albumname = itemView.findViewById(R.id.albumname);
            songNumber = itemView.findViewById(R.id.numofsong);
        }
    }*/
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
        }

    }

}
