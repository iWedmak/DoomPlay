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
import android.speech.RecognizerIntent;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.ArrayList;

public class SearchActivity extends AbstractList
{
    EditText editQuery;
    TextView textNoResults;
    public final static int REQUEST_CODE = 739;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        initialize();
        initializeAbstract();
        checkIsShown(savedInstanceState);

    }

    AdapterView.OnItemLongClickListener onItemLongTrackClick =  new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            startSupportActionMode(callback).setTag(position);
            return true;
        }
    };
    private void initialize()
    {
        editQuery = (EditText) findViewById(R.id.editQuery);
        listView = (ListView) findViewById(R.id.listSearch);
        editQuery.addTextChangedListener(onTextChangeHandler);
        textNoResults = (TextView)findViewById(R.id.textNoResult);
        linearControls = (RelativeLayout)findViewById(R.id.linearControls);
        intentService = new Intent(this,PlayingService.class);
        intentService.setAction(PlayingService.actionOffline);
        imgPlay = (ImageView)findViewById(R.id.imagePlay) ;
        imgShuffle = (ImageView)findViewById(R.id.imageShuffle);
        imgRepeat = (ImageView)findViewById(R.id.imageRepeat);
        imgNext = (ImageView)findViewById(R.id.imageNext);
        imgPrevious = (ImageView)findViewById(R.id.imagePrevious);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        textCurrentTime = (TextView)findViewById(R.id.textElapsed);
        textTotalTime = (TextView)findViewById(R.id.textDuration);

        listView.setOnItemClickListener(onItemTrackClick);
        listView.setOnItemLongClickListener(onItemLongTrackClick);
        adapter = new ListsAdapter(new ArrayList<Audio>(),getBaseContext());
        listView.setAdapter(adapter);

    }


    private void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onClickTrack(int position)
    {
        super.onClickTrack(position);
        hideKeyboard();
        ListTracksActivity.currentAction = ListVkActivity.actionJust;
    }

    TextWatcher onTextChangeHandler = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count){}
        @Override
        public void afterTextChanged(Editable s)
        {
            String query = s.toString();
            audios.clear();

            if(query.equals(""))
            {
                textNoResults.setVisibility(View.GONE);
                adapter.changeData(audios);
                return;
            }

            for(Audio audio : TracksHolder.allAudios)
            {
                if(audio.url.toLowerCase().contains(query.toLowerCase()))
                    audios.add(audio);
            }
            if(audios.size() == 0)
                textNoResults.setVisibility(View.VISIBLE);
            else
                textNoResults.setVisibility(View.GONE);

            adapter.changeData(audios);
            adapter.setMarkedItem(PlayingService.valueIncredible);
        }
    };
    ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getMenuInflater().inflate(R.menu.action_option,menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {return false;}

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            int position = (Integer)mode.getTag();

            switch(item.getItemId())
            {
                case R.id.itemToPlaylist:
                    ArrayList<Audio> temp = new ArrayList<Audio>();
                    temp.add(audios.get(position));
                    FileSystemActivity.showPlaybackDialog(temp ,getSupportFragmentManager());
                    break;
                case R.id.itemSetAsRingtone:
                    Utils.setRingtone(getBaseContext(), audios.get(position));
                    break;
                case R.id.itemGetLiricks:
                    Audio audio = audios.get(position);
                    ListTracksActivity.startLiryctDialog(getSupportFragmentManager(), audio.artist, audio.title);
                    break;
            }

            mode.finish();
            return true;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode){}

    };

    void startVoice()
    {
        startActivityForResult(SearchVkActivity.getVoiceIntent(), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            editQuery.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_search,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if(item.getItemId() == R.id.itemVoiceSearch)
        {
            startVoice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
