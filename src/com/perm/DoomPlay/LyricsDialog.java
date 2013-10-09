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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class LyricsDialog extends DialogFragment
{
    private LinearLayout linearLoading;
    private TextView textView ;
    public final static String keyLyricsId = "get_lyrics";
    public final static String keyLyricsTitle = "lyr_Title";
    private String title;
    boolean isLoading ;
    private boolean isFirstResume;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        isFirstResume = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        isLoading = false;

        title = getArguments().getString(keyLyricsTitle);
        View view = inflater.inflate(R.layout.dialog_lyrics,container,false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        textView = (TextView)view.findViewById(R.id.textLyrics);
        return view;
    }
    private AsyncTask<Void,Void,String> task ;



    private void getLyrics()
    {

        task = new AsyncTask<Void,Void,String>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                isLoading = true;
                linearLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params)
            {
                if(!MainScreenActivity.isRegister)
                    return "For getting lyrics is necessary to sign in (vk)";


                try
                {
                    if(title == null)
                        return MainScreenActivity.api.getLyrics(getArguments().getLong(keyLyricsId));
                    else
                    {
                        ArrayList<Audio> audios = MainScreenActivity.api.searchAudio(title,1);
                        if(audios.size() == 1 && audios.get(0).getLyrics_id() != 0)
                            return MainScreenActivity.api.getLyrics(audios.get(0).getLyrics_id());
                        else
                        {
                            return "Sorry , can't find lyrics";
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();return e.getMessage();
                } catch (JSONException e) {
                    e.printStackTrace();return e.getMessage();
                } catch (KException e) {
                    e.printStackTrace();
                    ((AbstractReceiver)getActivity()).handleKException(e);
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String s)
            {
                super.onPostExecute(s);
                isLoading = false;
                linearLoading.setVisibility(View.GONE);
                textView.setText(s);
            }
        };
        task.execute();

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if(isLoading && task != null)
            task.cancel(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(lp);

        if(isFirstResume)
        {
            getLyrics();
            isFirstResume = false;
        }

    }
}
