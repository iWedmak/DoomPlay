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
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadingService extends Service
{
    public static final String keyDownload = "downloadTrack";

    int notifId;
    String filePath;
    NotificationManager manager;
    Notification notification;
    RemoteViews views ;
    private Audio track;
    Downloader task;




    @Override
    public void onCreate()
    {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {


        if(intent.getAction()!= null && intent.getAction().equals(PlayingService.actionClose))
        {
            if(intent.getIntExtra("notificationId",666) == notifId && task != null)
                task.cancel(true);
        }
        else
        {
            track = intent.getParcelableExtra(keyDownload);
            notifId = (int)track.aid;

            String defaultFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
            File defaultFile = new File(defaultFolder);

            if(!defaultFile.exists())
                defaultFile.mkdir();

            filePath = defaultFolder +track.artist + "-" + track.title + ".mp3";




            task = new Downloader();
            task.execute();
        }

        return START_NOT_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    class Downloader extends AsyncTask<Void, Integer, Void>
    {

        void download() throws IOException
        {
            URL url = new URL(track.url);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            stream = new FileOutputStream(new File(filePath));
            in  = new BufferedInputStream(url.openStream());
            byte[] buffer = new byte[1024];
            int count ;

            while((count = in.read(buffer)) > 0 && !isCancelled())
            {
                stream.write(buffer,0,count);
            }
            stream.flush();
            stream.close();
            in.close();
        }

        void release()
        {
            if(connection != null)
                connection.disconnect();
            if(stream != null)
                try
                {
                    stream.flush();
                    stream.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                    Log.e("tag", e1.toString());
                }
            if(in != null)
                try
                {
                    in.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                    Log.e("tag", e1.toString());
                }
            stopSelf();
        }

        HttpURLConnection connection = null;
        FileOutputStream stream = null;
        InputStream in = null;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            views = new RemoteViews(getPackageName(),R.layout.notif_download);
            views.setProgressBar(R.id.progressDownload,100,0,true);
            views.setTextViewText(R.id.notifTitle,"Downloading");
            views.setTextViewText(R.id.notifArtist,track.artist + "-" + track.title);

            Intent intentClose = new Intent(PlayingService.actionClose);
            intentClose.putExtra("notificationId",notifId);
            intentClose.setComponent(new ComponentName(getBaseContext(),DownloadingService.class));

            views.setOnClickPendingIntent(R.id.notifClose, PendingIntent.getService(getBaseContext(),0,intentClose,0));

            notification = new Notification();
            notification.contentView = views;
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.icon = R.drawable.download_icon;

            manager.notify(notifId,notification);


        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            stopSelf();


            views.setProgressBar(R.id.progressDownload,100,100,false);
            views.setTextViewText(R.id.notifTitle,"Finish");
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification.icon = R.drawable.downloaded;
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "audio/*");

            notification.contentIntent = PendingIntent.getActivity(getBaseContext(),0,intent,0);

            MyApplication.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + filePath)));

            manager.notify(notifId,notification);

        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();


            views.setProgressBar(R.id.progressDownload,100,100,false);
            views.setTextViewText(R.id.notifTitle,"Canceled");
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            notification.icon = R.drawable.downloaded;
            manager.notify(notifId,notification);

            File file = new File(filePath);
            if(file.exists())
                file.delete();

            release();
        }


        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                download();
            }
            catch (IOException e)
            {
                Log.e("EXCEPTION",e.toString());
                release();

            }
            return null;
        }
    }

}
