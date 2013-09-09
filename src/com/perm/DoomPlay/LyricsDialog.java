package com.perm.DoomPlay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Api;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;

public class LyricsDialog extends SherlockDialogFragment
{
    LinearLayout linearLoading;
    TextView textView ;
    Api api ;
    RelativeLayout relativeLyrics;
    public static String keyLyrics = "get_lyrics";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.lyrics_dialog,container,false);
        getDialog().setTitle("Lyrics");
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        relativeLyrics = (RelativeLayout)view.findViewById(R.id.relativeLyrics);
        textView = (TextView)view.findViewById(R.id.textLyrics);
        api=new Api(MainScreenActivity.account.access_token, LoginActivity.API_ID);
        return view;
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


        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                MainScreenActivity.isLoading = true;
                linearLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params)
            {
                try {
                    return api.getLyrics(getArguments().getLong(keyLyrics));

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
                MainScreenActivity.isLoading = false;
                linearLoading.setVisibility(View.GONE);
                textView.setText(s);
            }
        }.execute();
    }
}
