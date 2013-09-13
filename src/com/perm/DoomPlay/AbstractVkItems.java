package com.perm.DoomPlay;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.DoomPlay.R;
import com.perm.vkontakte.api.Audio;

import java.util.ArrayList;

abstract class AbstractVkItems extends AbstractReceiver
{
    static boolean isLoading;
    LinearLayout linearLoading;
    ListView listView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.bar_vk_pgf,menu);
        return super.onCreateOptionsMenu(menu);
    }
    protected abstract void onClickRefresh();
    protected abstract ArrayList<Audio> getAudios(int position);

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemRefresh:
                onClickRefresh();
                return true;
            case R.id.itemInterrupt:
                cancelLoading();
                return true;
            case R.id.itemExit:
                sendBroadcast(new Intent(actionKill));
                return true;
            case R.id.itemSettings:
                startActivity(new Intent(this,SettingActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private TaskLoader taskLoader;

    @Override
    public void onBackPressed()
    {
        cancelLoading();
        super.onBackPressed();
    }
    AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

            if(!isLoading)
            {
                if(Utils.isOnline(getBaseContext()))
                {
                    taskLoader = new TaskLoader();
                    taskLoader.execute(position);
                }

            }
            else
                Toast.makeText(getBaseContext(), "please wait", Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {

        return super.onMenuItemSelected(featureId, item);
    }
    void cancelLoading()
    {
        if(isLoading && taskLoader != null)
        {
            isLoading = false;
            linearLoading.setVisibility(View.GONE);
            taskLoader.cancel(true);
        }
    }
    protected void startListVkActivity(ArrayList<Audio> audios)
    {
        Intent intent = new Intent(this,ListVkActivity.class);
        intent.setAction(ListVkActivity.actionJust);
        intent.putExtra(MainScreenActivity.keyOpenInListTrack,audios);
        startActivity(intent);
    }
    protected void setLoading()
    {
         if(isLoading)
         {
             isLoading = false;
             linearLoading.setVisibility(View.GONE);
         }
         else
         {
             linearLoading.setVisibility(View.VISIBLE);
             isLoading = true;
         }
    }



    class TaskLoader extends AsyncTask<Integer,Void,ArrayList<Audio>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            setLoading();
        }

        @Override
        protected ArrayList<Audio> doInBackground(Integer... params)
        {
            return getAudios(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Audio> audios)
        {
            super.onPostExecute(audios);
            setLoading();

            if(audios == null)
                Toast.makeText(getBaseContext(),"can't get Audio",Toast.LENGTH_SHORT).show();
            else
                startListVkActivity(audios);
        }
    }
}
