package com.perm.ExceptionHandler;
/*
 * Copyright 2011 Oleg Elifantiev
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
 */


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

final public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{

    private final DateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
    private String versionName = "0";
    private int versionCode = 0;
    public final String stacktraceDir;
    Thread.UncaughtExceptionHandler oldHandler;

    public ExceptionHandler(Context context)
    {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        PackageManager mPackManager = context.getPackageManager();
        PackageInfo mPackInfo;
        try
        {
            mPackInfo = mPackManager.getPackageInfo(context.getPackageName(), 0);
            versionName = mPackInfo.versionName;
            versionCode = mPackInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {}

        stacktraceDir = String.format("/Android/data/%s/files/", context.getPackageName());
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception)
    {
        final Date dumpDate = new Date(System.currentTimeMillis());

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder
                .append("\n\n\n")
                .append(formatter.format(dumpDate)).append("\n")
                .append(String.format("Version: %s (%d)\n", versionName, versionCode))
                .append("version SDK = ").append(Build.VERSION.SDK_INT).append("\n")
                .append(thread.toString()).append("\n");
        processThrowable(exception, reportBuilder);


        GMailSender.sendEmail("exception", reportBuilder.toString());

        Log.e("EXCEPTION", reportBuilder.toString());
        oldHandler.uncaughtException(thread, exception);

    }

    private void processThrowable(Throwable exception, StringBuilder builder)
    {
        if(exception == null)
            return;
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        builder
                .append("Exception: ").append(exception.getClass().getName()).append("\n")
                .append("Message: ").append(exception.getMessage()).append("\nStacktrace:\n");
        for(StackTraceElement element : stackTraceElements) {
            builder.append("\t").append(element.toString()).append("\n");
        }
        processThrowable(exception.getCause(), builder);
    }
}