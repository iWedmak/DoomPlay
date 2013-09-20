package com.perm.DoomPlay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;

public class LyricsDialog extends DialogFragment
{
    LinearLayout linearLoading;
    TextView textView ;
    RelativeLayout relativeLyrics;
    public static String keyLyrics = "get_lyrics";
    boolean isLoading ;
    boolean isFirstResume;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        isLoading = false;
        isFirstResume = true;
        View view = inflater.inflate(R.layout.dialog_lyrics,container,false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        relativeLyrics = (RelativeLayout)view.findViewById(R.id.relativeLyrics);
        textView = (TextView)view.findViewById(R.id.textLyrics);
        return view;
    }
    AsyncTask<Void,Void,String> task ;

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
                try {
                    return MainScreenActivity.api.getLyrics(getArguments().getLong(keyLyrics));

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (KException e) {
                    e.printStackTrace();
                }
                return "error";
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
