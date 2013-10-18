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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.Api;

public class MainScreenActivity extends AbstractReceiver
{
    public final static String keyOpenInListTrack = "gtsity";
    public static final boolean isJellyBean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean isOldSDK =  Build.VERSION.SDK_INT <= 8;
    private static final int REQUEST_LOGIN = 1093;
    public static boolean isLoading;
    public static boolean isRegister;
    static Api api;
    private MainLocalFragment localFragment;
    private MainVkFragment vkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if(!TracksHolder.isScanned())
            scan();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPagerMain);
        viewPager.setAdapter(new MainPageAdapter(getSupportFragmentManager()));

        if(savedInstanceState != null)
        {
            vkFragment =(MainVkFragment)getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString("vkFr"));
            localFragment =(MainLocalFragment)getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString("locFr"));
        }

        isLoading = false;
        Account.account.restore(this);

        if(Account.account.access_token != null)
        {
            isRegister = true;
            api = new Api(Account.account.access_token, LoginActivity.API_ID);
        }
        else
        {
            isRegister = false;
        }



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bar_main,menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if(vkFragment != null)
            outState.putString("vkFr",vkFragment.getTag());
        if(localFragment != null)
            outState.putString("locFr",localFragment.getTag());

    }

    private void scan()
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                MainScreenActivity.isLoading = true;

                if(vkFragment != null)
                    vkFragment.setLoading();
                if(localFragment != null)
                    localFragment.setLoading();

                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                TracksHolder.scanCard(getBaseContext());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                MainScreenActivity.isLoading = false;
                vkFragment.unsetLoading();
                localFragment.unsetLoading();

            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemRescan:
                if(isLoading)
                    AbstractList.waitMessage(this);
                else
                {
                   scan();
                }
                return true;
            case R.id.itemSendReport:
                new ReportDialog().show(getSupportFragmentManager(),"tag");
                return true;
            case R.id.itemSignIn:
                startLoginActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void startLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_LOGIN)
        {
            if (resultCode == RESULT_OK)
            {
                Account.account.access_token = data.getStringExtra("token");
                Account.account.user_id = data.getLongExtra("user_id", 0);
                Account.account.save(this);
                api = new Api(Account.account.access_token, LoginActivity.API_ID);
                isRegister = true;

                Toast.makeText(getBaseContext(),getResources().getString(R.string.registr_succes),Toast.LENGTH_SHORT).show();
            }
        }
    }
    class MainPageAdapter extends FragmentPagerAdapter
    {
        public MainPageAdapter(FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public Fragment getItem(int position)
        {
            if(position == 0)
            {
                localFragment = new MainLocalFragment();
                return localFragment;
            }
            else
            {
                vkFragment = new MainVkFragment();
                return vkFragment;
            }

        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            if(position == 0)
                return "offline";
            else
                return "vk";
        }

        @Override
        public int getCount()
        {
            return 2;
        }

    }


}
