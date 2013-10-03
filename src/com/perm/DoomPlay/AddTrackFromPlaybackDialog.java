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


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import java.util.ArrayList;

public class AddTrackFromPlaybackDialog extends DialogFragment
{

    private ListView listView;
    private PlaylistDB playlistDB;
    private String[] listPlaylist;
    private ArrayList<Audio> audios ;
    public final static String keyBundleDialog = "keybndleed";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        playlistDB = PlaylistDB.getInstance(getActivity());
        listPlaylist =  playlistDB.getListPlaylist();
        View view = inflater.inflate(R.layout.dialog_from_playback,container,false);
        listView = (ListView)view.findViewById(R.id.listDialogFromPlay);

        DialogAddAdapter adapter = new DialogAddAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickHandler);
        if(getArguments() != null)
            audios = getArguments().getParcelableArrayList(keyBundleDialog);
        else
            throw new IllegalArgumentException("tracks is null in AddTrackFromPlaybackDiaglog");

        return view;

    }
    private final AdapterView.OnItemClickListener onItemClickHandler = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

            if(!PlaylistDB.isLoading)
            {
                new AsyncTask<String, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(String... params)
                    {
                        playlistDB.addTracks(audios, params[0]);
                        return null;
                    }
                }.execute(listPlaylist[position]);
                dismiss();
                Toast.makeText(getActivity(),"tracks added",Toast.LENGTH_SHORT).show();
            }
            else
               AbstractList.waitMessage(getActivity());
        }
    };

    class DialogAddAdapter extends BaseAdapter
    {
        final LayoutInflater inflater;
        public DialogAddAdapter()
        {
            inflater = getActivity().getLayoutInflater();
        }
        @Override
        public int getCount()
        {
            return listPlaylist.length;
        }

        @Override
        public Object getItem(int position)
        {
            return listPlaylist[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = inflater.inflate(R.layout.item_playlist,parent,false);
            TextView textView = (TextView)view.findViewById(R.id.textItemPlaylist);
            textView.setText(listPlaylist[position]);
            return view;
        }
    }
}

