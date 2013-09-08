package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Api;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

abstract class AbstractListVk extends AbstractControls
{
    ListView listView;
    ListVkAdapter adapter;
    static protected ArrayList<Audio> audios;
    Api api;
    @Override
    protected void initializeAbstract()
    {
        super.initializeAbstract();
        api=new Api(MainScreenActivity.account.access_token, LoginActivity.API_ID);
    }

    protected void markItem(int position , boolean withScroll)
    {
        if(PlayingService.serviceAlive && PlayingService.audios.equals(audios))
        {
            adapter.setMarkedItem(position);
            if(withScroll && SettingActivity.getPreferences(this,SettingActivity.keyScroll) && Build.VERSION.SDK_INT >= 8)
                listView.smoothScrollToPosition(position);
        }
        else
            adapter.notifyDataSetChanged();
    }
    @Override
    protected void trackChanged()
    {
        markItem(PlayingService.indexCurrentTrack,true);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        markItem(PlayingService.indexCurrentTrack,false);
    }

    protected void goFullScreen()
    {
        if(PlayingService.serviceAlive)
        {
            Intent intent = new Intent(getBaseContext(),FullPlaybackActivity.class);
            intent.putExtra(FileSystemActivity.keyMusic,audios);
            intent.setAction(FullPlaybackActivity.actionReturnFull);
            startActivity(intent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemShowHide:
                showHide();
                return true;
            case R.id.itemEqualizer:
                startEqualizer();
                return true;
            case R.id.itemSleep:
                SleepDialog sleepDialog = new SleepDialog();
                sleepDialog.show(getSupportFragmentManager(),FullPlaybackActivity.tagSleepDialog);
                return true;
            case R.id.itemFullScreen:
                //goFullScreen();
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
    protected AdapterView.OnItemClickListener onItemTrackClick = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            onClickTrack(position);
        }
    };
    ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getSupportMenuInflater().inflate(R.menu.action_vk,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            if(!Utils.isOnline(getBaseContext()))
                return false;
            else
            {
                final int position =(Integer) mode.getTag();
                if(item.getItemId() == R.id.itemGetLiricks)
                {
                    getLyriks(position);
                }
                else if(item.getItemId() == R.id.itemLike)
                {
                    likeTrack(position);
                }
                mode.finish();
                return true;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    };
    void getLyriks(int position)
    {

    }
    void likeTrack(int position)
    {
        AsyncTask<Long,Void,Void> asyncTask = new AsyncTask<Long, Void, Void>()
        {
            @Override
            protected Void doInBackground(Long... params)
            {
                try
                {
                    api.addAudio(params[0],MainScreenActivity.account.user_id);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (KException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        asyncTask.execute(audios.get(position).aid);
    }
    AdapterView.OnItemLongClickListener onItemLongVkListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            startActionMode(callback).setTag(position);
            return true;
        }
    };
    protected void onClickTrack(int position)
    {
        if(!PlayingService.isOnline)
        {
            stopService(new Intent(getBaseContext(),PlayingService.class));
            PlayingService.serviceAlive = false;
        }

        if(!PlayingService.serviceAlive)
        {
            intentService.putExtra(FullPlaybackActivity.keyIndex,position);
            intentService.putExtra(FullPlaybackActivity.keyService,audios);
            if(PlayingService.isClosed)
            {
                bindService(intentService,serviceConnection,0);
                PlayingService.isClosed = false;
            }
            startService(intentService);
        }
        else
        {
            PlayingService.audios = audios;
            PlayingService.isPlaying = true;

            playingService.playTrackFromList(position);
        }
    }
}
