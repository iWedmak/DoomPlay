package com.perm.DoomPlay;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.io.File;

public class DownloadNotifFactory
{
    private final Audio track;
    private final String filePath;
    private final Context context;
    private Notification notification;
    private RemoteViews views;


    public DownloadNotifFactory(Audio track,String filePath,Context context)
    {
        this.track = track;
        this.filePath = filePath;
        this.context = context;

        notification = new Notification();
        views = new RemoteViews(context.getPackageName(),R.layout.notif_download);
    }

    public Notification createStarting()
    {
        views.setProgressBar(R.id.progressDownload,100,0,true);
        views.setTextViewText(R.id.notifTitle,"Downloading");
        views.setTextViewText(R.id.notifArtist, track.getArtist() + "-" + track.getTitle());

        Intent intentClose = new Intent(PlayingService.actionClose);
        intentClose.putExtra("aid",track.getAid());
        intentClose.setComponent(new ComponentName(context,DownloadingService.class));

        views.setOnClickPendingIntent(R.id.notifClose, PendingIntent.getService(context, 0, intentClose, 0));


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
        notification = new Notification();

        views.setProgressBar(R.id.progressDownload,100,100,false);
        views.setTextViewText(R.id.notifTitle,"Finish");
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.downloaded;
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "audio/*");

        notification.contentIntent = PendingIntent.getActivity(context,0,intent,0);
        notification.contentView = views;

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + filePath)));
        return notification;
    }

    public Notification createCanceled()
    {
        notification = new Notification();

        views.setProgressBar(R.id.progressDownload,100,100,false);
        views.setTextViewText(R.id.notifTitle,"Canceled");
        views.setTextViewText(R.id.notifArtist, track.getArtist() + "-" + track.getTitle());
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
        notification.icon = R.drawable.downloaded;
        notification.contentView = views;

        return notification;
    }
    public Notification createError()
    {
        notification = new Notification();

        views.setProgressBar(R.id.progressDownload,100,100,false);
        views.setTextViewText(R.id.notifTitle,"Sory,Error");
        views.setTextViewText(R.id.notifArtist, track.getArtist() + "-" + track.getTitle());
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
        notification.icon = R.drawable.downloaded;
        notification.contentView = views;

        return notification;
    }


}
