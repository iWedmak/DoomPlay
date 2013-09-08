package com.perm.DoomPlay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.example.DoomPlay.R;

public class ListVkActivity extends AbstractListVk
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_tracks);

        audios  = getIntent().getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);

        initializeUi();
        initializeAbstract();
        checkIsShown(savedInstanceState);
    }

    void initializeUi()
    {
        adapter = new ListVkAdapter(audios,this);
        listView = (ListView)findViewById(R.id.listAllSongs);
        listView.setOnItemClickListener(onItemTrackClick);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(onItemLongVkListener);
        linearControls = (RelativeLayout)findViewById(R.id.linearControls);
        intentService = new Intent(this,PlayingService.class);
        intentService.setAction(PlayingService.actionOnline);
        imgPlay = (ImageView)findViewById(R.id.imagePlay) ;
        imgShuffle = (ImageView)findViewById(R.id.imageShuffle);
        imgRepeat = (ImageView)findViewById(R.id.imageRepeat);
        imgNext = (ImageView)findViewById(R.id.imageNext);
        imgPrevious = (ImageView)findViewById(R.id.imagePrevious);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        textCurrentTime = (TextView)findViewById(R.id.textElapsed);
        textTotalTime = (TextView)findViewById(R.id.textDuration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.bar_list,menu);
        return true;
    }
}
