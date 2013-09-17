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
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AlbumArtistActivity extends AbstractReceiver
{

    ListView listView;
    public static String[] albumArtist;

    public final static String actionPlayArtist ="action.list.playArtist";
    public final static String actionPlayAlbum = "action.list.playAlbum";
    public final static String actionAddAlbum = "action.add.album";
    PlaylistDB playlistDB ;
    static String currentAction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        currentAction = getIntent().getAction();

        if(!currentAction.equals(actionAddAlbum))
            setContentView(R.layout.list_album_artist);
        else
        {
            setContentView(R.layout.list_albumart_add);
            LinearLayout linearAddToList = (LinearLayout)findViewById(R.id.linearToPlaylist);
            linearAddToList.setOnClickListener(onClickAddToList);
        }
        listView = (ListView)findViewById(R.id.listAlbumArtist);


        playlistDB = PlaylistDB.getInstance(this);

        if(currentAction.equals(actionPlayArtist))
            albumArtist = TracksHolder.allArtist;
        else
            albumArtist = TracksHolder.allAlbums;

        AlbumArtistAdapter adapter = new AlbumArtistAdapter(albumArtist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickAlbumArtist);
        listView.setOnItemLongClickListener(onLongClickAlbumArtist);
    }

    View.OnClickListener onClickAddToList = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            startActivity(new Intent(getBaseContext(),PlaylistActivity.class));
            finish();
        }
    };

    AdapterView.OnItemClickListener onClickAlbumArtist = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(currentAction.equals(actionPlayArtist))
            {
                onClickOpenArtist(position);
            }
            else
            {
                onClickOpenAlbum(position);
            }
        }
    };
    AdapterView.OnItemLongClickListener onLongClickAlbumArtist = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(!currentAction.equals(actionAddAlbum))
                startActionMode(callback).setTag(position);

            else
            {
                addToPlaylist(position);
                Toast.makeText(getBaseContext(),"album added",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };

    void onClickOpenAlbum(int position)
    {
        Intent intent = null;

        if(currentAction.equals(actionPlayAlbum))
        {
            intent = new Intent(getBaseContext(),ListTracksActivity.class);
            intent.setAction(ListTracksActivity.actionJust);
            intent.putExtra(MainScreenActivity.keyOpenInListTrack, getTracksFromAlbumArtist(position, true));
        }
        else if(currentAction.equals(actionAddAlbum))
        {
            intent = new Intent(getBaseContext(),ListTrackToListActivity.class);
            intent.putExtra(ListTrackToListActivity.keyAllTracks, getTracksFromAlbumArtist(position, true));
        }

        startActivity(intent);
    }
    String[] getTracksFromAlbumArtist(int position ,boolean fromAlbum)
    {

        Cursor cursor;

        if(fromAlbum)
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ALBUM + " = ?",
                new String[]{albumArtist[position]}, null);
        else
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,  new String[]{MediaStore.Audio.Media.DATA},
                    MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ARTIST+ " = ?",
                    new String[]{albumArtist[position]}, null);

        String[] result = new String[cursor.getCount()];
        final int index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA) ;

        if(cursor.moveToFirst())
        {
            do
            {
                result[cursor.getPosition()] =( cursor.getString(index));

            } while(cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    void onClickOpenArtist(int position)
    {
        Intent intent = new Intent(this,ListTracksActivity.class);
        intent.setAction(ListTracksActivity.actionJust);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack, getTracksFromAlbumArtist(position, false));
        startActivity(intent);
    }
    ActionMode.Callback  callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getSupportMenuInflater().inflate(R.menu.action_filesystem,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            int position =(Integer) mode.getTag();

            switch(item.getItemId())
            {
                case R.id.itemPlayAll:
                {
                    if(currentAction.equals(actionPlayAlbum))
                    {
                        startActivity(FileSystemActivity.getToFullIntent(getBaseContext(),getTracksFromAlbumArtist(position,true),0));
                    }
                    else
                    {
                        startActivity(FileSystemActivity.getToFullIntent(getBaseContext(),getTracksFromAlbumArtist(position,false),0));
                    }

                    break;
                }
                case R.id.itemToPlaylist:
                {

                    if(currentAction.equals(actionPlayAlbum) || currentAction.equals(actionAddAlbum))
                        FileSystemActivity.showPlaybackDialog(getTracksFromAlbumArtist(position,true),getSupportFragmentManager());
                    else
                        FileSystemActivity.showPlaybackDialog(getTracksFromAlbumArtist(position,false),getSupportFragmentManager());

                    break;
                }
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode){}
    };

    void addToPlaylist(int position)
    {
         playlistDB.addTracks(getTracksFromAlbumArtist(position,true), PlaylistActivity.selectedPlaylist);
    }

    class AlbumArtistAdapter extends BaseAdapter
    {
        String[] artistAlbums;
        LayoutInflater inflater;
        public AlbumArtistAdapter(String[] artistAlbums)
        {
            this.artistAlbums = artistAlbums;
            inflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount()
        {
            return artistAlbums.length;
        }

        @Override
        public Object getItem(int position)
        {
            return artistAlbums[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            if(!currentAction.equals(actionPlayArtist))
            {
                view = inflater.inflate(R.layout.item_album,parent,false);
                ((TextView)view.findViewById(R.id.textOnlyArtist)).setText(Song.getArtistFromAlbum(position));
            }
            else
            {
                view = inflater.inflate(R.layout.item_artist,parent,false);
            }

            TextView textAlbumArtist = (TextView)view.findViewById(R.id.textAlbumArtist);
            textAlbumArtist.setText(artistAlbums[position]);

            return view;
        }
    }
}