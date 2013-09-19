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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            String[] tracks = playlistDB.getTracks(selectedPlaylist);

            Intent intent = new Intent(getBaseContext(),ListTracksActivity.class);
            intent.setAction(ListTracksActivity.actionPlaylist);
            intent.putExtra(MainScreenActivity.keyOpenInListTrack,tracks);
            startActivity(intent);
        }
    };
    AdapterView.OnItemLongClickListener onItemLongTrackClick = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectedPlaylist = playlists[position];
            startActionMode(callback);
            return true;
        }
    };

    com.actionbarsherlock.view.ActionMode.Callback callback = new com.actionbarsherlock.view.ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(com.actionbarsherlock.view.ActionMode mode, com.actionbarsherlock.view.Menu menu)
        {
            getSupportMenuInflater().inflate(R.menu.action_option_playlist, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(com.actionbarsherlock.view.ActionMode mode, com.actionbarsherlock.view.Menu menu){return false;}

        @Override
        public boolean onActionItemClicked(com.actionbarsherlock.view.ActionMode mode, com.actionbarsherlock.view.MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.itemAddTracks:
                    Intent intentAlb = new Intent(getBaseContext(),AlbumArtistActivity.class);
                    intentAlb.setAction(AlbumArtistActivity.actionAddAlbum);
                    startActivity(intentAlb);
                    mode.finish();
                    return true;
                case R.id.itemDeleteList:
                    playlistDB.deletePlaylist(selectedPlaylist);
                    updateList();
                    mode.finish();
                    return true;
                case R.id.itemPlayAll:
                    String[] tracks = playlistDB.getTracks(selectedPlaylist);
                    Intent intent = new Intent(getBaseContext(),FullPlaybackActivity.class);
                    intent.putExtra(FileSystemActivity.keyMusic,tracks);
                    intent.setAction(FullPlaybackActivity.actionPlayFull);
                    startActivity(intent);
                    mode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(com.actionbarsherlock.view.ActionMode mode)
        {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.bar_playlist,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
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
