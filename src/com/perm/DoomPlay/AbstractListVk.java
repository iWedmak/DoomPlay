package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Account;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    protected void markItem(int position , boolean withScroll)
    {
        if(PlayingService.serviceAlive && PlayingService.isOnline && equalsCollections(PlayingService.audios, audios))
        {

            adapter.setMarkedItem(position);
            if(withScroll && SettingActivity.getPreferences(this,SettingActivity.keyScroll) && Build.VERSION.SDK_INT >= 8)
                listView.smoothScrollToPosition(position);
        }
        else
            adapter.notifyDataSetChanged();
    }
     public static boolean equalsCollections(ArrayList<Audio> first, ArrayList<Audio> second)
     {
         if(first == null || first.size() != second.size())
             return false;

         for (int i = 0 ; i < first.size(); i++)
             if (!first.get(i).equal(second.get(i)))
                return false;

         return true;
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
                goFullScreen();
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
    final ActionMode.Callback callback = new ActionMode.Callback()
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
                handleActionMode(item.getItemId(), (Integer) mode.getTag());
                mode.finish();
                return true;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    };
    void handleActionMode(int itemId,int position)
    {
        switch (itemId)
        {
            case  R.id.itemGetLiricks:
                getLyriks(position);
                break;
            case R.id.itemLike:
                likeTrack(position);
                break;
            case R.id.itemDislike:
                dislikeTrack(position);
                break;
        }
    }

    void dislikeTrack(int position)
    {
        if(Utils.isOnline(getBaseContext()))
        {
            new AsyncTask<Long, Void, Void>()
            {
                @Override
                protected Void doInBackground(Long... params)
                {
                    try
                    {
                        MainScreenActivity.api.deleteAudio(params[0],Account.account.user_id);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(audios.get(position).aid);
        }
    }

    void getLyriks(int position)
    {
        long lid = audios.get(position).lyrics_id;
        if(lid != 0 && Utils.isOnline(getBaseContext()))
        {
            Bundle bundle = new Bundle();
            bundle.putLong(LyricsDialog.keyLyrics, lid);
            LyricsDialog dialog = new LyricsDialog();
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(),"tag");

        }
        else
            Toast.makeText(this,"can't find lyrics",Toast.LENGTH_SHORT);
    }
    void likeTrack(int position)
    {
        if(Utils.isOnline(getBaseContext()))
        {
            new AsyncTask<Long, Void, Void>()
            {
                @Override
                protected Void doInBackground(Long... params)
                {
                    try
                    {
                        MainScreenActivity.api.addAudio(params[0], Account.account.user_id);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(audios.get(position).aid);
        }
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
        if(!PlayingService.isLoadingTrack)
        {
            Toast.makeText(getBaseContext(),"please wait",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!PlayingService.isOnline)
        {
            stopService(new Intent(getBaseContext(),PlayingService.class));
            PlayingService.serviceAlive = false;
        }

        if(!PlayingService.serviceAlive)
        {
            intentService.putExtra(FullPlaybackActivity.keyIndex,position);
            intentService.putExtra(FullPlaybackActivity.keyService,audios);

            bindService(intentService,serviceConnection,BIND_IMPORTANT);
            startService(intentService);
        }
        else
        {
            PlayingService.audios = audios;
            PlayingService.isPlaying = true;

            playingService.playTrackFromList(position);
        }
    }

    @Override
    void onClickControl(int id)
    {
        if(!PlayingService.isLoadingTrack)
            super.onClickControl(id);
        else
            Toast.makeText(this,"please wait",Toast.LENGTH_SHORT);
    }
}
