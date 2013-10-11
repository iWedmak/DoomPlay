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
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Toast;

import java.util.Locale;

public class SettingActivity extends PreferenceActivity
{
    public static final String keyOnCall = "oncall";
    public static final String keyAfterCall = "aftercall";
    public static final String keyShortFocus = "shortfocus";
    public static final String keyLongFocus = "longfocus";
    public static final String keyScroll = "scroll";
    public static final String keyOnGain = "gainfocus";
    public static final String keyOnClickNotif = "notifreturn";
    public static final String keyShowControls = "hideoncreate";


    public static boolean getPreferences(String key)
    {
         return PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance()).getBoolean(key,false);
    }
    public static int getPreference(String key)
    {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance()).getString(key,"666"));
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);



        findPreference("contact").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"doomplaye@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
                i.putExtra(Intent.EXTRA_TEXT   , "body of email");
                try
                {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                }
                catch (android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.doesnt_available), Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        findPreference("github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/quxey/doomPlaye"));
                startActivity(browserIntent);
                return true;
            }
        });


        findPreference("languages").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                String languageToLoad ;
                if(newValue.equals("ru"))
                {
                     languageToLoad = "ru";
                }
                else
                {
                    languageToLoad = "en";
                }

                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, null);

                Intent intent = new Intent(getBaseContext(),MainScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
        });


    }

}
