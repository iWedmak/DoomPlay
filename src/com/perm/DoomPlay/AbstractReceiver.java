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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

abstract class AbstractReceiver extends ActionBarActivity
{
    protected BroadcastReceiver broadcastReceiver;
    protected IntentFilter intentFilter;
    protected TextView textArtist;
    protected TextView textTitle;
    protected boolean isRegister;
    private BroadcastReceiver broadcastReceiverKiller;
    public static final String actionKill = "killAllActivities";

    @Override
    protected void onPause()
    {
        super.onPause();
        if(isRegister)
        {
            isRegister = false;
            unregisterReceiver(broadcastReceiver);
        }
    }
    private void onClickActionBar()
    {
        if((!PlayingService.isOnline && PlayingService.tracks != null)
                || (PlayingService.isOnline && PlayingService.audios != null))
        {
            startActivity(FullPlaybackActivity.returnSmall(this));
        }
    }

    private void initializeKiller()
    {
        IntentFilter intentFilterKiller = new IntentFilter(actionKill);
        broadcastReceiverKiller = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(PlayingService.serviceAlive)
                {
                    stopService(new Intent(getBaseContext(),PlayingService.class));
                    PlayingService.serviceAlive = false;
                }
                finish();
            }
        };
        registerReceiver(broadcastReceiverKiller, intentFilterKiller);
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(broadcastReceiverKiller);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        prepareActionBar();
        createBroadcastRec();
        initializeKiller();
    }
    protected void prepareActionBar()
    {
        ActionBar bar = getSupportActionBar();
        View view;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            view = getLayoutInflater().inflate(R.layout.action_bar_cust,null);
        else
            view = getLayoutInflater().inflate(R.layout.action_bar_landscape,null);
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickActionBar();
            }
        });

        textArtist = (TextView)view.findViewById(R.id.textControlArtist);
        textTitle = (TextView)view.findViewById(R.id.textControlTitle);
        bar.setDisplayShowCustomEnabled(true);
        bar.setCustomView(view);
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
    }

    protected void updateActionBar()
    {
        if(PlayingService.serviceAlive && PlayingService.tracks != null && !PlayingService.isOnline)
        {
            Song song = new Song(PlayingService.tracks[PlayingService.indexCurrentTrack]);

            textTitle.setText(song.getTitle());
            textArtist.setText(song.getArtist());
            textArtist.setVisibility(View.VISIBLE);
            textTitle.setTextColor(getResources().getColor(R.color.blue_text));

        }
        else if(PlayingService.serviceAlive && PlayingService.audios != null && PlayingService.isOnline)
        {
            textTitle.setText(PlayingService.audios.get(PlayingService.indexCurrentTrack).title);
            textArtist.setText(PlayingService.audios.get(PlayingService.indexCurrentTrack).artist);
            textArtist.setVisibility(View.VISIBLE);
            textTitle.setTextColor(getResources().getColor(R.color.blue_text));
        }
        else
        {
            textTitle.setText("DoomPlay");
            textTitle.setTextColor(getResources().getColor(R.color.almost_white));
            textArtist.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();

    }
    protected void createBroadcastRec()
    {
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(action.equals(PlayingService.actionTrackChanged) ||(action.equals(PlayingService.actionIconPlay)
                        || action.equals(PlayingService.actionIconPause)))
                {
                    updateActionBar();
                }
            }
        };
        intentFilter = new IntentFilter(PlayingService.actionTrackChanged);
        intentFilter.addAction(PlayingService.actionIconPause);
        intentFilter.addAction(PlayingService.actionIconPlay);
        registerReceiver(broadcastReceiver, intentFilter);
        isRegister = true;
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if(!isRegister)
        {
            isRegister = true;
            registerReceiver(broadcastReceiver,intentFilter);
        }
        updateActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            startActivity(new Intent(this, MainScreenActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
