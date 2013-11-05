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


import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/*
    Base class for all classes which content layout with controls
 */

abstract class AbstractControls extends AbstractReceiver
{
    RelativeLayout linearControls;
    ImageView imgPlay;
    ImageView imgShuffle;
    ImageView imgRepeat;
    ImageView imgNext;
    ImageView imgPrevious;
    TextView textCurrentTime;
    TextView textTotalTime;
    SeekBar seekBar;
    private volatile boolean flagT;
    private final static int messageUpdate = 2957;
    boolean isShown;
    protected abstract void trackChanged();
    private static final String keySaveShown = "keySswn";

    @Override
    protected void onPause()
    {
        super.onPause();
        flagT = false;
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        checkImage();

        if(isShown)
            loadUpdateThread();

    }

    protected void showHide()
    {
        if(isShown)
        {
            linearControls.setVisibility(View.GONE);
            isShown = false;
            flagT = false;
        }
        else
        {
            linearControls.setVisibility(View.VISIBLE);
            isShown = true;
            loadUpdateThread();
        }
    }
    protected void checkIsShown(Bundle savedInstanceState)
    {
        if(savedInstanceState != null)
        {
            final boolean savedState = savedInstanceState.getBoolean(AbstractControls.keySaveShown);

            if((savedState && !isShown) || (!savedState && isShown))
            {
                showHide();
            }
            isShown = savedState;
        }
        else if(!isShown && SettingActivity.getPreferences(SettingActivity.keyShowControls))
            showHide();
    }

    private void checkImage()
    {
        if(PlayingService.isPlaying)
            imgPlay.setImageResource(R.drawable.pause);
        else
            imgPlay.setImageResource(R.drawable.play);

        if(PlayingService.isLoop)
            imgRepeat.setImageResource(R.drawable.repeat_enable);
        else
            imgRepeat.setImageResource(R.drawable.repeat_disable);

        if(PlayingService.isShuffle)
            imgShuffle.setImageResource(R.drawable.shuffle_enable);
        else
            imgShuffle.setImageResource(R.drawable.shuffle_disable);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(keySaveShown,isShown);
    }


    void initializeAbstract()
    {
        isShown = false;
        flagT = false;

        seekBar.setOnSeekBarChangeListener(seekBarHandler);
        imgNext.setOnClickListener(onClickControlsListener);
        imgPlay.setOnClickListener(onClickControlsListener);
        imgPrevious.setOnClickListener(onClickControlsListener);
        imgRepeat.setOnClickListener(onClickControlsListener);
        imgShuffle.setOnClickListener(onClickControlsListener);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        connectService();
    }

    // for override
    void clickWithoutAction()
    {}

    private final View.OnClickListener onClickControlsListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(!PlayingService.serviceAlive)
                clickWithoutAction();
            else if(playingService != null && !playingService.isLoadingTrack())
            {
                onClickControl(v.getId());
            }
        }
    };
    void onClickControl(int id)
    {
            switch (id)
            {
                case (R.id.imageShuffle):
                    playingService.setShuffle();
                    if(PlayingService.isShuffle)
                        imgShuffle.setImageResource(R.drawable.shuffle_enable);
                    else
                        imgShuffle.setImageResource(R.drawable.shuffle_disable);
                    break;
                case (R.id.imagePrevious):
                    playingService.previousSong();
                    break;
                case (R.id.imagePlay):
                    playingService.playPause();
                    break;
                case (R.id.imageNext):
                    playingService.nextSong();
                    break;
                case (R.id.imageRepeat):
                    playingService.setLoop();
                    if(PlayingService.isLoop)
                        imgRepeat.setImageResource(R.drawable.repeat_enable);
                    else
                        imgRepeat.setImageResource(R.drawable.repeat_disable);
                    break;
            }
    }

    private final SeekBar.OnSeekBarChangeListener seekBarHandler = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if(fromUser && playingService != null && !playingService.isLoadingTrack())
                playingService.setCurrentPosition(playingService.getDuration()*progress / 100);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar){}
    };

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
            }
        };
        intentFilter = new IntentFilter(PlayingService.actionTrackChanged);
        intentFilter.addAction(PlayingService.actionIconPause);
        intentFilter.addAction(PlayingService.actionIconPlay);
        registerReceiver(broadcastReceiver, intentFilter);
        isRegister = true;
    }
    void loadUpdateThread()
    {
        flagT = false;
        flagT = true;
        Thread threadUpdate = new Thread(runnable);
        threadUpdate.start();
    }

    @Override
    protected void onServiceBound() {
      loadUpdateThread();
    }

    private final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            while(flagT)
            {
                try
                {
                    Thread.sleep(450);
                }
                catch (InterruptedException e)
                {}
                handler.sendEmptyMessage(messageUpdate);
            }
        }
    };
    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(isFinishing())
                return;
            if(playingService != null && PlayingService.serviceAlive && textCurrentTime != null)
            {
                textCurrentTime.setText(Utils.milliSecondsToTimer(playingService.getCurrentPosition()*1000));
                textTotalTime.setText(Utils.milliSecondsToTimer(playingService.getDuration()*1000));
                seekBar.setProgress(playingService.getProgressPercentage());
            }
        }
    };
}
