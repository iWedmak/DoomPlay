package com.perm.DoomPlay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Api;
import com.perm.vkontakte.api.KException;
import org.json.JSONException;

import java.io.IOException;

public class MainVkFragment extends SherlockFragment
{
    MainScreenActivity activity;
    LinearLayout linearLoading;
    Api api ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        activity =(MainScreenActivity)getActivity();
        api=new Api(MainScreenActivity.account.access_token, LoginActivity.API_ID);
        View view = inflater.inflate(R.layout.main_vk_fragment,container,false);
        linearLoading = (LinearLayout)view.findViewById(R.id.linearLoading);
        view.findViewById(R.id.linearVkAll).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkTop).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkRecomended).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkSearch).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkAlbum).setOnClickListener(onClickVkListener);
        view.findViewById(R.id.linearVkFriends).setOnClickListener(onClickVkListener);
        return view;
    }
    void startAllTracks()
    {
        if(TracksHolder.tempAudiosMine == null)
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    handler.sendEmptyMessage(1);
                    try
                    {
                        TracksHolder.tempAudiosMine = api.getAudio(MainScreenActivity.account.user_id,null);
                    } catch (IOException e) {
                        e.printStackTrace();Toast.makeText(activity,"failed",Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace(); Toast.makeText(activity,"failed",Toast.LENGTH_SHORT).show();
                    } catch (KException e) {
                        e.printStackTrace();Toast.makeText(activity,"failed",Toast.LENGTH_SHORT).show();
                    }
                    handler.sendEmptyMessage(2);
                    handler.sendEmptyMessage(3);
                }
            }).start();
        else
            intentToAllTracks();
    }


    View.OnClickListener onClickVkListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(!MainScreenActivity.isRegister)
            {
                Toast.makeText(getActivity(), "please sign in", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!MainScreenActivity.isLoading )
            {
                if(Utils.isOnline(activity))
                {
                    switch (v.getId())
                    {
                        case R.id.linearVkAll:
                            startAllTracks();
                            break;
                        case R.id.linearVkAlbum:
                            startActivity(new Intent(activity,VkAlbumsActivity.class));
                            break;
                        case R.id.linearVkRecomended:
                            if(TracksHolder.tempAudiosRecomended == null)
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        handler.sendEmptyMessage(1);
                                        try
                                        {
                                            TracksHolder.tempAudiosRecomended = api.getAudioRecommendations(30,MainScreenActivity.account.user_id);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (KException e) {
                                            e.printStackTrace();
                                        }
                                        handler.sendEmptyMessage(2);
                                        handler.sendEmptyMessage(4);
                                    }
                                }).start();
                            else
                                intentToAllTracks();
                            break;
                        case R.id.linearVkSearch:
                            startActivity(new Intent(activity,SearchVkActivity.class));
                            break;
                        case R.id.linearVkTop:
                            startActivity(new Intent(activity,VkPopularActivity.class));
                            break;
                        case R.id.linearVkFriends:
                            startActivity(new Intent(activity,VkFriendsActivity.class));
                            break;
                    }
                }
            }
            else
                Toast.makeText(activity,"please wait",Toast.LENGTH_SHORT).show();
        }
    };
    void intentToAllTracks()
    {
        Intent intent = new Intent(activity,ListVkActivity.class);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,TracksHolder.tempAudiosMine);
        startActivity(intent);
    }
    void startRecomended()
    {
        Intent intent = new Intent(activity,ListVkActivity.class);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,TracksHolder.tempAudiosRecomended);
        startActivity(intent);
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
                intentToAllTracks();
            }
            else if(msg.what == 4)
            {
                startRecomended();
            }

        }
    };

}
