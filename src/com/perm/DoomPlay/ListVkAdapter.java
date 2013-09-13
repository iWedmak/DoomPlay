package com.perm.DoomPlay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Audio;

import java.util.ArrayList;

public class ListVkAdapter  extends BaseAdapter
{
    LayoutInflater inflater;
    Context context;
    int markedItem ;
    ArrayList<Audio> audios;


    public ListVkAdapter(ArrayList<Audio> audios,Context context)
    {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        markedItem = PlayingService.valueTrackNotChanged;
        this.audios = audios;
        this.context = context;
    }
    public void changeData(ArrayList<Audio> audios)
    {
        this.audios = audios;
        notifyDataSetChanged();
    }

    public void setMarkedItem(int position)
    {
        markedItem = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return audios.size();
    }

    @Override
    public Object getItem(int position)
    {
        return audios.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view ;
        ViewHolder holder;
        if(convertView == null)
        {
            view = inflater.inflate(R.layout.item_song,parent,false);
            holder = new ViewHolder();
            holder.textArtist =(TextView)view.findViewById(R.id.textSongArtist);
            holder.textTitle = (TextView)view.findViewById(R.id.textSongName);

            view.setTag(holder);

        }
        else
        {
            view = convertView;
            holder =(ViewHolder)view.getTag();
        }

        holder.textArtist.setText(audios.get(position).artist);
        holder.textTitle.setText(audios.get(position).title);

        if(markedItem == position)
            holder.textTitle.setTextColor(context.getResources().getColor(R.color.blue_text));
        else
            holder.textTitle.setTextColor(context.getResources().getColor(R.color.almost_white));


        return view;
    }

    static class ViewHolder
    {
        TextView textTitle;
        TextView textArtist;
    }
}
