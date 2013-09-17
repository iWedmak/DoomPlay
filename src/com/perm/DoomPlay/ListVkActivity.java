package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class ListVkActivity extends AbstractListVk
{
    static String currentAction ;
    public static final String actionMyMusic ="actionMyMusic";
    public static final String actionMyAlbums = "actionMyAlbums";
    public static final String actionJust = "actionJust";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk_with_controlls);

        audios  = getIntent().getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);
        currentAction = getIntent().getAction();

        initializeUi();
        initializeAbstract();
        checkIsShown(savedInstanceState);
    }

    AsyncTask<Void,Void,Void> asyncTask;

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemRefresh:
                if(currentAction.equals(actionMyMusic) && !isLoading)
                    refreshAudios();


                return true;
            case R.id.itemInterrupt:
                asyncTask.cancel(true);
                isLoading = false;
                linearLoading.setVisibility(View.GONE);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    void refreshAudios()
    {
        asyncTask = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                isLoading = true;
                linearLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                try
                {
                    audios = TracksHolder.tempAudiosMine = MainScreenActivity.api.getAudio(Account.account.user_id,
                            null,null,SettingActivity.getPreference(getBaseContext(),"countvkall"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (KException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                isLoading = false;
                linearLoading.setVisibility(View.GONE);
                adapter.changeData(audios);
            }
        };
        asyncTask.execute();
    }

    private ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getSupportMenuInflater().inflate(R.menu.action_vk_my,menu);
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

    void initializeUi()
    {
        adapter = new ListVkAdapter(audios,this);
        listView = (ListView)findViewById(R.id.listAllSongs);
        listView.setOnItemClickListener(onItemTrackClick);
        listView.setAdapter(adapter);
        if(currentAction.equals(actionMyMusic))
        {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
                {
                    startActionMode(callback).setTag(position);
                    return true;
                }
            });
        }
        else
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
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        ArrayList<Audio> tempTracks = intent.getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);

        if(tempTracks != null)
            audios = tempTracks;


        currentAction  = intent.getAction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(currentAction.equals(actionJust))
            getSupportMenuInflater().inflate(R.menu.bar_list,menu);
        else
            getSupportMenuInflater().inflate(R.menu.bar_vk_mymusic,menu);
        return true;
    }
}
