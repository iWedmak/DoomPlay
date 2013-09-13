package com.perm.DoomPlay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.AudioAlbum;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkAlbumsActivity extends AbstractVkItems
{
    static ArrayList<AudioAlbum> albums;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);
        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);


        if(albums == null && Utils.isOnline(getBaseContext()))
        {
             getAlbums();
        }
        else
        {
            listView.setAdapter(new VkAlbumsAdapter());
        }
    }
    void getAlbums()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                handler.sendEmptyMessage(1);
                try {
                    albums = MainScreenActivity.api.getAudioAlbums(Account.account.user_id,null,
                            SettingActivity.getPreference(getBaseContext(),"countvkall"));

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


    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                linearLoading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
            else if(msg.what == 2)
            {
                linearLoading.setVisibility(View.GONE);
                isLoading = false;
            }
            else if(msg.what == 3)
            {
                listView.setAdapter(new VkAlbumsAdapter());
            }

        }
    };

    @Override
    protected void startListVkActivity(ArrayList<Audio> audios)
    {
        Intent intent = new Intent(this,ListVkActivity.class);
        intent.setAction(ListVkActivity.actionMyAlbums);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,audios);
        startActivity(intent);
    }

    @Override
    protected void onClickRefresh()
    {
        getAlbums();
    }

    @Override
    protected ArrayList<Audio> getAudios(int position)
    {
        try
        {

            return MainScreenActivity.api.getAudio(null,null,albums.get(position).album_id
                    ,SettingActivity.getPreference(getBaseContext(),"countvkall"));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (KException e) {
            e.printStackTrace();
        }
        return null;
    }


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
