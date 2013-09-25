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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivity extends AbstractReceiver
{
    ListView listPlaylist;
    PlaylistAdapter adapter;
    PlaylistDB playlistDB;
    public static String[] playlists;
    public static String selectedPlaylist = null;
    public final static String tagNewDialog = "tagNewDialog";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);
        listPlaylist = (ListView) findViewById(R.id.listPlaylist);
        playlistDB = PlaylistDB.getInstance(this);
        playlists = playlistDB.getListPlaylist();

        if(playlists == null)
        {
            playlists = new String[]{};
        }
        adapter = new PlaylistAdapter();
        listPlaylist.setAdapter(adapter);
        listPlaylist.setOnItemClickListener(onItemTrackClick);
        listPlaylist.setOnItemLongClickListener(onItemLongTrackClick);

        updateList();
    }
    AdapterView.OnItemClickListener onItemTrackClick = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectedPlaylist = playlists[position];

            Intent intent = new Intent(getBaseContext(),ListTracksActivity.class);
            intent.setAction(ListTracksActivity.actionPlaylist);
            intent.putExtra(MainScreenActivity.keyOpenInListTrack,playlistDB.getTracks(selectedPlaylist));
            startActivity(intent);
        }
    };
    AdapterView.OnItemLongClickListener onItemLongTrackClick = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectedPlaylist = playlists[position];
            startSupportActionMode(callback);
            return true;
        }
    };

    ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getMenuInflater().inflate(R.menu.action_option_playlist, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false;}

        @Override
        public boolean onActionItemClicked(ActionMode mode,MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.itemDeleteList:
                    playlistDB.deletePlaylist(selectedPlaylist);
                    updateList();
                    mode.finish();
                    return true;
                case R.id.itemPlayAll:
                    Intent intent = new Intent(getBaseContext(),FullPlaybackActivity.class);
                    intent.putExtra(FileSystemActivity.keyMusic,playlistDB.getTracks(selectedPlaylist));
                    intent.setAction(FullPlaybackActivity.actionPlayFull);
                    startActivity(intent);
                    mode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_playlist,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemNewPlaylist:
                AddListDialog dialog = new AddListDialog()
                {
                    @Override
                    boolean isPlaylistExist(String playlist)
                    {
                        for(int i = 0 ; i < playlistDB.getListPlaylist().length ; i++)
                        {
                            if(playlistDB.getListPlaylist()[i].equals(playlist))
                                return true;
                        }
                        return false;
                    }

                    @Override
                    void createPlatlist(String playlist)
                    {
                        playlistDB.addPlaylist(playlist);
                        updateList();
                    }
                };
                dialog.show(getSupportFragmentManager(),tagNewDialog);
                return true;
            case R.id.itemSettings:
                startActivity(new Intent(this,SettingActivity.class));
                return true;
            case R.id.itemExit:
                sendBroadcast(new Intent(actionKill));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class PlaylistAdapter extends BaseAdapter
    {

        LayoutInflater inflater;

        public PlaylistAdapter()
        {
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount()
        {
            return playlists.length;
        }

        @Override
        public Object getItem(int position)
        {
            return playlists[position];
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
            TextView name = (TextView)view.findViewById(R.id.textItemPlaylist);
            name.setText(playlists[position]);
            return view;
        }
    }
    void updateList()
    {
        playlists = playlistDB.getListPlaylist();
        adapter.notifyDataSetChanged();
    }

}
