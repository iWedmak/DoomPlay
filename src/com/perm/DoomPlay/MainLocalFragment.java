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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainLocalFragment extends Fragment
{
    private LinearLayout linearLoading;

    void setLoading()
    {
        linearLoading.setVisibility(View.VISIBLE);
    }
    void unsetLoading()
    {
        linearLoading.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.mainscreen_fragment,container,false);
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        view.findViewById(R.id.linearAlbums).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearAllSongs).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearArtists).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearPlaylists).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearFolders).setOnClickListener(onClickMainLinear);
        view.findViewById(R.id.linearSearch).setOnClickListener(onClickMainLinear);

        return view;
    }
    private final View.OnClickListener onClickMainLinear = new View.OnClickListener()
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
                        if(!PlaylistDB.isLoading)
                            startActivity(new Intent(getActivity(),PlaylistActivity.class));
                        else
                            AbstractList.waitMessage(getActivity());
                        break;
                    case R.id.linearFolders:
                        startActivity(new Intent(getActivity(),FileSystemActivity.class));
                        break;

                    case R.id.linearSearch:
                        startActivity(new Intent(getActivity(),SearchActivity.class));
                        break;
                }
            else
                AbstractList.waitMessage(getActivity());
        }
    };

    void goArtist()
    {
        startActivity(new Intent(AlbumArtistActivity.actionPlayArtist).setClass(getActivity(),AlbumArtistActivity.class));
    }
    void goAlbums()
    {
        Intent intent = new Intent(AlbumArtistActivity.actionPlayAlbum);
        intent.setClass(getActivity(),AlbumArtistActivity.class);
        startActivity(intent);
    }
    void goAllSongs()
    {
        Intent intent = new Intent(getActivity(),ListTracksActivity.class);
        intent.setAction(ListTracksActivity.actionJust);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,TracksHolder.allAudios);
        startActivity(intent);
    }

}