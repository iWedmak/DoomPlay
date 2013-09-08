package com.perm.DoomPlay;

import android.app.Application;
import com.perm.ExceptionHandler.ExceptionHandler;

public class MyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

    }
}
