package com.midisheetmusic;
import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;
public class AlbumAdaptor  extends RecyclerView.Adapter<AlbumAdaptor.ViewHolder> {

    private ArrayList<Data> data;

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

        holder.albumname.setText(data.get(position).title);
        holder.songNumber.setText(data.get(position).getTracknum()+"");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView albumname;
        private TextView songNumber;
        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            albumname = itemView.findViewById(R.id.album_name);
            songNumber = itemView.findViewById(R.id.numOfSongs);
        }
    }

}