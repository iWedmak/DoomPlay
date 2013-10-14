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
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.File;

class DownloadNotifBuilder
{
    private final Audio track;
    private final String filePath;
    private final Context context;

    public int notificationId = 0;

    private static int counter  = 0;


    public DownloadNotifBuilder(Audio track, String filePath, Context context)
    {
        this.track = track;
        this.filePath = filePath;
        this.context = context;

        counter++;
        if(counter == Integer.MAX_VALUE)
            counter = 0;

        notificationId = counter;
    }

    private Notification createStartingOld()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setOngoing(true);
        builder.setContentTitle( context.getResources().getString(R.string.downloading));
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.download_icon);

        Intent intentClose = new Intent(PlayingService.actionClose);
        intentClose.putExtra("aid", track.getAid());
        intentClose.setComponent(new ComponentName(context, DownloadingService.class));

        builder.setContentIntent(PendingIntent.getService(context, notificationId, intentClose, PendingIntent.FLAG_UPDATE_CURRENT));

        return builder.build();
    }

    public Notification createStarting()
    {
        if(Build.VERSION.SDK_INT >= 11)
            return createStartingNew();
        else
            return createStartingOld();
    }

    private Notification createStartingNew()
    {
        Notification notification = new Notification();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notif_download);

        views.setProgressBar(R.id.progressDownload, 100,0, true);
        views.setTextViewText(R.id.notifTitle, context.getResources().getString(R.string.downloading));
        views.setTextViewText(R.id.notifArtist, track.getArtist() + "-" + track.getTitle());
        views.setImageViewResource(R.id.notifPause,R.drawable.widget_pause);

        ComponentName componentName = new ComponentName(context,DownloadingService.class);

        Intent intentClose = new Intent(PlayingService.actionClose);
        intentClose.putExtra("aid",track.getAid());
        intentClose.setComponent(componentName);

        Intent intentPause = new Intent(PlayingService.actionIconPause);
        intentPause.putExtra("aid",track.getAid());
        intentPause.setComponent(componentName);

        views.setOnClickPendingIntent(R.id.notifClose,
                PendingIntent.getService(context, notificationId, intentClose,PendingIntent.FLAG_UPDATE_CURRENT));

        views.setOnClickPendingIntent(R.id.notifPause,
                PendingIntent.getService(context, notificationId, intentPause,PendingIntent.FLAG_UPDATE_CURRENT));

        notification.contentView = views;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.icon = R.drawable.download_icon;

        return notification;

    }


    public Notification createPaused()
    {
        Notification notification = new Notification();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notif_download);

        views.setProgressBar(R.id.progressDownload, 100,0, false);
        views.setTextViewText(R.id.notifTitle,  context.getResources().getString(R.string.paused));
        views.setTextViewText(R.id.notifArtist, track.getArtist() + "-" + track.getTitle());
        views.setImageViewResource(R.id.notifPause,R.drawable.widget_play);

        Intent intentClose = new Intent(PlayingService.actionClose);
        intentClose.putExtra("aid",track.getAid());
        intentClose.setComponent(new ComponentName(context,DownloadingService.class));

        Intent intentPause = new Intent(PlayingService.actionIconPlay);
        intentPause.putExtra("aid",track.getAid());
        intentPause.setComponent(new ComponentName(context,DownloadingService.class));

        views.setOnClickPendingIntent(R.id.notifClose,
                PendingIntent.getService(context, notificationId, intentClose,PendingIntent.FLAG_UPDATE_CURRENT));

        views.setOnClickPendingIntent(R.id.notifPause,
                PendingIntent.getService(context, notificationId, intentPause,PendingIntent.FLAG_UPDATE_CURRENT));

        notification.contentView = views;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.icon = R.drawable.download_icon;

        return notification;
    }

    public Notification createCompleted()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setOngoing(false);
        builder.setContentTitle( context.getResources().getString(R.string.completed));
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.downloaded);

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "audio/*");

        builder.setContentIntent(PendingIntent.getActivity(context,0,intent,0));
        builder.setAutoCancel(true);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + filePath)));

        return builder.build();
    }

    public Notification createCanceled()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setOngoing(false);
        builder.setContentTitle( context.getResources().getString(R.string.canceled));
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.downloaded);

        return builder.build();
    }
    public Notification createError()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setOngoing(false);
        builder.setContentTitle( context.getResources().getString(R.string.error));
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.downloaded);

        return builder.build();
    }


}
