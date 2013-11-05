package com.perm.DoomPlay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

public class EqualizerActivity extends AbstractReceiver
{
    private int currentTab;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.empty);

        if (savedInstanceState != null)
            currentTab = savedInstanceState.getInt("currentTab");

        prepareTabs();


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTab",currentTab);
    }

    private void prepareTabs()
    {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab tab = actionBar.newTab().setText("Bands");
        tab.setTabListener(new MyTabsListener(new EqualizerBandsFragment()){
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                super.onTabSelected(tab, ft);
                currentTab = tab.getPosition();
            }
        });
        actionBar.addTab(tab);


        tab = actionBar.newTab().setText("Effects");
        tab.setTabListener(new MyTabsListener(new EqualizerEffectsFragment()){
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                super.onTabSelected(tab, ft);
                currentTab = tab.getPosition();
            }
        });
        actionBar.addTab(tab);

        actionBar.setSelectedNavigationItem(currentTab);
    }


    private static class MyTabsListener implements ActionBar.TabListener {
        public Fragment fragment;

        public MyTabsListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.replace(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

    }
}
