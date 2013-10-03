package com.perm.DoomPlay;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractList extends AbstractControls
{
    ListView listView;
    ListsAdapter adapter;
    static protected ArrayList<Audio> audios;
    static boolean isLoading = false;
    LinearLayout linearLoading;
    protected PlaylistDB playlistDB;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        playlistDB = PlaylistDB.getInstance(this);
    }
    protected void markItem(int position , boolean withScroll)
    {
        if(PlayingService.serviceAlive && equalsCollections(PlayingService.audios, audios))
        {

            adapter.setMarkedItem(position);
            if(withScroll && SettingActivity.getPreferences(SettingActivity.keyScroll) && Build.VERSION.SDK_INT >= 8)
                listView.smoothScrollToPosition(position);
        }
        else
            adapter.setMarkedItem(PlayingService.valueIncredible);
    }
     public static boolean equalsCollections(List<Audio> first,List<Audio> second)
     {
         if(first == null || first.size() != second.size())
             return false;

         for (int i = 0 ; i < first.size(); i++)
             if (!first.get(i).equals(second.get(i)))
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
    protected final AdapterView.OnItemClickListener onItemTrackClick = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            onClickTrack(position);
        }
    };
    protected final ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {

            getMenuInflater().inflate(R.menu.action_vk,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {

            handleActionMode(item.getItemId(), (Integer) mode.getTag());
            mode.finish();
            return true;

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    };

    void handleActionMode(int itemId,int position)
    {
        if(Utils.isOnline(getBaseContext()))
        {
            switch (itemId)
            {
                case  R.id.itemGetLiricks:
                    startLyricsDialog(getSupportFragmentManager(), audios.get(position).getLyrics_id());
                    break;
                case R.id.itemLike:
                    if(!PlaylistDB.isLoading)
                        likeTrack(position);
                    else
                        waitMessage(getBaseContext());
                    break;
                case R.id.itemDislike:
                    if(!PlaylistDB.isLoading)
                        dislikeTrack(position);
                    else
                        waitMessage(getBaseContext());
                    break;
                case R.id.itemDownload:
                    if(DownloadingService.isDownloading(audios.get(position).getAid()))
                        Toast.makeText(getBaseContext(),"track is still downloading",Toast.LENGTH_SHORT).show();
                    else
                    {
                        Intent downloadIntent = new Intent(this,DownloadingService.class);
                        downloadIntent.putExtra(DownloadingService.keyDownload,(Parcelable)audios.get(position));
                        startService(downloadIntent);
                    }
                    break;
                case R.id.itemMoveToAlbum:
                    moveToAlbum(getSupportFragmentManager(), audios.get(position).getAid());
                    break;
            }
        }
    }
    public static void waitMessage(Context context)
    {
        Toast.makeText(context,"please wait",Toast.LENGTH_SHORT).show();
    }

    static void moveToAlbum(FragmentManager manager,long aid)
    {
        AddTrackToAlbumDialog dialog = new AddTrackToAlbumDialog();
        Bundle bundle = new Bundle();
        bundle.putLong(AddTrackToAlbumDialog.keyDialogAlbum,aid);
        dialog.setArguments(bundle);
        dialog.show(manager,"tag");

    }


    void dislikeTrack(int position)
    {
        if(Utils.isOnline(getBaseContext()))
        {
            new AsyncTask<Integer, Void, Integer>()
            {
                @Override
                protected Integer doInBackground(Integer... params)
                {
                    try
                    {
                        MainScreenActivity.api.deleteAudio(audios.get(params[0]).getAid(),Account.account.user_id);
                        playlistDB.deleteTrack(params[0],PlaylistDB.TABLE_VK);
                        playlistDB.setAcordingPositions(params[0],PlaylistDB.TABLE_VK);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e)
                    {

                        if(AbstractVkItems.handleKException(e, getBaseContext()))
                            finish();
                    }
                    return params[0];
                }

                @Override
                protected void onPostExecute(Integer position)
                {
                    super.onPostExecute(position);
                    audios.remove((int) position);
                    TracksHolder.audiosVk = audios;
                    adapter.changeData(audios);

                    if(position == PlayingService.indexCurrentTrack && equalsCollections(PlayingService.audios,audios))
                        playingService.playTrackFromList(PlayingService.indexCurrentTrack);
                }
            }.execute(position);
        }
    }
    public static void startLyricsDialog(FragmentManager manager, long lid)
    {
        Bundle bundle = new Bundle();
        bundle.putLong(LyricsDialog.keyLyricsId, lid);
        LyricsDialog dialog = new LyricsDialog();
        dialog.setArguments(bundle);
        dialog.show(manager,"tag");
    }


    void likeTrack(int position)
    {
        if(Utils.isOnline(getBaseContext()))
        {
            new AsyncTask<Integer, Void, Void>()
            {
                @Override
                protected Void doInBackground(Integer... params)
                {
                    try
                    {
                       MainScreenActivity.api.addAudio(audios.get(params[0]).getAid(), audios.get(params[0]).getOwner_id());
                       TracksHolder.audiosVk.add(0,audios.get(params[0]));

                       playlistDB.addVkTracks(TracksHolder.audiosVk);


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {

                        if(AbstractVkItems.handleKException(e, getBaseContext()))
                            finish();
                    }
                    return null;

                }

            }.execute(position);
        }
    }
    final AdapterView.OnItemLongClickListener onItemLongVkListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            startSupportActionMode(callback).setTag(position);
            return true;
        }
    };
    protected void onClickTrack(int position)
    {
        if(PlayingService.isLoadingTrack)
        {
            waitMessage(this);
            return;
        }

        if(!PlayingService.serviceAlive)
        {
            intentService.putExtra(FullPlaybackActivity.keyIndex,position);
            intentService.putExtra(FullPlaybackActivity.keyService,audios);

            connectService();
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
