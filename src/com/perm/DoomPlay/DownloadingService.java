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
import java.util.Observable;
import java.util.Observer;

public class DownloadingService extends Service implements Observer
{
    public static final String keyDownload = "downloadTrack";
    private NotificationManager manager;

    private final static Map<Long,Download> downloads = new HashMap<Long,Download>();
    private final static Map<Long,DownloadNotifFactory> factories = new HashMap<Long, DownloadNotifFactory>();


    public static boolean isDownloading(long aid)
    {
        return downloads.containsKey(aid);
    }
    private void dispose(long aid)
    {
        downloads.remove(aid);
        factories.remove(aid);

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
            e.printStackTrace();
            return;
        }
        Download d = new Download(url,filePath,audio.getAid());
        d.addObserver(this);

        downloads.put(audio.getAid(),d);
        factories.put(audio.getAid(),new DownloadNotifFactory(audio,filePath,getBaseContext()));

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
            Download d = downloads.get(aid);
            if(d != null)
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


    @Override
    public void update(Observable observable, Object data)
    {
        long aid = (Long)data ;
        DownloadNotifFactory factory = factories.get(aid);

        Notification notification = null;

         switch (((Download)observable).getStatus())
         {
             case DOWNLOADING:
                 notification = factory.createStarting();
                 break;
             case CANCELLED:
                 notification = factory.createCanceled();
                 dispose(aid);
                 break;
             case COMPLETED:
                 notification = factory.createCompleted();
                 dispose(aid);
                 break;
             case ERROR:
                 notification = factory.createError();
                 dispose(aid);
                 break;
             case PAUSED:
                 //
                 break;

         }

        // bad convert , need solve this!!!!
        manager.notify((int)aid,notification);

    }
}
