package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkPopularActivity extends AbstractReceiver
{
    public final static String[] genres = {"Rock","Pop","Rap & Hip-Hop","Easy Listening",
            "Dance & House","Instrumental","Metal","Alternative","Dubstep","Jazz & Blues","Drum & Bass",
            "Trance","Chanson","Ethnic","Acoustic & Vocal","Reggae","Classical","Indie Pop","Speech","Electropop & Disco","Other"};
    public final static int[] acordingIds = {1,2,3,4,5,6,7,21,8,9,10,11,12,13,14,15,16,17,19,22,18};

    ListView listView;
    LinearLayout linearLoading;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);

        listView = (ListView)findViewById(R.id.listVk);
        listView.setAdapter(new VkPopularAdapter());
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);

        listView.setOnItemClickListener(onClickListener);
    }

    AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
              if(!MainScreenActivity.isLoading && Utils.isOnline(getBaseContext()))
              {
                  new AsyncTask<Integer,Void, ArrayList < Audio >>()
                  {
                      @Override
                      protected void onPreExecute()
                      {
                          super.onPreExecute();
                          MainScreenActivity.isLoading = true;
                          linearLoading.setVisibility(View.VISIBLE);
                      }

                      @Override
                      protected ArrayList<Audio> doInBackground(Integer... params)
                      {
                          try {
                              return MainScreenActivity.api.getAudioPopular(acordingIds[params[0]],
                                      SettingActivity.getPreference(getBaseContext(),"countvkpopular"));
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
                      protected void onPostExecute(ArrayList<Audio> audios)
                      {
                          super.onPostExecute(audios);
                          MainScreenActivity.isLoading = false;
                          linearLoading.setVisibility(View.GONE);


                          Intent intent = new Intent(getBaseContext(),ListVkActivity.class);
                          intent.setAction(ListVkActivity.actionJust);
                          intent.putExtra(MainScreenActivity.keyOpenInListTrack,audios);
                          startActivity(intent);
                      }
                  }.execute(position);
              }
              else
                  Toast.makeText(getBaseContext(),"please wait",Toast.LENGTH_SHORT).show();
        }


    };


    class VkPopularAdapter extends BaseAdapter
    {
        LayoutInflater inflater;
        public VkPopularAdapter()
        {
            inflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount()
        {
            return genres.length;
        }

        @Override
        public Object getItem(int position)
        {
            return genres[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = inflater.inflate(R.layout.item_genre,parent,false);
            ((TextView)view.findViewById(R.id.textGenre)).setText(genres[position]);

            return view;
        }
    }

}
