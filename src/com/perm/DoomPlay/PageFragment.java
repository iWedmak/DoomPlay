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


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.example.DoomPlay.R;

public class PageFragment extends SherlockFragment
{
    static final String argumentPageNumber = "arg_page_number";

    int pageNumber;
    static boolean isPortrait;

    static PageFragment newInstance(int page)
    {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(argumentPageNumber, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(argumentPageNumber);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;


        View page = isPortrait ? inflater.inflate(R.layout.page,container,false) : inflater.inflate(R.layout.page_landscape,container,false);
        Bitmap background;

        Song song = new Song(FullPlaybackActivity.tracks[pageNumber]);
        background = song.getBitmap(getActivity().getBaseContext());


        ImageView imgAlbum = (ImageView)page.findViewById(R.id.imageAlbum);
        TextView textNumberSong = (TextView)page.findViewById(R.id.textNumberSong);


        if(background != null)
            imgAlbum.setImageBitmap(background);

        textNumberSong.setText(pageNumber+1 + "/" + FullPlaybackActivity.tracks.length);

        return page;
    }
}
