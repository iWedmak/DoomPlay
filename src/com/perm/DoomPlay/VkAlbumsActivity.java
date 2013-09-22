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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.view.ActionMode;
import android.view.*;
import android.widget.*;
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
    static int currentAlbum;
    VkAlbumsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk);
        listView = (ListView)findViewById(R.id.listVk);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
        listView.setOnItemClickListener(onClickListener);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentAlbum = position;
                startSupportActionMode(callback).setTag(position);
                return true;
            }
        });


        if(albums == null && Utils.isOnline(getBaseContext()))
        {
             getAlbums();
        }
        else
        {
            adapter = new VkAlbumsAdapter();
            listView.setAdapter(adapter);
        }
    }

    ActionMode.Callback callback = new ActionMode.Callback()
    {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getMenuInflater().inflate(R.menu.action_vk_album,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false;}

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            //int position = (Integer)mode.getTag();
            switch (item.getItemId())
            {
                case R.id.itemEditAlbum:
                {
                     AddListDialog dialog = new AddListDialog()
                     {
                         @Override
                         boolean isPlaylistExist(String playlist)
                         {
                             for(AudioAlbum album : albums)
                             {
                                 if(album.title.equals(playlist))
                                     return true;
                             }
                             return false;
                         }

                         @Override
                         void createPlatlist(String playlist)
                         {
                             new AsyncTask<String,Void,Void>()
                             {

                                 @Override
                                 protected Void doInBackground(String... params)
                                 {
                                     try
                                     {
                                         handler.sendEmptyMessage(1);
                                         MainScreenActivity.api.editAudioAlbum(params[0], albums.get(currentAlbum).album_id);
                                         albums.set(currentAlbum,new AudioAlbum(albums.get(currentAlbum).album_id,params[0]));
                                         handler.sendEmptyMessage(2);
                                     }


                                     catch (IOException e)
                                     {
                                         e.printStackTrace();
                                     } catch (JSONException e)
                                     {
                                         e.printStackTrace();
                                     } catch (KException e)
                                     {
                                         e.printStackTrace();
                                     }
                                     return null;
                                 }

                                 @Override
                                 protected void onPostExecute(Void aVoid)
                                 {
                                     super.onPostExecute(aVoid);
                                     adapter.notifyDataSetChanged();
                                 }
                             }.execute(playlist) ;
                         }
                     };
                    dialog.show(getSupportFragmentManager(),"tag");
                    break;
                }
                case R.id.itemDeleteAlbum:
                {
                    new AsyncTask<Void,Void,Void>()
                    {

                        @Override
                        protected Void doInBackground(Void... params)
                        {
                            try
                            {
                                handler.sendEmptyMessage(1);
                                MainScreenActivity.api.deleteAudioAlbum(albums.get(currentAlbum).album_id);
                                albums.remove(currentAlbum);
                                handler.sendEmptyMessage(2);
                            }


                            catch (IOException e)
                            {
                                e.printStackTrace();
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            } catch (KException e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid)
                        {
                            super.onPostExecute(aVoid);
                            adapter.notifyDataSetChanged();
                        }
                    }.execute();
                    break;
                }


            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_vk_album,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.itemNewAlbum)
        {
            AddListDialog dialog = new AddListDialog()
            {
                @Override
                boolean isPlaylistExist(String playlist)
                {
                    for(AudioAlbum album : albums)
                    {
                        if(album.title.equals(playlist))
                            return true;
                    }
                    return false;
                }

                @Override
                void createPlatlist(String playlist)
                {


                    new AsyncTask<String,Void,Void>()
                    {

                        @Override
                        protected Void doInBackground(String... params)
                        {
                            try
                            {
                                handler.sendEmptyMessage(1);
                                albums.add(0,new AudioAlbum(MainScreenActivity.api.addAudioAlbum(params[0]),params[0]));
                                handler.sendEmptyMessage(2);
                            }


                            catch (IOException e)
                            {
                                e.printStackTrace();
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            } catch (KException e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid)
                        {
                            super.onPostExecute(aVoid);
                            adapter.notifyDataSetChanged();
                        }
                    }.execute(playlist) ;



                }
            };
            dialog.show(getSupportFragmentManager(),"tag");
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    void getAlbums()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                handler.sendEmptyMessage(1);
                try
                {
                    albums = MainScreenActivity.api.getAudioAlbums(Account.account.user_id,null,
                            SettingActivity.getPreference("countvkall"));

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
                adapter = new VkAlbumsAdapter();
                listView.setAdapter(adapter);
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
        currentAlbum = position;
        try
        {

            return MainScreenActivity.api.getAudio(null,null,albums.get(position).album_id
                    ,SettingActivity.getPreference("countvkall"));

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
