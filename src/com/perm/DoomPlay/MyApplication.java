package com.perm.DoomPlay;

import android.app.Application;
import com.perm.ExceptionHandler.ExceptionHandler;

public class MyApplication extends Application
{
    private static MyApplication instance;

    public static MyApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

    }
}
