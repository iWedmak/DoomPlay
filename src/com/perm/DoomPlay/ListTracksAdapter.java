package com.perm.DoomPlay;


/*
 *    Copyright 2013 Vladislav Krot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    You can contact me <DoomPlaye@gmail.com>
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.DoomPlay.R;


public class ListTracksAdapter extends BaseAdapter
{
    String[] tracks ;
    LayoutInflater inflater;
    Context context;
    private int markedItem ;


    public ListTracksAdapter(String[] tracks,Context context)
    {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        markedItem = PlayingService.valueTrackNotChanged;
        this.tracks = tracks;
        this.context = context;
    }
    public void changeData(String[] tracks)
    {
        this.tracks = tracks;
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
        return tracks.length;
    }

    @Override
    public Object getItem(int position)
    {
        return tracks[position];
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
        Song song = new Song(tracks[position]);
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

        holder.textArtist.setText(song.getArtist());
        holder.textTitle.setText(song.getTitle());

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