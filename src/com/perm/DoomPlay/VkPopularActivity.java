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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class VkPopularActivity extends AbstractVkItems
{
    public final static String[] genres = {"Rock","Pop","Rap & Hip-Hop","Easy Listening",
            "Dance & House","Instrumental","Metal","Alternative","Dubstep","Jazz & Blues","Drum & Bass",
            "Trance","Chanson","Ethnic","Acoustic & Vocal","Reggae","Classical","Indie Pop","Speech","Electropop & Disco","Other"};
    public final static int[] acordingIds = {1,2,3,4,5,6,7,21,8,9,10,11,12,13,14,15,16,17,19,22,18};


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


    @Override
    protected void onClickRefresh()
    {

    }

    @Override
    protected ArrayList<Audio> getAudios(int position)
    {
        try {
            return MainScreenActivity.api.getAudioPopular(acordingIds[position],
                    SettingActivity.getPreference("countvkpopular"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (KException e) {
            e.printStackTrace();
        }
        return null;
    }


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
