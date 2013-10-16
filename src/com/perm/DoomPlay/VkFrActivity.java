package com.perm.DoomPlay;

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
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.KException;
import com.perm.vkontakte.api.User;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkFrActivity extends AbstractVkItems
{
    public static void setUsers(ArrayList<User> users)
    {
        VkFrActivity.users = users;
    }

    private static ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);

        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);



        if(users == null)
        {
            getFriends();
        }
        else
        {
            listView.setAdapter(new VkAlbumsAdapter());
        }
    }

    @Override
    protected void onClickRefresh()
    {
        getFriends();
    }

    @Override
    protected ArrayList<Audio> getAudios(int position) throws KException, JSONException, IOException
    {
        return MainScreenActivity.api.getAudio(users.get(position).uid, null, null,
                SettingActivity.getPreference("countfrgr"));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_vk_pgf,menu);
        return true;
    }

    private void getFriends()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                handler.sendEmptyMessage(1);
                try
                {
                    users = MainScreenActivity.api.getFriends(Account.account.user_id);
                    Serializator<User> factory = new Serializator<User>(getBaseContext(), Serializator.FileNames.User);
                    factory.inSerialize(users);

                } catch (IOException e)
                {
                    handler.sendEmptyMessage(2);
                    showException(e);
                    return;

                }
                catch (JSONException e)
                {
                    handler.sendEmptyMessage(2);
                    showException(e);
                    return;
                }
                catch (KException e)
                {

                    handler.sendEmptyMessage(2);
                    handleKException(e);
                    return;
                }
                handler.sendEmptyMessage(2);
                handler.sendEmptyMessage(3);


            }
        }).start();
    }
    private final Handler handler = new Handler()
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
                isLoading = false;
                linearLoading.setVisibility(View.GONE);

            }
            else if(msg.what == 3)
            {
                listView.setAdapter(new VkAlbumsAdapter());
            }

        }
    };
    private class VkAlbumsAdapter extends BaseAdapter
    {
        final LayoutInflater inflater;
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
            view = inflater.inflate(R.layout.item_friend,parent,false);
            ((TextView)view.findViewById(R.id.textFirstName)).setText(users.get(position).first_name);
            ((TextView)view.findViewById(R.id.textlastName)).setText(users.get(position).last_name);


            return view;
        }
    }
}
