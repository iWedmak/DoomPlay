package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;

import java.util.Random;

public class MainScreenFragment extends SherlockFragment
{
    MainScreenActivity activity;
    LinearLayout linearLoading;

    void scan()
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                MainScreenActivity.isLoading = true;
                linearLoading.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                TracksHolder.scanCard(activity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                linearLoading.setVisibility(View.GONE);
                MainScreenActivity.isLoading = false;
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        activity =(MainScreenActivity)getActivity();
        activity.setScanCallback(new MainScreenActivity.IScanCallback()
        {
            @Override
            public void scanI()
            {
                scan();
            }
        });
        View view = inflater.inflate(R.layout.mainscreen_fragment,container,false);
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        view.findViewById(R.id.linearAlbums).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearAllSongs).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearArtists).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearPlaylists).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearFolders).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearSearch).setOnClickListener(onClickMainLinear);


        if(!TracksHolder.isScanned)
            scan();

        return view;
    }
    View.OnClickListener onClickMainLinear = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(!MainScreenActivity.isLoading)
                switch (v.getId())
                {
                    case R.id.linearAllSongs:
                        goAllSongs();
                        break;
                    case R.id.linearAlbums:
                        goAlbums();
                        break;
                    case R.id.linearArtists:
                        goArtist();
                        break;
                    case R.id.linearPlaylists:
                        if(!AddTrackFromPlaybackDialog.isAdding)
                            startActivity(new Intent(activity,PlaylistActivity.class));
                        else
                            Toast.makeText(activity, "please wait, tracks didn't added yet", Toast.LENGTH_SHORT);
                        break;
                    case R.id.linearFolders:
                        startActivity(new Intent(activity,FileSystemActivity.class));
                        break;

                    case R.id.linearSearch:
                        startActivity(new Intent(activity,SearchActivity.class));
                        break;
                }
            else
                Toast.makeText(activity,"please wait",Toast.LENGTH_SHORT).show();
        }
    };


    static String[] getRandomTracks()
    {
        Random random = new Random();
        String[] randomTracks = new String[10];

        for(int i = 0 ; i < 10 ; i++)
        {
            randomTracks[i] = TracksHolder.songAllPath[random.nextInt(TracksHolder.songAllPath.length)];
        }

        return randomTracks;
    }
    void goArtist()
    {
        Intent intent = new Intent(AlbumArtistActivity.actionPlayArtist);
        intent.setClass(activity,AlbumArtistActivity.class);
        startActivity(intent);
    }
    void goAlbums()
    {
        Intent intent = new Intent(AlbumArtistActivity.actionPlayAlbum);
        intent.setClass(activity,AlbumArtistActivity.class);
        startActivity(intent);
    }
    void goAllSongs()
    {
        Intent intent = new Intent(activity,ListTracksActivity.class);
        intent.setAction(ListTracksActivity.actionJust);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,TracksHolder.songAllPath);
        startActivity(intent);
    }

}