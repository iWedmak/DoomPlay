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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.perm.vkontakte.api.Audio;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;


public class FullPlaybackActivity  extends AbstractControls
{
    static String[] tracks;
    static ArrayList<Audio> audios;
    LinearLayout linearBackground;
    CustomViewPager viewPager;
    public final static String keyService = "zajiy";
    public final static String keyIndex = "indKey";
    public final static String tagSleepDialog = "diasTa";
    public final static String actionReturnFull = "android.action.return";
    public final static String actionPlayFull = "android.action.play";
    public final static String keyReturn = "keyForsaveService";
    public final static String actionDataChanged = "notifyDataChanged";
    PagePlaybackAdapter adapterPager;
    Intent intentWas;

    @Override
    protected void trackChanged()
    {
        viewPager.setCurrentItem(PlayingService.indexCurrentTrack,false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isShown = true;
        viewPager.setCurrentItem(PlayingService.indexCurrentTrack,false);

    }

    @Override
    protected void createBroadcastRec()
    {
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(action.equals(PlayingService.actionTrackChanged))
                {
                    trackChanged();
                    updateActionBar();
                }
                else if(action.equals(PlayingService.actionIconPlay))
                {
                    imgPlay.setImageResource(R.drawable.play);
                }

                else if(action.equals(PlayingService.actionIconPause))
                {
                    imgPlay.setImageResource(R.drawable.pause);
                }
                else if(action.equals(actionDataChanged))
                {
                    adapterPager.notifyDataSetChanged();
                }
            }
        };
        intentFilter = new IntentFilter(PlayingService.actionTrackChanged);
        intentFilter.addAction(PlayingService.actionIconPause);
        intentFilter.addAction(PlayingService.actionIconPlay);
        intentFilter.addAction(actionDataChanged);
        registerReceiver(broadcastReceiver, intentFilter);
        isRegister = true;
    }

    void startScan()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                TracksHolder.scanCard(getBaseContext());
            }
        });
        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playing);


        intentWas = getIntent();
        intentService = new Intent(this,PlayingService.class);

        getTracks(intentWas);
        initialize();
        initializeAbstract();

        if(intentWas.getAction().equals(actionReturnFull) || savedInstanceState != null)
        {
            intentService.putExtra(keyIndex, PlayingService.valueIncredible);
        }
        else if(PlayingService.serviceAlive)
        {
            stopService(intentService);
            initializeService();
        }
        else if(!PlayingService.serviceAlive)
            initializeService();




    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        intentWas = intent;
        getTracks(intent);
        adapterPager.notifyDataSetChanged();
    }

    void getTracks(Intent intent)
    {
        if(intent.getAction().equals(Intent.ACTION_VIEW))
        {
            if(!TracksHolder.isScanned)
                startScan();
            tracks = new String[]{getRealPathFromIntent(intent)};
        }
        else if(PlayingService.isOnline)
        {
           audios = PlayingService.audios;
        }
        else
        {
            if(intent.getAction().equals(actionPlayFull))
            {
                tracks =  intent.getStringArrayExtra((FileSystemActivity.keyMusic));
            }
            else if(intent.getAction().equals(actionReturnFull))
            {
                tracks = PlayingService.tracks;
            }

        }

        if(intent.getIntExtra(keyIndex,11116) != 11116)
            intentService.putExtra(keyIndex,intent.getIntExtra(keyIndex,0));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(keyReturn,true);
    }

    public String getRealPathFromIntent(Intent intent)
    {
        if(intent.getScheme().equals("file"))
            return new File(URI.create(intent.getData().toString())).getAbsolutePath();
        else
        {
            Cursor cursor = getContentResolver().query(intent.getData(),
                    new String[]{MediaStore.Audio.Media.DATA} , null, null, null);

            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        if(!PlayingService.isOnline)
            getMenuInflater().inflate(R.menu.bar_playing,menu);
        else
            getMenuInflater().inflate(R.menu.bar_online_playing,menu);


        return true;
    }
    public static Intent returnSmall(Context context)
    {
        if(PlayingService.isOnline)
        {
            Intent intent = new Intent(context,ListVkActivity.class);
            intent.setAction(ListVkActivity.currentAction);
            intent.putExtra(MainScreenActivity.keyOpenInListTrack,PlayingService.audios);

            return intent;
        }
        else
        {
            Intent intent = new Intent(context,ListTracksActivity.class);

            if(ListTracksActivity.currentAction != null )
                intent.setAction(ListTracksActivity.currentAction);
            else
                intent.setAction(ListTracksActivity.actionJust);

            intent.putExtra(MainScreenActivity.keyOpenInListTrack,PlayingService.tracks);
            return intent;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemAddtoPlaylist:
                FileSystemActivity.showPlaybackDialog(new String[]{tracks[PlayingService.indexCurrentTrack]},getSupportFragmentManager());
                return true;
            case R.id.itemEqualizer:
                startEqualizer();
                return true;
            case R.id.itemSleep:
                SleepDialog sleepDialog = new SleepDialog();
                sleepDialog.show(getSupportFragmentManager(),tagSleepDialog);
                return true;
            case R.id.itemReturnSmall:
                startActivity(returnSmall(this));
                finish();
                return true;
            case R.id.itemSetAsRingtone:
                Utils.setRingtone(getBaseContext(), tracks[PlayingService.indexCurrentTrack]);
                return true;
            case R.id.itemExit:
                sendBroadcast(new Intent(actionKill));
                return true;
            case R.id.itemSettings:
                startActivity(new Intent(this,SettingActivity.class));
                return true;
            case R.id.itemGetLiricks:
                if(PlayingService.isOnline)
                   AbstractListVk.startLyricsDialog(getSupportFragmentManager(),audios.get(PlayingService.indexCurrentTrack).lyrics_id);
                else
                {
                    Song song = new Song(tracks[PlayingService.indexCurrentTrack]);
                    ListTracksActivity.startLiryctDialog(getSupportFragmentManager(),song.getArtist(),song.getTitle());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeService()
    {

        if(intentWas.getAction().equals(actionReturnFull) && PlayingService.isOnline)
        {
            intentService.setAction(PlayingService.actionOnline);
            intentService.putExtra(keyService, audios);
        }
        else
        {
            intentService.setAction(PlayingService.actionOffline);
            intentService.putExtra(keyService, tracks);
        }

        bindService(intentService,serviceConnection,BIND_IMPORTANT);
        startService(intentService);
        PlayingService.serviceAlive = true;
    }

    @Override
    protected void clickWithoutAction()
    {
        initializeService();
    }

    private void initialize()
    {
        imgShuffle = (ImageView)findViewById(R.id.imageShuffle);
        imgRepeat = (ImageView)findViewById(R.id.imageRepeat);
        imgNext = (ImageView)findViewById(R.id.imageNext);
        imgPrevious = (ImageView)findViewById(R.id.imagePrevious);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        textCurrentTime = (TextView)findViewById(R.id.textElapsed);
        textTotalTime = (TextView)findViewById(R.id.textDuration);
        linearBackground = (LinearLayout)findViewById(R.id.linearPlaying);
        imgPlay = (ImageView)findViewById(R.id.imagePlay);
        viewPager = (CustomViewPager)findViewById(R.id.viewPager);
        viewPager.setOnPageChangeListener(pageChangeHandler);
        adapterPager = new PagePlaybackAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterPager);
    }
    ViewPager.OnPageChangeListener pageChangeHandler = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            if(playingService != null)
            {
                if(position > PlayingService.indexCurrentTrack)
                    playingService.playTrackFromList(PlayingService.indexCurrentTrack+1);

                else if(position < PlayingService.indexCurrentTrack)
                    playingService.playTrackFromList(PlayingService.indexCurrentTrack-1);
            }
            else
            {
                if(position > PlayingService.indexCurrentTrack)
                    PlayingService.changeTrack(PlayingService.nextTrack);
                else if(position < PlayingService.indexCurrentTrack)
                    PlayingService.changeTrack(PlayingService.previousTrack);
            }

        }
        @Override
        public void onPageSelected(int position) {}
        @Override
        public void onPageScrollStateChanged(int state){}
        };

    class PagePlaybackAdapter extends FragmentStatePagerAdapter
    {
        public PagePlaybackAdapter(FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public Fragment getItem(int position)
        {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount()
        {

            if(PlayingService.isOnline)
                return audios.size();
            else
                return tracks.length;
        }
    }

}
