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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.perm.vkontakte.api.Account;
import com.perm.vkontakte.api.Api;

public class MainScreenActivity extends AbstractReceiver
{
    public final static String keyOpenInListTrack = "gtsity";
    public static final boolean isJellyBean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean isOldSDK =  Build.VERSION.SDK_INT <= 8;
    private static final int REQUEST_LOGIN = 1093;
    public static boolean isLoading;
    public static boolean isRegister = false;
    private ViewPager viewPager;
    static Api api;

    interface IScanCallback
    {
        void scanI();
    }
    private IScanCallback scanCallback;

    void setScanCallback(IScanCallback scanCallback)
    {
         this.scanCallback = scanCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager)findViewById(R.id.viewPagerMain);
        viewPager.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {
                 ActivityCompat.invalidateOptionsMenu(MainScreenActivity.this);
            }
            @Override
            public void onPageSelected(int i){}
            @Override
            public void onPageScrollStateChanged(int i){}
        });
        isLoading = false;
        Account.account.restore(this);

        if(Account.account.access_token != null)
        {
            isRegister = true;
            api = new Api(Account.account.access_token, LoginActivity.API_ID);
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(viewPager.getCurrentItem() == 0)
            getMenuInflater().inflate(R.menu.bar_main,menu);
        else
            getMenuInflater().inflate(R.menu.bar_vk,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemRescan:
                if(!isLoading)
                    scanCallback.scanI();
                else
                    AbstractList.waitMessage(this);
                return true;
            case R.id.itemExit:
                if(!isLoading)
                    sendBroadcast(new Intent(actionKill));
                else
                    AbstractList.waitMessage(this);
                return true;
            case R.id.itemSettings:
                startActivity(new Intent(this,SettingActivity.class));
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
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_LOGIN)
        {
            if (resultCode == RESULT_OK)
            {
                Account.account.access_token=data.getStringExtra("token");
                Account.account.user_id=data.getLongExtra("user_id", 0);
                Account.account.save(this);
                api = new Api(Account.account.access_token, LoginActivity.API_ID);
                isRegister = true;
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
                return new MainScreenFragment();
            else
                return new MainVkFragment();
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
