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
import android.widget.RemoteViews;

import java.io.File;

public class DownloadNotifBuilder
{
    private final Audio track;
    private final String filePath;
    private final Context context;


    public DownloadNotifBuilder(Audio track, String filePath, Context context)
    {
        this.track = track;
        this.filePath = filePath;
        this.context = context;

    }

    public Notification createStarting()
    {

        Notification notification = new Notification();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notif_download);

        views.setProgressBar(R.id.progressDownload, 100, 0, true);
        views.setTextViewText(R.id.notifTitle, "Downloading");
        views.setTextViewText(R.id.notifArtist, track.getArtist() + "-" + track.getTitle());

        Intent intentClose = new Intent(PlayingService.actionClose);
        intentClose.putExtra("aid",track.getAid());
        intentClose.setComponent(new ComponentName(context,DownloadingService.class));

        views.setOnClickPendingIntent(R.id.notifClose,
                PendingIntent.getService(context, 0, intentClose, PendingIntent.FLAG_UPDATE_CURRENT));


        notification.contentView = views;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.icon = R.drawable.download_icon;

        return notification;
    }

    public Notification createProcessing()
    {
         return null;
    }

    public Notification createCompleted()
    {
        Notification.Builder builder = new Notification.Builder(context);

        builder.setOngoing(false);
        builder.setContentTitle("completed");
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.downloaded);

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "audio/*");

        builder.setContentIntent(PendingIntent.getActivity(context,0,intent,0));

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + filePath)));

        return builder.build();
    }

    public Notification createCanceled()
    {
        Notification.Builder builder = new Notification.Builder(context);

        builder.setOngoing(false);
        builder.setContentTitle("canceled");
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.downloaded);

        return builder.build();
    }
    public Notification createError()
    {
        Notification.Builder builder = new Notification.Builder(context);

        builder.setOngoing(false);
        builder.setContentTitle("error");
        builder.setContentText(track.getArtist() + "-" + track.getTitle());
        builder.setSmallIcon(R.drawable.downloaded);

        return builder.build();
    }


}
