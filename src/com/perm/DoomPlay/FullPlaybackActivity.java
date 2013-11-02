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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class FullPlaybackActivity  extends AbstractControls
{
    static ArrayList<Audio> audios;
    private CustomViewPager viewPager;
    public final static String keyService = "zajiy";
    public final static String keyIndex = "indKey";
    public final static String tagSleepDialog = "diasTa";
    public final static String actionReturnFull = "android.action.return";
    public final static String actionPlayFull = "android.action.play";
    private final static String keyReturn = "keyForsaveService";
    public final static String actionDataChanged = "notifyDataChanged";
    private PagePlaybackAdapter adapterPager;
    private Intent intentWas;

    private static boolean isPaused  = false;

    @Override
    protected void trackChanged()
    {
        viewPager.setCurrentItem(PlayingService.indexCurrentTrack, false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isShown = true;
        isPaused = false;

        //TODO: without that throws  java.lang.IllegalStateException: The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: 1, found: 384
        adapterPager.notifyDataSetChanged();
        viewPager.setCurrentItem(PlayingService.indexCurrentTrack, false);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isPaused = true;
    }

    private final PlayingService.OnAlbumArtSave onAlbumArtSave = new PlayingService.OnAlbumArtSave()
    {
        // TODO: sometimes throw the exception
        //java.lang.IllegalStateException: Fragment PageFragment{4240e278} is not currently in the FragmentManager

        @Override
        public void onAlbumArtSave(long id)
        {
            if(audios != null && !isPaused)
                adapterPager.notifyDataSetChanged();

        }
    };
    @Override
    protected void onServiceAbstractConnected()
    {
        playingService.setOnAlbumArtSaveListener(onAlbumArtSave);
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
        else
        {
            PlayingService.isPlaying = true;
            initializeService();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        intentWas = intent;
        setIntent(intent);
        getTracks(intent);
        adapterPager = new PagePlaybackAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterPager);
        viewPager.setCurrentItem(PlayingService.indexCurrentTrack);
    }

    void getTracks(Intent intent)
    {
        if(intent.getAction().equals(Intent.ACTION_VIEW))
        {
            String path = intent.getData().getPath();
            File fpath = new File(path) ;

            if(PlaylistParser.isFileCue(path))
            {
                audios = PlaylistParser.displayCue(fpath,this);
            }
            else if(PlaylistParser.isFilePlaylist(path))
            {
                audios = PlaylistParser.displayPlaylist(fpath,this);
            }
            else
            {
                audios = new ArrayList<Audio>();
                audios.add(getAudioFromPath(fpath));
            }
        }
        else if(intent.getAction().equals(actionPlayFull) || intent.getAction().equals(actionReturnFull))
        {
            audios =  intent.getParcelableArrayListExtra((FileSystemActivity.keyMusic));
        }
        else
        {
            audios = PlayingService.audios;
        }

        if(intent.getIntExtra(keyIndex,11116) != 11116)
            intentService.putExtra(keyIndex,intent.getIntExtra(keyIndex,0));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_HOME))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(keyReturn,true);
    }



    /*
    Cursor cursor;


        String filePath = null;

        if(intent.getScheme().equals("file"))
        {
            File file = new File(URI.create(intent.getData().toString())) ;

            try
            {
                filePath  = file.getCanonicalPath();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                filePath = file.getAbsolutePath();
            }

            String[] selectionArgs = new String[]{filePath};
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,TracksHolder.projection,
                    MediaStore.Audio.Media.DATA + " = ? ",selectionArgs,null);
        }
        else
        {
            cursor = getContentResolver().query(intent.getData(),
                    TracksHolder.projection , null, null, null);
        }
        Audio audio;

        if(cursor.moveToFirst())
            audio = Audio.parseAudioCursor(cursor);
        else
        {
            if(intent.getScheme().equals("file"))
            {
                audio = new Audio("unknown",new File(filePath).getName(),filePath,0);
            }
            else
            {
                File file = new File(intent.getData().getPath());

                audio = new Audio("unknown",file.getName(),file.getAbsolutePath(),0);
            }
        }

        cursor.close();
        return audio;
     */

    Audio getAudioFromPath(File fpath)
    {
        String filePath = Utils.getRealPath(fpath);

        String[] selectionArgs = new String[]{filePath};

        Cursor  cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,TracksHolder.projection,
                    MediaStore.Audio.Media.DATA + " = ? ",selectionArgs,null);

        Audio audio;

        if(cursor.moveToFirst())
            audio = Audio.parseAudioCursor(cursor);
        else
        {
            audio = new Audio("unknown",fpath.getName(),filePath,0);
        }

        cursor.close();
        return audio;
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
    public static Intent getReturnSmallIntent(Context context, ArrayList<Audio> audios)
    {
        if(PlayingService.isOnline)
        {
            Intent intent = new Intent(context,ListVkActivity.class);
            intent.setAction(ListVkActivity.actionJust);
            intent.putExtra(MainScreenActivity.keyOpenInListTrack, audios);

            return intent;
        }
        else
        {
            Intent intent = new Intent(context,ListTracksActivity.class);
            intent.setAction(ListTracksActivity.actionJust);
            intent.putExtra(MainScreenActivity.keyOpenInListTrack,audios);
            return intent;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemAddtoPlaylist:
                if(audios.size() > 0)
                {
                    ArrayList<Audio> temp = new ArrayList<Audio>();
                    temp.add(audios.get(PlayingService.indexCurrentTrack));
                    showPlaybackDialog(temp);
                }
                return true;
            case android.R.id.home:
                startActivity(new Intent(this, MainScreenActivity.class));
                finish();
                return true;
            case R.id.itemEqualizer:
                startActivity(new Intent(this,EqualizerActivity.class));
                return true;
            case R.id.itemSleep:
                SleepDialog sleepDialog = new SleepDialog();
                sleepDialog.show(getSupportFragmentManager(),tagSleepDialog);
                return true;
            case R.id.itemReturnSmall:
                startActivity(getReturnSmallIntent(this, audios));
                finish();
                return true;
            case R.id.itemSetAsRingtone:
                if(audios.size() > 0)
                    Utils.setRingtone(getBaseContext(), audios.get(PlayingService.indexCurrentTrack));
                return true;
            case R.id.itemGetLiricks:
                if(audios.size() > 0)
                {
                    if(PlayingService.isOnline)
                        AbstractList.startLyricsDialog(getSupportFragmentManager(), audios.get(PlayingService.indexCurrentTrack).getLyrics_id());
                    else
                    {
                        Audio audio = audios.get(PlayingService.indexCurrentTrack);
                        AbstractList.startLiryctDialog(getSupportFragmentManager(), audio.getArtist(), audio.getTitle());
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onClickActionBar()
    {
        super.onClickActionBar();
        finish();
    }

    private void initializeService()
    {

        if(PlayingService.isOnline)
            intentService.setAction(PlayingService.actionOnline);
        else
            intentService.setAction(PlayingService.actionOffline);

        intentService.putExtra(keyService, audios);
        bindService(intentService,serviceConnection,0);
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
        imgPlay = (ImageView)findViewById(R.id.imagePlay);
        viewPager = (CustomViewPager)findViewById(R.id.viewPager);
        viewPager.setOnPageChangeListener(pageChangeHandler);
        adapterPager = new PagePlaybackAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterPager);
    }
    private final ViewPager.OnPageChangeListener pageChangeHandler = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            if(playingService != null)
            {
                if(position > PlayingService.indexCurrentTrack)
                    playingService.playTrackFromList(PlayingService.indexCurrentTrack +1);

                else if(position < PlayingService.indexCurrentTrack)
                    playingService.playTrackFromList(PlayingService.indexCurrentTrack -1);
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
            return audios.size();
        }
        @Override
        public int getItemPosition(Object object)
        {
            return POSITION_NONE;
        }
    }

}
