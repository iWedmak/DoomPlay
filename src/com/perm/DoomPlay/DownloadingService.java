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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DownloadingService extends Service implements DoomObserver
{
    public static final String keyDownload = "downloadTrack";
    private NotificationManager manager;

    private final static Map<Long,DownloadHolder> downloads = new HashMap<Long,DownloadHolder>();


    @Override
    public void doomUpdate(Download observable, long aid)
    {
        DownloadHolder holder = downloads.get(aid);

        Notification notification = null;

        switch (observable.getStatus())
        {
            case DOWNLOADING:
                notification = holder.downloadBuilder.createStarting();
                break;
            case CANCELLED:
                notification = holder.downloadBuilder.createCanceled();
                dispose(aid);
                break;
            case COMPLETED:
                notification = holder.downloadBuilder.createCompleted();
                dispose(aid);
                break;
            case ERROR:
                notification = holder.downloadBuilder.createError();
                dispose(aid);
                break;
            case PAUSED:
                //
                break;

        }

        // bad convert , need solve this!!!!
        manager.notify(holder.notificationId,notification);

    }


    private static class DownloadHolder
    {
        Download download;
        DownloadNotifBuilder downloadBuilder;

        int notificationId = 0;

        static int counter  = 0;

        public DownloadHolder()
        {
            counter++;
            if(counter == Integer.MAX_VALUE)
                counter = 0;

            notificationId = counter;
        }
    }


    public static boolean isDownloading(long aid)
    {
        return downloads.containsKey(aid);
    }
    private void dispose(long aid)
    {
        downloads.remove(aid);

        if(downloads.size() == 0)
            stopSelf();

    }


    private void addDownload(Audio audio,String filePath)
    {
        URL url;
        try
        {
            url = new URL(audio.getUrl());
        }
        catch (MalformedURLException e)
        {
            if(downloads.size() == 0)
                stopSelf();

            e.printStackTrace();
            return;
        }
        Download d = new Download(url,filePath,audio.getAid());
        d.addObserver(this);


        DownloadHolder holder = new DownloadHolder();
        holder.download = d;
        holder.downloadBuilder = new DownloadNotifBuilder(audio,filePath,getBaseContext());
        downloads.put(audio.getAid(),holder);

        d.resume();


    }


    static String generateFilePath(Audio track)
    {
        String defaultFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
        File defaultFile = new File(defaultFolder);
        if(!defaultFile.exists() && !defaultFile.mkdirs())
            Log.e("tag","can't create directory");

        String title = track.getTitle() ;
        if(title.length() > 25)
            title.substring(0,25);


        String trackName = (track.getArtist() + "-" + title + ".mp3").replaceAll("[%#@+^:,&]","");


        return defaultFolder + trackName;
    }



    @Override
    public void onCreate()
    {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {


        if(intent.getAction().equals(PlayingService.actionClose))
        {
            long aid = intent.getLongExtra("aid", 666);
            Download d = downloads.get(aid).download;
            d.cancel();

        }
        else if(intent.getAction().equals(PlayingService.actionPlay))
        {
            Audio track = intent.getParcelableExtra(keyDownload);

            addDownload(track, generateFilePath(track));
        }

        return START_NOT_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
