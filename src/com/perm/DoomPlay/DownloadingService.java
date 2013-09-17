package com.perm.DoomPlay;

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
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import com.perm.vkontakte.api.Audio;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadingService extends Service
{
    public static final String keyDownload = "downloadTrack";

    int notifId;
    String folder;



    NotificationManager manager;
    Notification notification;
    RemoteViews views ;

    private Audio track;

    @Override
    public void onCreate()
    {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    AsyncTask<Void,Integer,Void> task;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {


        if(intent.getAction()!= null && intent.getAction().equals(PlayingService.actionClose))
        {
            if(task != null)
                task.cancel(true);
        }
        else
        {
            track = intent.getParcelableExtra(keyDownload);

            String tempFolder = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("download_folder","null");
            File tempFile = new File(tempFolder);

            if(tempFile.exists() && tempFile.isDirectory())
            {
                 folder = tempFolder + track.artist + "-" + track.title + ".mp3";
            }
            else
            {
                String defaultFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
                File defaultFile = new File(defaultFolder);

                assert  defaultFile.exists() && defaultFile.mkdir();

                folder = defaultFolder +track.artist + "-" + track.title + ".mp3";
            }


            notifId = (int)track.aid;
            go();
        }

        return START_NOT_STICKY;

    }
    void go()
    {
        task = new AsyncTask<Void, Integer, Void>()
        {
            void release()
            {
                if(connection != null)
                    connection.disconnect();
                if(stream != null)
                    try
                    {
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
                notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("audio/*");
                intent.setData(Uri.parse(folder));

                notification.contentIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                manager.notify(notifId,notification);

            }

            @Override
            protected void onCancelled()
            {
                super.onCancelled();


                views.setProgressBar(R.id.progressDownload,100,100,false);
                views.setTextViewText(R.id.notifTitle,"Canceled");
                notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
                manager.notify(notifId,notification);

                File file = new File(folder);
                if(file.exists())
                    file.delete();

                release();
            }


            @Override
            protected Void doInBackground(Void... params)
            {

                URL url ;

                try
                {
                    url = new URL(track.url);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoOutput(true);
                    connection.connect();

                    //int fileLength = connection.getContentLength();

                    stream = new FileOutputStream(new File(folder));
                    in  = new BufferedInputStream(url.openStream());
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    //long total = 0;

                    while((count = in.read(buffer)) > 0 && !isCancelled())
                    {
                        stream.write(buffer,0,count);
                        //total += count ;
                        //publishProgress((int) (total * 100 / fileLength));
                    }
                    stream.flush();
                    stream.close();
                    in.close();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Log.e("tag",e.toString());
                    release();

                }
                return null;
            }

        };
        task.execute();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
