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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class ListTracksActivity extends AbstractList
{
    public final static String actionJust = "actionPlayFull";
    public final static String actionPlaylist = "actionPlaylist";
    static String currentAction;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk_with_controlls);

        getTracksFromIntent();

        initialize();
        initializeAbstract();
        checkIsShown(savedInstanceState);

    }

    void getTracksFromIntent()
    {
        audios = getIntent().getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);
        if(audios == null)
            audios = TracksHolder.allAudios;
    }

    protected final AdapterView.OnItemLongClickListener onItemLongTrackClick = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            ActionMode actionMode = startSupportActionMode(callback);
            actionMode.setTag(position);
            return true;

        }
    };

    protected final ActionMode.Callback callback = new ActionMode.Callback()
    {
        int position;
        boolean isFirstCall;
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            if(currentAction.equals(actionPlaylist))
                getMenuInflater().inflate(R.menu.action_edit,menu);

            else
                getMenuInflater().inflate(R.menu.action_option,menu);
            isFirstCall = true;
            return true;

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false;}


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            if(isFirstCall)
            {
                position = (Integer)mode.getTag();
                isFirstCall = false;
            }
            if(currentAction.equals(actionPlaylist))
            {
                switch (item.getItemId())
                {

                    case R.id.itemDeleteTrack:
                    {
                        if(!PlaylistDB.isLoading)
                        {
                            trackDelete(position);
                        }
                        else
                            AbstractList.waitMessage(getBaseContext());

                        mode.finish();
                        break;
                    }
                    case R.id.itemTrackDown:
                    {
                        if(!PlaylistDB.isLoading)
                        {
                            trackChange(false, position);

                            if(position == audios.size() - 1)
                                position = 0;
                            else
                                position++;

                            updateList();
                        }
                        else
                        {
                            mode.finish();
                            AbstractList.waitMessage(getBaseContext());
                        }
                        break;
                    }
                    case R.id.itemTrackUp:
                    {
                        if(!PlaylistDB.isLoading)
                        {
                            trackChange(true, position);
                            if(position == 0)
                                position = audios.size() - 1;
                            else
                                position--;

                            updateList();
                        }
                        else
                        {
                            mode.finish();
                            AbstractList.waitMessage(getBaseContext());
                        }

                        break;
                    }
                    case R.id.itemGetLiricks:
                        startLiryctDialog(getSupportFragmentManager(),audios.get(position).getArtist(),audios.get(position).getTitle());
                        mode.finish();
                        break;
                }
            }
            else
            {
                switch(item.getItemId())
                {
                    case R.id.itemToPlaylist:
                        ArrayList<Audio> temp = new ArrayList<Audio>();
                        temp.add(audios.get(position));
                        showPlaybackDialog(temp);
                        break;
                    case R.id.itemSetAsRingtone:
                        Utils.setRingtone(getBaseContext(), audios.get(position));
                        break;
                    case R.id.itemGetLiricks:
                        Audio audio = audios.get(PlayingService.indexCurrentTrack);
                        startLiryctDialog(getSupportFragmentManager(), audio.getArtist(), audio.getTitle());
                        break;
                }
                mode.finish();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode){}
    };

    static void startLiryctDialog(FragmentManager fragmentManager,String artist,String title)
    {
        LyricsDialog dialog = new LyricsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(LyricsDialog.keyLyricsTitle,artist+" "+title);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager,"tag");
    }

    void updateList()
    {
        boolean isEquals = AbstractList.equalsCollections(audios, PlayingService.audios);
        audios = playlistDB.getTracks(PlaylistActivity.selectedPlaylist);

        if(isEquals)
        {
            PlayingService.audios = audios;
            markItem(PlayingService.indexCurrentTrack, false);
        }

        adapter.changeData(audios);

    }

    @Override
    protected void onClickTrack(int position)
    {
        super.onClickTrack(position);
        PlayingService.isOnline = false;
    }

    private void trackDelete(int position)
    {

            playlistDB.deleteTrack(position, PlaylistActivity.selectedPlaylist);

             if(AbstractList.equalsCollections(audios, PlayingService.audios))
                 PlayingService.audios.remove(position);

            audios.remove(position);
            adapter.changeData(audios);


            AsyncTask<Integer,Void,Void> asyncAdder  = new AsyncTask<Integer, Void, Void>()
            {
                @Override
                protected Void doInBackground(Integer... params)
                {
                    playlistDB.setAcordingPositions(params[0],PlaylistActivity.selectedPlaylist);
                    return null;
                }
            };
            asyncAdder.execute(position);

            if(position == PlayingService.indexCurrentTrack && AbstractList.equalsCollections(audios, PlayingService.audios))
            {
                playingService.playTrackFromList(PlayingService.indexCurrentTrack);
            }

    }

    private void trackChange(boolean up, int position)
    {
        int to ;

        if(up)
        {
            if(position == 0)
            {
                to  = audios.size() - 1;
            }
            else
            {
                to = position - 1;
            }
        }
        else
        {
            if(position == audios.size()-1)
            {
                to = 0;
            }
            else
            {
                to = position + 1 ;
            }
        }
        if(AbstractList.equalsCollections(audios, PlayingService.audios))
        {
            if(PlayingService.indexCurrentTrack == to)
                PlayingService.indexCurrentTrack = position;

            else if(PlayingService.indexCurrentTrack == position)
                PlayingService.indexCurrentTrack = to;
        }


        playlistDB.changeColumns(PlaylistActivity.selectedPlaylist, position, to);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        ArrayList<Audio> tempTracks = intent.getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);

        if(tempTracks != null)
            audios = tempTracks;

        adapter.changeData(audios);
        currentAction  = intent.getAction();
    }

    private void initialize()
    {
        listView = (ListView)findViewById(R.id.listAllSongs);
        intentService = new Intent(this,PlayingService.class);
        intentService.setAction(PlayingService.actionOffline);
        intentService.putExtra(FullPlaybackActivity.keyService, audios);
        adapter = new ListsAdapter(audios,this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemTrackClick);
        listView.setOnItemLongClickListener(onItemLongTrackClick);


        imgPlay = (ImageView)findViewById(R.id.imagePlay);
        imgShuffle = (ImageView)findViewById(R.id.imageShuffle);
        imgRepeat = (ImageView)findViewById(R.id.imageRepeat);
        imgNext = (ImageView)findViewById(R.id.imageNext);
        imgPrevious = (ImageView)findViewById(R.id.imagePrevious);

        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        textCurrentTime = (TextView)findViewById(R.id.textElapsed);
        textTotalTime = (TextView)findViewById(R.id.textDuration);
        linearControls = (RelativeLayout)findViewById(R.id.linearControls);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        currentAction = getIntent().getAction();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.bar_list,menu);
        return true;
    }



}
