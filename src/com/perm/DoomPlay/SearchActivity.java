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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.DoomPlay.R;

import java.util.ArrayList;

public class SearchActivity extends AbstractLists
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
            startActionMode(callback).setTag(position);
            return true;
        }
    };
    private void initialize()
    {
        isShown = false;
        editQuery = (EditText) findViewById(R.id.editQuery);
        listView = (ListView) findViewById(R.id.listSearch);
        editQuery.addTextChangedListener(onTextChangeHandler);
        editQuery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                linearControls.setVisibility(View.GONE);
                isShown = false;
            }
        });
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
        adapter = new ListTracksAdapter(new String[0],getBaseContext());
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
            ArrayList<String> list = new ArrayList<String>();
            String query = s.toString();

            if(query.equals(""))
            {
                textNoResults.setVisibility(View.GONE);
                adapter.changeData(new String[0]);
                return;
            }

            for(String track : TracksHolder.songAllPath)
            {
                if(track.toLowerCase().contains(query.toLowerCase()))
                    list.add(track);
            }
            tracks = new String[list.size()];
            tracks = list.toArray(tracks);

            if(tracks.length == 0)
                textNoResults.setVisibility(View.VISIBLE);
            else
                textNoResults.setVisibility(View.GONE);

            adapter.changeData(tracks);
            adapter.setMarkedItem(PlayingService.valueTrackNotChanged);
        }
    };
    ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getSupportMenuInflater().inflate(R.menu.action_option,menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {return false;}

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {

            switch(item.getItemId())
            {
                case R.id.itemToPlaylist:
                    AddTrackFromPlaybackDialog dialog = new AddTrackFromPlaybackDialog();
                    Bundle bundle = new Bundle();
                    bundle.putStringArray(AddTrackFromPlaybackDialog.keyBundleDialog,
                            new String[]{tracks[(Integer)mode.getTag()]});

                    dialog.setArguments(bundle);
                    dialog.show(getSupportFragmentManager(), "tag");
                    mode.finish();
                    break;
                case R.id.itemSetAsRingtone:
                    Utils.setRingtone(getBaseContext(), tracks[(Integer) mode.getTag()]);
                    mode.finish();
                    break;
            }

              return true;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode){}

    };

    void startVoice()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "voice search");
        startActivityForResult(intent, REQUEST_CODE);
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
        if(!MainScreenActivity.isOldSDK)
            getSupportMenuInflater().inflate(R.menu.bar_search,menu);
        else
            getSupportMenuInflater().inflate(R.menu.bar_search_old,menu);
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
