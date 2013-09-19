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




import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;

public class AddTrackToAlbumDialog extends SherlockDialogFragment
{

    ListView listView;
    LinearLayout linearLoading;
    static boolean isLoading = false;

    public static boolean isAdding = false;
    public final static String keyDialogAlbum = "keybndleed";
    long trackId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        trackId = getArguments().getLong(keyDialogAlbum);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.list_vk,container,false);
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        listView = (ListView)view.findViewById(R.id.listVk);
        listView.setOnItemClickListener(onItemClickHandler);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(VkAlbumsActivity.albums == null && !isLoading)
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                    linearLoading.setVisibility(View.VISIBLE);
                    isLoading = true;
                }

                @Override
                protected Void doInBackground(Void... params)
                {
                    try
                    {
                        VkAlbumsActivity.albums = MainScreenActivity.api.getAudioAlbums(Account.account.user_id,null,
                                SettingActivity.getPreference(getActivity(),"countvkall"));

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
                protected void onPostExecute(Void aVoid)
                {
                    super.onPostExecute(aVoid);
                    listView.setAdapter(new DialogAddAdapter());
                    linearLoading.setVisibility(View.GONE);
                    isLoading = false;
                }
            }.execute();
        }
        else if(!isLoading)
        {
            listView.setAdapter(new DialogAddAdapter());
        }
    }

    AdapterView.OnItemClickListener onItemClickHandler = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

            new AsyncTask<Long, Void, Void>()
            {
                @Override
                protected Void doInBackground(Long... params)
                {
                    try
                    {
                        MainScreenActivity.api.moveToAudioAlbum(params[0],trackId);



                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(VkAlbumsActivity.albums.get(position).album_id);
            dismiss();
            Toast.makeText(getActivity(),"tracks added",Toast.LENGTH_SHORT).show();


        }
    };

    class DialogAddAdapter extends BaseAdapter
    {
        LayoutInflater inflater;
        public DialogAddAdapter()
        {
            inflater = getActivity().getLayoutInflater();
        }
        @Override
        public int getCount()
        {
            return VkAlbumsActivity.albums.size();
        }

        @Override
        public Object getItem(int position)
        {
            return VkAlbumsActivity.albums.get(position);
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
            TextView textView = (TextView)view.findViewById(R.id.textGenre);
            textView.setText(VkAlbumsActivity.albums.get(position).title);
            return view;
        }
    }
}

