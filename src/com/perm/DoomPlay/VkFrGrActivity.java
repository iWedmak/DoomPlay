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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.perm.vkontakte.api.*;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkFrGrActivity extends AbstractVkItems
{
    static ArrayList<User> users;
    static ArrayList<Group> groups;
    static boolean isFriend;
    public static final String actionFriends = "actionFrend";
    public static final String actionGroup = "actionGroup";


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_vk_pgf,menu);
        return true;
    }

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
                 getFriends();
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
                  getGroups();
            }
            else
            {
                listView.setAdapter(new VkAlbumsAdapter());
            }
        }
    }
    void getFriends()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                handler.sendEmptyMessage(1);
                isLoading= true;
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
                isLoading = false;
                handler.sendEmptyMessage(3);

            }
        }).start();
    }
    void getGroups()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = true;
                handler.sendEmptyMessage(1);
                try
                {
                    groups = MainScreenActivity.api.getGroups(Account.account.user_id,100);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (KException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(2);
                isLoading = false;
                handler.sendEmptyMessage(3);

            }
        }).start();
    }

    @Override
    protected void onClickRefresh()
    {
        if(isFriend)
             getFriends();
        else
             getGroups();
    }


    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                linearLoading.setVisibility(View.VISIBLE);
            }
            else if(msg.what == 2)
            {
                linearLoading.setVisibility(View.GONE);
            }
            else if(msg.what == 3)
            {
                listView.setAdapter(new VkAlbumsAdapter());
            }
        }
    };



    @Override
    protected ArrayList<Audio> getAudios(int position)
    {

        try
        {
            if(isFriend)
                return MainScreenActivity.api.getAudio(users.get(position).uid,null,null,
                            SettingActivity.getPreference("countfrgr"));

            else
                return MainScreenActivity.api.getAudio(null,groups.get(position).gid,null,
                        SettingActivity.getPreference("countfrgr"));
        }
        catch (IOException e) {
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
            if(isFriend)
                return users.size();
            else
                return groups.size();
        }

        @Override
        public Object getItem(int position)
        {
            if(isFriend)
                return users.get(position);
            else
                return groups.get(position);
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
