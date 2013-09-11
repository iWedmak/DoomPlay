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
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.example.DoomPlay.R;


public class ListTrackToListActivity extends AbstractReceiver
{
    String[] tracks ;
    ListView listView;
    PlaylistDB playlistDB ;
    public final static String keyAllTracks = "keytracksall";
    ListTracksAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_albumart_add);
        prepareActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tracks = getIntent().getStringArrayExtra(keyAllTracks);
        if(tracks == null)
            tracks = TracksHolder.songAllPath;

        listView = (ListView)findViewById(R.id.listAlbumArtist);
        adapter = new ListTracksAdapter(tracks,this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickHandler);
        playlistDB = PlaylistDB.getInstance(this);;
        LinearLayout linearAddToList = (LinearLayout)findViewById(R.id.linearToPlaylist);
        linearAddToList.setOnClickListener(onClickAddToList);



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
    AdapterView.OnItemClickListener onItemClickHandler = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

            playlistDB.addTracks(new String[]{tracks[position]}, PlaylistActivity.selectedPlaylist);

            Toast.makeText(getBaseContext(),ListTracksActivity.getReadableName(tracks[position]) +
                    " was added",Toast.LENGTH_SHORT).show();
        }
    };
}
