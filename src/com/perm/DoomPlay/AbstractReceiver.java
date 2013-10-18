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
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.perm.vkontakte.api.KException;

import java.util.ArrayList;
import java.util.Locale;

/*
 base class for all activities
 */

abstract class AbstractReceiver extends ActionBarActivity
{
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    private TextView textArtist;
    private TextView textTitle;
    boolean isRegister;
    private BroadcastReceiver broadcastReceiverKiller;
    public static final String actionKill = "killAllActivities";

    public void handleKException(KException e)
    {
        if(e.getMessage().contains("autorization failded"))
        {
            MainScreenActivity.isRegister = false;
        }
        showException(e);
    }


    private String message = null;
    public void showException(Exception e)
    {
        e.printStackTrace();
        message = e.getMessage();
        runOnUiThread(runnable);
    }

    private final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            Toast.makeText(getBaseContext(),message, Toast.LENGTH_SHORT).show();
        }
    };


    void showPlaybackDialog(ArrayList<Audio> audios)
    {
        if(!PlaylistDB.isLoading)
        {
            AddTrackFromPlaybackDialog dialog = new AddTrackFromPlaybackDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(AddTrackFromPlaybackDialog.keyBundleDialog, audios);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(),"tag");
        }
        else
            AbstractList.waitMessage(this);


    }

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
    void onClickActionBar()
    {
        if(PlayingService.audios != null)
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

    private void prepareLang()
    {
        String lang = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance()).getString("languages",
                Locale.getDefault().getLanguage());

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        prepareLang();


        prepareActionBar();
        createBroadcastRec();
        initializeKiller();
    }
    void prepareActionBar()
    {
        ActionBar bar = getSupportActionBar();
        View view = getLayoutInflater().inflate(R.layout.action_bar_cust,null);
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

    void updateActionBar()
    {
        if(PlayingService.serviceAlive && PlayingService.audios != null)
        {
            textTitle.setText(PlayingService.audios.get(PlayingService.indexCurrentTrack).getTitle());
            textArtist.setText(PlayingService.audios.get(PlayingService.indexCurrentTrack).getArtist());
            textArtist.setVisibility(View.VISIBLE);
            textTitle.setTextColor(getResources().getColor(R.color.blue_text));
        }
        else
        {
            textTitle.setText("DoomPlay");
            textTitle.setTextColor(getResources().getColor(R.color.almost_white));
            textArtist.setVisibility(View.GONE);
        }
        ActivityCompat.invalidateOptionsMenu(this);

    }
    void createBroadcastRec()
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
        switch(item.getItemId())
        {
            case android.R.id.home:
                startActivity(new Intent(this, MainScreenActivity.class));
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
}
