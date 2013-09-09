package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Api;
import com.perm.vkontakte.api.Audio;
import com.perm.vkontakte.api.KException;
import com.perm.vkontakte.api.User;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkFriendsActivity extends AbstractReceiver
{
    ListView listView;
    Api api;
    LinearLayout linearLoading;
    static ArrayList<User> users;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);

        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);

        api = new Api(MainScreenActivity.account.access_token, LoginActivity.API_ID);

        if(users == null)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    handler.sendEmptyMessage(1);
                    try {
                        users = api.getFriends(MainScreenActivity.account.user_id);
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
                            return api.getAudio(users.get(params[0]).uid,null);

                        } catch (IOException e) {
                            e.printStackTrace(); cancel(false);
                        } catch (JSONException e) {
                            Log.e("JSONException","JSONexception",e); cancel(false);
                        } catch (KException e) {
                            e.printStackTrace();cancel(false);Toast.makeText(getBaseContext(),"acces to users audio is denied",Toast.LENGTH_SHORT).show();
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
            return users.size();
        }

        @Override
        public Object getItem(int position)
        {
            return users.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = inflater.inflate(R.layout.item_friend,parent,false);
            ((TextView)view.findViewById(R.id.textFirstName)).setText(users.get(position).first_name);
            ((TextView)view.findViewById(R.id.textlastName)).setText(users.get(position).last_name);
            return view;
        }
    }
}
