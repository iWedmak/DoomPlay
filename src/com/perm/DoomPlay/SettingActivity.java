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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;

import java.io.File;

public class SettingActivity extends PreferenceActivity
{
    public static final String keyOnCall = "oncall";
    public static final String keyAfterCall = "aftercall";
    public static final String keyShortFocus = "shortfocus";
    public static final String keyLongFocus = "longfocus";
    public static final String keyDownloadArt = "downloadart";
    public static final String keySaveArt = "saveart";
    public static final String keyScroll = "scroll";
    public static final String keyOnGain = "gainfocus";
    public static final String keyOnClickNotif = "notifreturn";
    public static final String keyShowControls = "hideoncreate";


    public static boolean getPreferences(Context context, String key)
    {
         return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key,false);
    }
    public static int getPreference(Context context, String key)
    {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(key,"10"));
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);


        /*
        findPreference("download_folder").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent theIntent = new Intent(Intent.ACTION_PICK);

                theIntent.setData(Uri.parse("filePath://"));
                theIntent.putExtra(Intent.EXTRA_TITLE,"Choose directory");
                startActivityForResult(theIntent, 1);
                return true;
            }
        });
        */

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
           SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
           assert new File(data.getData().getPath()).isDirectory();
           edit.putString("download_folder",data.getData().getPath());
           edit.apply();
           edit.commit();
        }
    }

}
