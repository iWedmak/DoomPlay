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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.DoomPlay.R;

import java.io.File;
import java.net.URI;


public class FullPlaybackActivity  extends AbstractControls
{
    public static String[] tracks;
    LinearLayout linearBackground;
    ViewPager viewPager;
    public final static String keyService = "zajiy";
    public final static String keyIndex = "indKey";
    public final static String tagSleepDialog = "diasTa";
    public final static String actionReturnFull = "android.action.return";
    public final static String actionPlayFull = "android.action.play";
    public final static String keyReturn = "keyForsaveService";
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

        tracks = PlayingService.tracks;
        isShown = true;
        viewPager.setCurrentItem(PlayingService.indexCurrentTrack,false);

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
        getTracks();
        initialize();


        if(intentWas.getAction().equals(actionReturnFull) || savedInstanceState != null)
        {
            intentService.putExtra(keyIndex, PlayingService.valueTrackNotChanged);
        }
        else if(PlayingService.serviceAlive)
        {
            stopService(intentService);
            PlayingService.serviceAlive = false;
        }
        if(intentWas.getIntExtra(keyIndex,11116) != 11116)
            intentService.putExtra(keyIndex,intentWas .getIntExtra(keyIndex,0));

        if(!PlayingService.serviceAlive )
        {
            initializeService();
        }

        initializeAbstract();


    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    void getTracks()
    {
        if(!intentWas.getAction().equals(Intent.ACTION_VIEW))
            tracks =  intentWas.getStringArrayExtra((FileSystemActivity.keyMusic));
        else
        {
            if(!TracksHolder.isScanned)
                startScan();
            tracks = new String[]{getRealPathFromIntent(intentWas)};
        }
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
        if(!MainScreenActivity.isOldSDK)
            getSupportMenuInflater().inflate(R.menu.bar_playing,menu);
        else
            getSupportMenuInflater().inflate(R.menu.bar_playing_old,menu);
        return true;
    }
    public static Intent returnSmall(Context context)
    {
        Intent intent = new Intent(context,ListTracksActivity.class);

        if(ListTracksActivity.currentAction != null &&
                ListTracksActivity.currentAction.equals(ListTracksActivity.actionFromPlaylist))
            intent.setAction(ListTracksActivity.actionFromPlaylist);
        else
            intent.setAction(ListTracksActivity.actionPlayList);

        intent.putExtra(MainScreenActivity.keyOpenInListTrack,PlayingService.tracks);
        return intent;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemAdd:
                AddTrackFromPlaybackDialog dialog = new AddTrackFromPlaybackDialog();
                dialog.show(getSupportFragmentManager(),"tag");
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeService()
    {
        intentService.putExtra(keyService,tracks);
        startService(intentService);
        PlayingService.serviceAlive = true;
    }

    @Override
    protected void clickWithoutAction()
    {
        if(PlayingService.isClosed)
        {
            bindService(intentService,serviceConnection,0);
            PlayingService.isClosed = false;
        }
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
        intentService = new Intent(this,PlayingService.class);
        intentService.setAction(PlayingService.actionOffline);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
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
            return tracks.length;
        }
    }

}
