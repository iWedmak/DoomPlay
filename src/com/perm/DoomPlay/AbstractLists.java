package com.perm.DoomPlay;

import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

abstract class AbstractLists extends AbstractControls
{
    PlaylistDB playlistDB ;
    ListView listView;
    ListTracksAdapter adapter;
    static protected String[] tracks;

    protected void markItem(int position , boolean withScroll)
    {
        if(PlayingService.serviceAlive && !PlayingService.isOnline && Arrays.equals(PlayingService.tracks, tracks))
        {
            adapter.setMarkedItem(position);
            if(withScroll && SettingActivity.getPreferences(this,SettingActivity.keyScroll) && Build.VERSION.SDK_INT >= 8)
                listView.smoothScrollToPosition(position);
        }
        else
            adapter.setMarkedItem(PlayingService.valueIncredible);
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
            intent.putExtra(FileSystemActivity.keyMusic,tracks);
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
            case R.id.itemAddMusic:
                Intent intentAlb = new Intent(getBaseContext(),AlbumArtistActivity.class);
                intentAlb.setAction(AlbumArtistActivity.actionAddAlbum);
                startActivity(intentAlb);
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
    protected void onClickTrack(int position)
    {
        if(PlayingService.isOnline)
        {
            stopService(new Intent(getBaseContext(),PlayingService.class));
            PlayingService.serviceAlive = false;
        }

        if(!PlayingService.serviceAlive)
        {
            intentService.putExtra(FullPlaybackActivity.keyIndex,position);
            intentService.putExtra(FullPlaybackActivity.keyService,tracks);
            connectService();
            startService(intentService);
        }
        else
        {
            PlayingService.tracks = tracks;
            PlayingService.isPlaying = true;

            playingService.playTrackFromList(position);
        }
    }

}
