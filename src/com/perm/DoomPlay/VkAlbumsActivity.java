package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Api;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.AudioAlbum;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkAlbumsActivity extends AbstractReceiver
{
    ListView listView;
    Api api;
    LinearLayout linearLoading;
    static ArrayList<AudioAlbum> albums;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);
        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);

        api = new Api(MainScreenActivity.account.access_token, LoginActivity.API_ID);

        if(albums == null && Utils.isOnline(getBaseContext()))
        {
             new Thread(new Runnable()
             {
                 @Override
                 public void run()
                 {
                     handler.sendEmptyMessage(1);
                     try {
                         albums = api.getAudioAlbums(MainScreenActivity.account.user_id,40);
                     } catch (IOException e) {
                         e.printStackTrace();
                     } catch (JSONException e) {
                         e.printStackTrace();
                     } catch (KException e) {
                         e.printStackTrace();
                     }
                     handler.sendEmptyMessage(2);
                     handler.sendEmptyMessage(3);
                 }
             }).start();
        }
        else
        {
            listView.setAdapter(new VkAlbumsAdapter());
        }
    }


    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                MainScreenActivity.isLoading = true;
                linearLoading.setVisibility(View.VISIBLE);
            }
            else if(msg.what == 2)
            {
                linearLoading.setVisibility(View.GONE);
                MainScreenActivity.isLoading = false;
            }
            else if(msg.what == 3)
            {
                listView.setAdapter(new VkAlbumsAdapter());
            }

        }
    };

    AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

            if(!MainScreenActivity.isLoading && Utils.isOnline(getBaseContext()))
            {
                new AsyncTask<Integer,Void,ArrayList<Audio>>()
                {
                    @Override
                    protected ArrayList<Audio> doInBackground(Integer... params)
                    {
                        handler.sendEmptyMessage(1);
                        try
                        {
                            return api.getAudio(null,albums.get(params[0]).album_id);



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
                        handler.sendEmptyMessage(2);
                        Intent intent = new Intent(getBaseContext(),ListVkActivity.class);
                        intent.putExtra(MainScreenActivity.keyOpenInListTrack,audios);
                        startActivity(intent);
                    }


                }.execute(position);
            }
            else
                Toast.makeText(getBaseContext(), "please wait", Toast.LENGTH_SHORT).show();
        }


    };


    class VkAlbumsAdapter extends BaseAdapter
    {
        LayoutInflater inflater;
        public VkAlbumsAdapter()
        {
            inflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount()
        {
            return albums.size();
        }

        @Override
        public Object getItem(int position)
        {
            return albums.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = inflater.inflate(R.layout.item_vk_album,parent,false);
            ((TextView)view.findViewById(R.id.textGenre)).setText(albums.get(position).title);

            return view;
        }
    }
}
