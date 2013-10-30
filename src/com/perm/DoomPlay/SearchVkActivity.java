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
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class SearchVkActivity extends AbstractList
{

    private EditText editQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_vk);

        initializeUi();
        initializeAbstract();
        checkIsShown(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.getBoolean(AbstractVkItems.keyLinerLoadingRestore,false))
        {
            linearLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_search,menu);
        return true;
    }
    private void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AbstractVkItems.keyLinerLoadingRestore,isLoading);
    }

    @Override
    protected void onClickTrack(int position)
    {
        super.onClickTrack(position);
        hideKeyboard();
        ListVkActivity.currentAction = ListVkActivity.actionJust;
        PlayingService.isOnline = true;
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

    public static Intent getVoiceIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "voice search");
        return intent;
    }

    void startVoice()
    {
        Intent intent = getVoiceIntent();

        if(Utils.isIntentAvailable(getBaseContext(),intent))
            startActivityForResult(intent, 23);
        else
            Toast.makeText(getBaseContext(),getResources().getString(R.string.doesnt_available),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 23 && resultCode == RESULT_OK)
        {
            editQuery.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onServiceAbstractConnected()
    {
        playingService.setOnLoadingTrackListener(new PlayingService.OnLoadingTrackListener()
        {
            //TODO: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
            //but i call it only in ui thread , is it a bug?
            @Override
            public void onLoadingTrackStarted()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        linearLoading.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onLoadingTrackEnded()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        linearLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private TaskLoader taskLoader;
    private void initializeUi()
    {
        editQuery = (EditText) findViewById(R.id.editQuery);
        listView = (ListView) findViewById(R.id.listSearch);
        ImageView buttonSearch = (ImageView) findViewById(R.id.imageSearchVk);
        buttonSearch.setOnClickListener(onClickSearch);
        listView.setOnItemClickListener(onItemTrackClick);

        audios = new ArrayList<Audio>();
        adapter = new ListsAdapter(audios,this);
        listView.setAdapter(adapter);
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
    private final View.OnClickListener onClickSearch = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            if(isLoading)
                AbstractList.waitMessage(getBaseContext());
            else if(!MainScreenActivity.isRegister)
                Toast.makeText(getBaseContext(),getResources().getString(R.string.please_sign_in),Toast.LENGTH_SHORT).show();
            else  if(!Utils.isOnline(getBaseContext()))
                Toast.makeText(getBaseContext(), getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            else
            {
                taskLoader = new TaskLoader();
                taskLoader.execute(editQuery.getText().toString());
                hideKeyboard();
            }
        }
    };

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(taskLoader != null)
            taskLoader.cancel(true);
        isLoading = false;
    }

    private class TaskLoader extends AsyncTask<String,Void,ArrayList<Audio>>
    {
        @Override
        protected ArrayList<Audio> doInBackground(String... params)
        {
            try
            {
                return MainScreenActivity.api.searchAudio(params[0],
                        SettingActivity.getPreference("countvksearch"));

            } catch (IOException e) {
                showException(e);
                cancel(false);
            } catch (JSONException e) {
                showException(e);
                cancel(false);
            } catch (KException e) {

                handleKException(e);
                cancel(false);
            }

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            linearLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            linearLoading.setVisibility(View.GONE);
            isLoading = false;
        }

        @Override
        protected void onPostExecute(ArrayList<Audio> aVoid)
        {
            super.onPostExecute(aVoid);
            isLoading = false;
            linearLoading.setVisibility(View.GONE);

            audios.clear();
            audios.addAll(aVoid);
            adapter.changeData(audios);
            markItem(PlayingService.indexCurrentTrack,false);
        }
    }
}
