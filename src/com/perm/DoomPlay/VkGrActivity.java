package com.perm.DoomPlay;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.Group;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkGrActivity extends AbstractVkItems
{
    public static void setGroups(ArrayList<Group> groups)
    {
        VkGrActivity.groups = groups;
    }

    private static ArrayList<Group> groups;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);

        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);



        if(groups == null)
        {
            getGroups();
        }
        else
        {
            listView.setAdapter(new VkAlbumsAdapter());
        }
    }

    @Override
    protected void onClickRefresh()
    {
        getGroups();
    }

    @Override
    protected ArrayList<Audio> getAudios(int position) throws KException, JSONException, IOException
    {
        return MainScreenActivity.api.getAudio(null,groups.get(position).gid,null,
                SettingActivity.getPreference("countfrgr"));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_vk_pgf,menu);
        return true;
    }

    private void getGroups()
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

                    Serializator<Group> factory = new Serializator<Group>(getBaseContext(), Serializator.FileNames.Group);
                    factory.inSerialize(groups);

                } catch (IOException e)
                {
                    showException(e);
                    handler.sendEmptyMessage(2);
                    return;

                } catch (JSONException e)
                {
                    showException(e);
                    handler.sendEmptyMessage(2);
                    return;

                } catch (KException e)
                {

                    handleKException(e);
                    handler.sendEmptyMessage(2);
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
                linearLoading.setVisibility(View.GONE);
                isLoading = false;

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
            return groups.size();
        }

        @Override
        public Object getItem(int position)
        {

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
            view = inflater.inflate(R.layout.item_group,parent,false);
            ((TextView)view.findViewById(R.id.textGroup)).setText(groups.get(position).name);

            return view;
        }
    }
}
