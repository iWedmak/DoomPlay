package com.perm.DoomPlay;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class SearchVkActivity extends AbstractListVk
{

    EditText editQuery;
    TextView textNoResults;
    ImageView buttonSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_vk);

        initializeUi();
        initializeAbstract();
        checkIsShown(savedInstanceState);
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
    protected void onClickTrack(int position)
    {
        super.onClickTrack(position);
        hideKeyboard();
        ListVkActivity.currentAction = ListVkActivity.actionJust;
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
    void startVoice()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "voice search");
        startActivityForResult(intent, 23);
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
    TaskLoader taskLoader;
    void initializeUi()
    {
        editQuery = (EditText) findViewById(R.id.editQuery);
        listView = (ListView) findViewById(R.id.listSearch);
        textNoResults = (TextView)findViewById(R.id.textNoResult);
        buttonSearch = (ImageView)findViewById(R.id.imageSearchVk);
        buttonSearch.setOnClickListener(onClickSearch);
        listView.setOnItemClickListener(onItemTrackClick);
        adapter = new ListVkAdapter(new ArrayList<Audio>(),this);
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
    View.OnClickListener onClickSearch = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(!isLoading)
            {
                taskLoader = new TaskLoader();
                taskLoader.execute(editQuery.getText().toString());
                hideKeyboard();
            }
            else
                Toast.makeText(getBaseContext(),"please wait",Toast.LENGTH_SHORT);

        }
    };

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        taskLoader.cancel(true);
        isLoading = false;
    }

    class TaskLoader extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... params)
        {
            try
            {
                audios = MainScreenActivity.api.searchAudio(params[0],
                        SettingActivity.getPreference(getBaseContext(),"countvksearch"));

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
        protected void onPreExecute()
        {
            super.onPreExecute();
            linearLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            isLoading = false;
            linearLoading.setVisibility(View.GONE);
            adapter.changeData(audios);
            markItem(PlayingService.indexCurrentTrack,false);
        }
    }
}
