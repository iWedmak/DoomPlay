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
import com.perm.vkontakte.api.*;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkFrGrActivity extends AbstractReceiver
{
    ListView listView;
    LinearLayout linearLoading;
    static ArrayList<User> users;
    static ArrayList<Group> groups;
    static boolean isFriend;
    public static final String actionFriends = "actionFrend";
    public static final String actionGroup = "actionGroup";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);

        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);

        isFriend = getIntent().getAction().equals(actionFriends);
        if(isFriend)
        {
            if(users == null)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handler.sendEmptyMessage(1);
                        try {
                            users = MainScreenActivity.api.getFriends(Account.account.user_id);
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
        else
        {
            if(groups == null)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handler.sendEmptyMessage(1);
                        try {
                            groups = MainScreenActivity.api.getGroups(Account.account.user_id,null);
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
                            if(isFriend)
                                return MainScreenActivity.api.getAudio(users.get(params[0]).uid,null,null,
                                        SettingActivity.getPreference(getBaseContext(),"countfrgr"));
                            else
                                return MainScreenActivity.api.getAudio(null,groups.get(params[0]).gid,null,
                                        SettingActivity.getPreference(getBaseContext(),"countfrgr"));

                        } catch (IOException e) {
                            e.printStackTrace(); cancel(false);
                        } catch (JSONException e) {
                            Log.e("JSONException","JSONexception",e); cancel(false);
                        } catch (KException e) {
                            e.printStackTrace();cancel(false);Toast.makeText(getBaseContext(),"acces to users or groups audio is denied",Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(ArrayList <Audio> audios)
                    {
                        super.onPostExecute(audios);
                        handler.sendEmptyMessage(2);
                        Intent intent = new Intent(getBaseContext(),ListVkActivity.class);
                        intent.setAction(ListVkActivity.actionJust);
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
            View view ;
            if(isFriend)
            {
                view = inflater.inflate(R.layout.item_friend,parent,false);
                ((TextView)view.findViewById(R.id.textFirstName)).setText(users.get(position).first_name);
                ((TextView)view.findViewById(R.id.textlastName)).setText(users.get(position).last_name);
            }
            else
            {
                view = inflater.inflate(R.layout.item_group,parent,false);
                ((TextView)view.findViewById(R.id.textGroup)).setText(groups.get(position).name);
            }
            return view;
        }
    }
}
