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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.example.DoomPlay.R;

public class SettingActivity extends SherlockPreferenceActivity
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

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }

}
